/*
 * +----------------------------------------------------------------------
 * | Copyright (c) 奇特物联 2021-2022 All rights reserved.
 * +----------------------------------------------------------------------
 * | Licensed 未经许可不能去掉「奇特物联」相关版权
 * +----------------------------------------------------------------------
 * | Author: xw2sy@163.com
 * +----------------------------------------------------------------------
 */
package cc.iotkit.ruleengine.config;

import cc.iotkit.common.thing.ThingModelMessage;
import cc.iotkit.mq.MqConsumer;
import cc.iotkit.ruleengine.EmqxTest.handler.IMqtt;
import cc.iotkit.ruleengine.EmqxTest.service.Emqx;
import cc.iotkit.ruleengine.handler.RuleDeviceConsumer;
import cc.iotkit.ruleengine.task.TaskManager;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleConfiguration {
    private String host="tcp://192.168.0.129:1883";
    private String clientId="IotTest";
    @Bean
    public RuleDeviceConsumer getConsumer(MqConsumer<ThingModelMessage> consumer) {
        return new RuleDeviceConsumer(consumer);
    }

    @Bean
    public TaskManager getTaskManager() {
        return new TaskManager();
    }
    @Bean
    public IMqtt getMqttClient(MqttCallback handler){
        MqttClient client;
        try {
            client = new MqttClient(host,clientId, new MemoryPersistence());
            //  client.setCallback(handler);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setUserName("emqx_test");
            connOpts.setPassword("emqx_test_password".toCharArray());
            // 保留会话
            connOpts.setCleanSession(true);

            // 设置回调
            client.setCallback(handler);

            // 建立连接
            System.out.println("Connecting to broker: " + handler);
            client.connect(connOpts);
            client.subscribe("test");
            return new Emqx(client);


        }catch (MqttException me){
            System.out.println(me);
        }
        return null;
    }
}
