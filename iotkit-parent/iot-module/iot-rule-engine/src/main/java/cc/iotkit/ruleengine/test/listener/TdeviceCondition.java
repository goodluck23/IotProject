package cc.iotkit.ruleengine.test.listener;


import lombok.Data;

import java.util.List;
@Data
public class TdeviceCondition {

    private String identifier;
    private String type;
    private String device;
    private List<Parameter> parameters;

    @Data
    public static class Parameter {
        private String identifier;
        private String comparator;
        private String value;
    }
}
