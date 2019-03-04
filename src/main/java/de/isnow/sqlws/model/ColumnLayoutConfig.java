package de.isnow.sqlws.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(of={"type", "columnId"}, callSuper = true)
@ToString(of={"columnId"}, callSuper = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ColumnLayoutConfig extends LayoutConfig {

    private String columnId;

    private String columnName;

    private String type;
}
