package cc.iotkit.ruleengine.test.listener;

import cc.iotkit.common.thing.ThingModelMessage;
import cc.iotkit.ruleengine.listener.Listener;

import java.util.List;

public class TdeviceListener implements Listener<TdeviceCondition> {

    public static final String TYPE = "device";
    private String type;
    private List<TdeviceCondition> conditions;
    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public List<TdeviceCondition> getConditions() {
        return this.conditions=conditions;
    }

    @Override
    public boolean execute(ThingModelMessage msg) {
        return false;
    }
}
