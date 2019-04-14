package de.isnow.sqlws.model.viewModel;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.isnow.sqlws.model.WsColumn;
import de.isnow.sqlws.model.WsTable;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
@Getter
public class VmTable extends VmObject {

    @JsonProperty("columns")
    Set<VmColumn> columns = new LinkedHashSet<>();

    @JsonProperty("relations")
    Map<String, VmForeignKey> relations = new HashMap<>();

    @JsonProperty("children")
    Set<VmTable> children = new LinkedHashSet<>();

    public void setColumns(Collection<VmColumn> cols) {
        columns = new LinkedHashSet<>(cols
                .stream()
                .sorted((a, b) -> {return a.getPosition() - b.getPosition();})
                .collect(Collectors.toList()));
    }

    public void addColumn(VmColumn col) {
        columns.add(col);
        setColumns(columns);
    }

    public VmColumn getColumnByFullName(String fullName) {
        for (VmColumn c : columns) {
            if (c.fullName.equals(fullName))
                return c;
        }
        return null;
    }

    public VmTable getChildTableByFullName(String fullName) {
        for (VmTable c : children) {
            if (c.fullName.equals(fullName))
                return c;
        }
        return null;
    }

    public static VmTable fromWsTable(
            WsTable wst,
            Collection<String> columnNamesToShow,
            int recurseChildTablesDeep,
            boolean copyForeignKeys) {
        if (null == wst)
            return null;
        VmTable vmt = new VmTable();
        vmt.name = wst.getName();
        vmt.fullName = wst.getFullName();
        if (copyForeignKeys)
            vmt.setForeignKeys(wst);
        Set<WsColumn> colsToShow = wst.getColumnsToShow(columnNamesToShow);
        Set<VmColumn> cols = new LinkedHashSet<>();
        int cnt = 0;
        if (null != wst.getColumns()) {
            for (WsColumn c : wst.getColumns()) {
                if (colsToShow.contains(c)) {
                    VmColumn col = VmColumn.fromWsColumn(c);
                    if (!c.isForeignKey()) {
                        col.setPosition(cnt++);
                    }
                    cols.add(col);
                }
            }
        };
        vmt.setColumns(cols);
        if (recurseChildTablesDeep > 0) {
            List<WsTable> children = wst.getChildTables();
            children.forEach((t) -> {
                VmTable ct = fromWsTable(t, null, (recurseChildTablesDeep -1),copyForeignKeys);
                vmt.children.add(ct);
            });
            vmt.setForeignKeys(getForeignKeys(wst, vmt));
        }
        return vmt;
    }

    public void setRowValues(Map<String, Object> row) {
        if (row.size() == 0)
            return;
        getColumns().forEach((c) -> c.setValue(row.get(c.getName())));
        this.relations.values().forEach((c) -> {
            if (c.getParentTableKey().equals(this.fullName)) {
                c.getPrimaryForeignKeyRelationships().forEach((rel) -> {
                    VmColumn col = getColumnByFullName(rel.pk);
                    rel.value = col.getValue().toString();
                });
            } else if (c.getChildTableKey().equals(this.fullName)) {
                c.getPrimaryForeignKeyRelationships().forEach((rel) -> {
                    VmColumn col = getColumnByFullName(rel.fk);
                    rel.value = col.getValue().toString();
                });
            }
        });
    }

    public static Set<VmForeignKey> getForeignKeys(WsTable table, VmTable tableToReturn) {
        List<WsTable> cts = table.getChildTables();
        Set<VmForeignKey> newFks = new HashSet<>();
        cts.forEach((t) -> {
            VmTable childTable = tableToReturn.getChildTableByFullName(t.getFullName());
            VmForeignKey fk = tableToReturn.getMatchingFKs(childTable);
            fk.getPrimaryForeignKeyRelationships().forEach((m) -> {
                VmColumn c = tableToReturn.getColumnByFullName((String)m.pk);
                if (null != c)
                    m.value = c.getValue();
            });
            childTable.addForeignKey(fk);
            newFks.add(fk);
        });
        return newFks;
    }

    public VmForeignKey getMatchingFKs(VmTable childTable) {
        return relations.get(childTable.fullName);
    }

    public void addForeignKey(VmForeignKey fk) {
        if (null == fk)
            return;
        String name = fk.getChildTableKey();
        if (name.equals(this.fullName)) {
            name = fk.getParentTableKey();
        }
        relations.put(name, fk);
    }

    public void addForeignKey(WsTable.WsForeignKey fk) {
        if (null == fk)
            return;
        addForeignKey(VmForeignKey.fromWsForeignKey(fk));
    }

    public void setForeignKeys(Set<VmForeignKey> keys) {
        if (null == keys)
            return;
        relations = new HashMap<>();
        keys.forEach(this::addForeignKey);
        this.getChildren().forEach((c) -> {
            String fName = c.fullName;
            Set<VmForeignKey> rels = keys
                .stream()
                .filter((k) -> k.getChildTableKey().equals(fName))
                .collect(Collectors.toSet());
            c.setForeignKeys(rels);
        });
    }

    public void setForeignKeys(Collection<WsTable.WsForeignKey> fks) {
        if (null == fks)
            return;
        relations = new HashMap<>();
        fks.forEach(this::addForeignKey);
    }

    public void setForeignKeys(WsTable table) {
        if (null != table) {
            Set<WsTable.WsForeignKey> wsfks = table.parseForeignKeys();
            setForeignKeys(wsfks);
        }
    }

    public void setForeignKeys(VmTable parentTable) {
        if (null != parentTable) {
            relations.values().forEach((rel) -> {
                rel.setChildKeyValues(parentTable.getParentKeyValues());
            });
        }
    }

    public Map<String, String> getChildKeyValues() {
        Map<String, String> retVal = new HashMap<>();
        relations.values().forEach((rel) -> {
            if (rel.getChildTableKey().equals(this.fullName)) {
                Map<String, String> iVal = rel.getChildKeyValues();
                retVal.putAll(iVal);
            }
        });
        return retVal;
    }

    public Map<String, String> getParentKeyValues() {
        Map<String, String> retVal = new HashMap<>();
        relations.values().forEach((rel) -> {
            if (rel.getParentTableKey().equals(this.fullName)) {
                Map<String, String> iVal = rel.getChildKeyValues();
                retVal.putAll(iVal);
            }
        });
        return retVal;
    }

}
