package de.isnow.sqlws.model.viewModel;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.isnow.sqlws.model.WsColumn;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
public class VmColumn extends VmObject {

    @JsonProperty("referencedBy")
    private List<String> referencedBy;

    @JsonProperty("references")
    private String references;

    public static VmColumn fromWsColumn(WsColumn wsc) {
        VmColumn col = new VmColumn();
        col.name = wsc.getName();
        col.fullName = wsc.getFullName();
        col.references = wsc.getReferences();
        col.visible = !(wsc.isForeignKey());
        col.referencedBy = new ArrayList<>();
        wsc.getReferencedBy().forEach((c) -> {
            col.referencedBy.add("+"+c);
        });
        return col;
    }
}
