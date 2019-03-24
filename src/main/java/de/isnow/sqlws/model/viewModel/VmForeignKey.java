package de.isnow.sqlws.model.viewModel;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.isnow.sqlws.model.WsTable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(of={"childTableKey", "primaryForeignKeyRelationships"})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class VmForeignKey {

    String childTableKey;

    List<Map<String, String>> primaryForeignKeyRelationships;

    public static VmForeignKey fromWsForeignKey(WsTable.WsForeignKey fk) {
        VmForeignKey that = new VmForeignKey();
        that.childTableKey = fk.getChildTableKey();
        that.primaryForeignKeyRelationships = fk.getPrimaryForeignKeyRelationships();
        return that;
    }
}
