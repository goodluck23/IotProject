package cc.iotkit.ruleengine.test.rule;

import cc.iotkit.ruleengine.listener.Listener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trule {
    private String id;

    private String name;

    private List<Listener<?>> listener;

}
