package cc.iotkit.ruleengine.EmqxTest.handler;

import org.eclipse.paho.client.mqttv3.MqttException;

public interface IMqtt {
    void subscribe(String topic) throws MqttException;
    void publish(String msg);
}
