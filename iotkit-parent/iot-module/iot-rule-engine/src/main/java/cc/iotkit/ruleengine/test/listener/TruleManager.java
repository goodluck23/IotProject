package cc.iotkit.ruleengine.test.listener;

import cc.iotkit.common.api.PageRequest;
import cc.iotkit.common.api.Paging;
import cc.iotkit.common.utils.JsonUtils;
import cc.iotkit.common.utils.StringUtils;
import cc.iotkit.data.manager.*;
import cc.iotkit.message.model.Message;
import cc.iotkit.message.service.MessageService;
import cc.iotkit.model.alert.AlertConfig;
import cc.iotkit.model.notify.Channel;
import cc.iotkit.model.notify.ChannelConfig;
import cc.iotkit.model.notify.ChannelTemplate;
import cc.iotkit.model.rule.FilterConfig;
import cc.iotkit.model.rule.RuleAction;
import cc.iotkit.model.rule.RuleInfo;
import cc.iotkit.ruleengine.action.Action;
import cc.iotkit.ruleengine.action.alert.AlertAction;
import cc.iotkit.ruleengine.action.alert.AlertService;
import cc.iotkit.ruleengine.action.device.DeviceAction;
import cc.iotkit.ruleengine.action.device.DeviceActionService;
import cc.iotkit.ruleengine.action.http.HttpAction;
import cc.iotkit.ruleengine.action.http.HttpService;
import cc.iotkit.ruleengine.action.kafka.KafkaAction;
import cc.iotkit.ruleengine.action.kafka.KafkaService;
import cc.iotkit.ruleengine.action.mqtt.MqttAction;
import cc.iotkit.ruleengine.action.mqtt.MqttService;
import cc.iotkit.ruleengine.action.tcp.TcpAction;
import cc.iotkit.ruleengine.action.tcp.TcpService;
import cc.iotkit.ruleengine.config.RuleConfiguration;
import cc.iotkit.ruleengine.filter.DeviceFilter;
import cc.iotkit.ruleengine.filter.Filter;
import cc.iotkit.ruleengine.link.LinkFactory;
import cc.iotkit.ruleengine.listener.DeviceListener;
import cc.iotkit.ruleengine.listener.Listener;
import cc.iotkit.ruleengine.rule.Rule;
import cc.iotkit.ruleengine.rule.RuleMessageHandler;
import cc.iotkit.ruleengine.test.filter.TdeviceFilter;
import cn.hutool.core.collection.CollectionUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TruleManager {

    @Autowired
    private RuleConfiguration ruleConfiguration;

    @Autowired
    private RuleMessageHandler ruleMessageHandler;

    @Autowired
    private IRuleInfoData ruleInfoData;

    @Autowired
    @Qualifier("deviceInfoPropertyDataCache")
    private IDeviceInfoData deviceInfoData;

    @Autowired
    private DeviceActionService deviceActionService;

    @Autowired
    private IAlertConfigData alertConfigData;

    @Autowired
    private IChannelTemplateData channelTemplateData;

    @Autowired
    private IChannelConfigData channelConfigData;

    @Autowired
    private IChannelData channelData;

    @Autowired
    private MessageService messageService;

    public TruleManager() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.schedule(this::initRules, 1, TimeUnit.SECONDS);
    }

    @SneakyThrows
    public void initRules() {
        int idx = 1;
        while (true) {
            PageRequest<RuleInfo> pageRequest = new PageRequest<>();
            pageRequest.setPageNum(idx);
            pageRequest.setPageSize(100);
            Paging<RuleInfo> all = ruleInfoData.findAll(pageRequest);
            List<RuleInfo> rules = all.getRows();
            if (CollectionUtil.isEmpty(rules)) {
                return;
            }
            rules.forEach(rule -> {
                try {
                    //不添加停止的规则
                    if (RuleInfo.STATE_STOPPED.equals(rule.getState())) {
                        return;
                    }
                    log.info("got rule {} to init", rule.getId());
                    add(rule);
                } catch (Throwable e) {
                    log.error("add rule error", e);
                }
            });
            idx++;
        }
    }

    public void add(RuleInfo ruleInfo) {
        Rule rule = parseRule(ruleInfo);
        ruleMessageHandler.putRule(rule);
    }

    public void remove(String ruleId) {
        ruleMessageHandler.removeRule(ruleId);
        // 移出link连接
        LinkFactory.ruleClose(ruleId);
    }

    public void pause(String ruleId) {
        remove(ruleId);
    }

    public void resume(RuleInfo ruleInfo) {
        add(ruleInfo);
    }

    private Rule parseRule(RuleInfo ruleInfo) {
       List<Listener<?>> listeners=new ArrayList<>();
       List<Filter<?>> filters=new ArrayList<>();
       Rule rule=new Rule();
       //List<FilterConfig> listeners;
        for (FilterConfig filterConfig : ruleInfo.getListeners()){
            if(StringUtils.isBlank(filterConfig.getConfig()))
                continue;
            listeners.add(parseListener(filterConfig.getType(),filterConfig.getConfig()));
        }
        rule.setListeners(listeners);
        for (FilterConfig filterConfig : ruleInfo.getListeners()){
            if (StringUtils.isBlank(filterConfig.getConfig()))
                continue;
            filters.add(parseFilter(filterConfig.getType(),filterConfig.getConfig()));
        }

       return rule;

    }

    private Listener<?> parseListener(String type, String config) {
        if (TdeviceListener.TYPE.equals(type)) {
            //Listener<TdeviceCondition> conditionListener
            return parse(config, TdeviceListener.class);
        }
        return null;

    }

    private Filter<?> parseFilter(String type, String config) {
        if(TdeviceFilter.TYPE.equals(type)){
            return  parse(config,TdeviceFilter.class);
        }

        return null;
    }

    private Action<?> parseAction(String ruleId, String type, String config) {
        if (DeviceAction.TYPE.equals(type)) {
            DeviceAction action = parse(config, DeviceAction.class);
            action.setDeviceActionService(deviceActionService);
            return action;
        } else if (HttpAction.TYPE.equals(type)) {
            HttpAction httpAction = parse(config, HttpAction.class);
            for (HttpService service : httpAction.getServices()) {
                service.setDeviceInfoData(deviceInfoData);
            }
            return httpAction;
        } else if (MqttAction.TYPE.equals(type)) {
            MqttAction mqttAction = parse(config, MqttAction.class);
            for (MqttService service : mqttAction.getServices()) {
                service.setDeviceInfoData(deviceInfoData);
                service.initLink(ruleId);
            }
            return mqttAction;
        } else if (KafkaAction.TYPE.equals(type)) {
            KafkaAction kafkaAction = parse(config, KafkaAction.class);
            for (KafkaService service : kafkaAction.getServices()) {
                service.setDeviceInfoData(deviceInfoData);
                service.initLink(ruleId);
            }
            return kafkaAction;
        } else if (TcpAction.TYPE.equals(type)) {
            TcpAction tcpAction = parse(config, TcpAction.class);
            for (TcpService service : tcpAction.getServices()) {
                service.setDeviceInfoData(deviceInfoData);
                service.initLink(ruleId);
            }
            return tcpAction;
        } else if (AlertAction.TYPE.equals(type)) {
            List<AlertConfig> alertConfigs = alertConfigData.findAllByCondition(AlertConfig.builder()
                    .ruleInfoId(ruleId)
                    .build());

            AlertAction alertAction = parse(config, AlertAction.class);
            String script = alertAction.getServices().get(0).getScript();

            List<AlertService> alertServices = new ArrayList<>();
            for (AlertConfig alertConfig : alertConfigs) {
                if(alertConfig.getEnable()!=null && !alertConfig.getEnable()){
                    continue;
                }

                AlertService service = new AlertService();
                service.setScript(script);
                service.setDeviceInfoData(deviceInfoData);
                service.setMessageService(messageService);

                ChannelTemplate channelTemplate = channelTemplateData.findById(alertConfig.getMessageTemplateId());
                Long channelConfigId = channelTemplate.getChannelConfigId();

                Message message = Message.builder()
                        .content(channelTemplate.getContent())
                        .alertConfigId(alertConfig.getId())
                        .build();

                if(channelConfigId!=null) {
                    ChannelConfig channelConfig = channelConfigData.findById(channelTemplate.getChannelConfigId());
                    Channel channel = channelData.findById(channelConfig.getChannelId());
                    message.setChannel(channel.getCode());
                    message.setChannelId(channel.getId());
                    message.setChannelConfig(channelConfig.getParam());
                }

                service.setMessage(message);
                alertServices.add(service);
            }
            alertAction.setServices(alertServices);
            return alertAction;
        }
        return null;
    }

    private <T> T parse(String config, Class<T> cls) { //将string转为对象
        return JsonUtils.parseObject(config, cls);
    }
}
