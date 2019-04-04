package de.isnow.sqlws.model.viewModel;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.isnow.sqlws.model.WsColumn;
import de.isnow.sqlws.model.WsRelation;
import de.isnow.sqlws.model.WsTable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@Data
@EqualsAndHashCode(of={"childTableKey", "primaryForeignKeyRelationships"})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class VmForeignKey {

    String childTableKey;

    String parentTableKey;

    List<Map<String, Object>> primaryForeignKeyRelationships;

    public static VmForeignKey fromWsForeignKey(WsTable.WsForeignKey fk) {
        VmForeignKey that = new VmForeignKey();
        that.childTableKey = fk.getChildTableKey();
        that.parentTableKey = fk.getParentTableKey();
        that.setPrimaryForeignKeyRelationships(fk.getPrimaryForeignKeyRelationships());
        return that;
    }

    public void setPrimaryForeignKeyRelationships (Collection<WsRelation> rels) {
        rels.forEach((rel) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("fk", rel.getFkColumnName());
            m.put("pk", rel.getPkColumnName());
        });
    }
/*
    public static VmForeignKey fromColumnMap(String childTableKey, Map<WsColumn, WsColumn> colMap) {
        VmForeignKey fk = new VmForeignKey();
        fk.childTableKey = childTableKey;
        fk.primaryForeignKeyRelationships = new ArrayList<>();
        colMap.keySet().forEach((c) -> {
            Map<String, String> kv = new LinkedHashMap<>();
            kv.put("pk", c.getFullName());
            kv.put("fk", colMap.get(c).getFullName());
            fk.primaryForeignKeyRelationships.add(kv);
        });

        return fk;
    }*/
}
