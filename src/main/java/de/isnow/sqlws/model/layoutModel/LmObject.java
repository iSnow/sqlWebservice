package de.isnow.sqlws.model.layoutModel;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LmObject {

    String type;

    @JsonProperty("orientation")
    String Orientation;

    @JsonProperty("children")
    List<LmObject> children = new ArrayList<>();

    @JsonProperty("visible")
    boolean visible = true;

    @JsonProperty("id")
    String containerId;

    String columnName;

    String columnId;

    public void addChild(LmObject child) {
        if (null != child)
            children.add(child);
    }

    public void addChildren(Collection<LmObject> children) {
        children.forEach(this::addChild);
    }

    public void setContainerId (int id) {
        containerId = type+"-"+id;
    }

    public enum Orientation {
        HORZONTAL,
        VERTICAL,
        NONE

    }
}
