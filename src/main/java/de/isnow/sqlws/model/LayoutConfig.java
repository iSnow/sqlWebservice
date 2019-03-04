package de.isnow.sqlws.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(of={"containerType", "cointainerId"})
@ToString(of={"containerType", "cointainerId", "children"})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LayoutConfig {

    private String containerType;

    private String cointainerId;

    List<LayoutConfig> children = new ArrayList<>();
}
