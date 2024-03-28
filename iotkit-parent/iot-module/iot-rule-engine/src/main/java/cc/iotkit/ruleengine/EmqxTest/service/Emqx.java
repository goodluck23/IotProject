package cc.iotkit.ruleengine.EmqxTest.service;



import cc.iotkit.ruleengine.EmqxTest.handler.IMqtt;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;

public class Emqx implements IMqtt {

    private MqttClient mqttClient;

    public Emqx(MqttClient mqttClient){

        this.mqttClient=mqttClient;
    }
    @Override
    public void subscribe(String topic) throws MqttException {
        mqttClient.subscribe(topic);

    }

    @Override
    public void publish(String msg) {

    }


}
