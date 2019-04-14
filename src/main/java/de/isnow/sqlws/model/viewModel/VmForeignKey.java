package de.isnow.sqlws.model.viewModel;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.isnow.sqlws.model.WsRelation;
import de.isnow.sqlws.model.WsTable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.security.KeyPair;
import java.util.*;

@Data
@EqualsAndHashCode(of={"childTableKey", "childTableKey"})
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class VmForeignKey {

    private String childTableKey;

    private String parentTableKey;

    private List<KeyPair> primaryForeignKeyRelationships = new ArrayList<>();

    public static VmForeignKey fromWsForeignKey(WsTable.WsForeignKey fk) {
        VmForeignKey that = new VmForeignKey();
        that.childTableKey = fk.getChildTableKey();
        that.parentTableKey = fk.getParentTableKey();
        that.transformPrimaryForeignKeyRelationships(fk.getPrimaryForeignKeyRelationships());
        return that;
    }

    public void setPrimaryForeignKeyRelationships(Collection<KeyPair> rels) {
        primaryForeignKeyRelationships = new ArrayList<>();
        rels.forEach((rel) -> {
            primaryForeignKeyRelationships.add(rel);
        });
    }

    public void transformPrimaryForeignKeyRelationships(Collection<WsRelation> rels) {
        List<KeyPair> kps = new ArrayList<>();
        rels.forEach((rel) -> {
            KeyPair kp = new KeyPair(
                rel.getFkColumnName(),
                rel.getPkColumnName(),
                null
            );
            kps.add(kp);
        });
        setPrimaryForeignKeyRelationships(kps);
    }

    public Map<String, String> getChildKeyValues() {
        Map<String, String> retVal = new HashMap<>();
        primaryForeignKeyRelationships.forEach((rel) -> {
            String val = (rel.value != null) ? rel.value.toString() : null;
            retVal.put(rel.fk, val);
        });
        return retVal;
    }


    public void setChildKeyValues(Map<String, String> vals) {
        primaryForeignKeyRelationships.forEach((rel) -> {
           if (vals.get(rel.getFk()) != null) {
               rel.value = vals.get(rel.getFk());
           }
        });
    }

    public void setKeyValues(VmTable parentTable) {
        Map<String, String> vals = parentTable.getParentKeyValues();
        primaryForeignKeyRelationships.forEach((rel) -> {
            if (vals.get(rel.getPk()) != null) {
                rel.value = vals.get(rel.getFk());
            }
        });
    }

    @Data
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            setterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class KeyPair {
        public String fk;
        public String pk;
        public Object value;

        public KeyPair() {}

        public KeyPair(String fk, String pk, Object value) {
            this();
            this.fk = fk;
            this.pk = pk;
            this.value = value;
        }
    }
}
