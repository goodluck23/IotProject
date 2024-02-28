package cc.iotkit.plugin.core.thing.actions;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NodeType {
    //网关设备
    GATEWAY(0),
    //网关子设备
    GATEWAY_DEVICE(1),
    //直连设备
    DEVICE(2);


    private final Integer nodeType;
}
