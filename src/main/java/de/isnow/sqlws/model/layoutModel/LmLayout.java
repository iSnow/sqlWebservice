package de.isnow.sqlws.model.layoutModel;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LmLayout extends LmObject {
    public LmLayout() {
        type = "layout";
    }
}
