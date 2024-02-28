package cc.iotkit.ruleengine.test.action.device;

import lombok.Data;

import java.util.List;

@Data
public class TdeviceService {

    private String device;
    private String identifier;
    private String type;
    List<InputData> inputDataLis;

    //identifier":"pressure","value":"21"

    @Data
    public static class InputData{
        private String identifier;
        private String value;

    }
}
