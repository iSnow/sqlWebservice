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
    private String Orientation;

    @JsonProperty("children")
    private List<LmObject> children = new ArrayList<>();

    @JsonProperty("visible")
    private boolean visible = true;

    @JsonProperty("id")
    private String containerId;

    private String columnName;

    private String columnId;

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
