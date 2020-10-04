package pro.belbix.tim.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class Guilty {
    private Type type;
    private boolean needToReact;
    private String message = "";

    public Guilty(Type type, boolean needToReact) {
        this.type = type;
        this.needToReact = needToReact;
    }

    public enum Type {
        OPEN, CLOSE, POSITIVE, NO_REACT
    }
}
