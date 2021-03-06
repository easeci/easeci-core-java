package io.easeci.core.engine.pipeline;

import io.easeci.core.workspace.vars.Variable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stage {
    private String name;
    private int order;
    private List<Step> steps;
    private List<Variable> variables;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Stage stage = (Stage) o;
        return order == stage.order && Objects.equals(name, stage.name) && Objects.equals(steps, stage.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, order, steps);
    }
}
