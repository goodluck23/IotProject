package cc.iotkit.ruleengine.test.filter;

import cc.iotkit.common.thing.ThingModelMessage;

import cc.iotkit.ruleengine.filter.Filter;
import lombok.Data;

import java.util.List;
@Data
public class TdeviceFilter implements Filter<TdeviceCondition> {
    public static final String TYPE = "device";
    private String type;
    private List<TdeviceCondition> conditions;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void init() {

    }


    @Override
    public boolean execute(ThingModelMessage msg) {
        return false;
    }


}
