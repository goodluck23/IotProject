package cc.iotkit.ruleengine.config;




import cc.iotkit.ruleengine.EmqxTest.handler.IMqtt;
import cc.iotkit.ruleengine.EmqxTest.service.Emqx;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Configurable;


import org.springframework.context.annotation.Bean;

@Configurable
public class EmqxConfig {



    private String host="tcp://192.168.0.129:1883";
    private String clientId="IotTest";
    //@Bean
//    public IMqtt getMqttClient(MqttCallback handler){
//             MqttClient client;
//        try {
//            client = new MqttClient(host,clientId, new MemoryPersistence());
//          //  client.setCallback(handler);
//            MqttConnectOptions connOpts = new MqttConnectOptions();
//            connOpts.setUserName("emqx_test");
//            connOpts.setPassword("emqx_test_password".toCharArray());
//            // 保留会话
//            connOpts.setCleanSession(true);
//
//            // 设置回调
//            client.setCallback(handler);
//
//            // 建立连接
//            System.out.println("Connecting to broker: " + handler);
//            client.connect(connOpts);
//            client.subscribe("test");
//            return new Emqx(client);
//
//
//        }catch (MqttException me){
//
//        }
//        return null;
//    }


}
