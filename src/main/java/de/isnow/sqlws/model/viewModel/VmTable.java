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
    Set<VmForeignKey> relations = new HashSet<>();

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
        }
        return vmt;
    }

    public Set<VmForeignKey> getMatchingFKs(VmTable childTable) {
        Set<VmForeignKey> retVal = new HashSet<>();
        Set<VmForeignKey> fkCols = childTable.relations;
        fkCols.forEach((c) -> {
            if (c.getParentTableKey().equals(getFullName())) {
               retVal.add(c);
            }
        });
        return retVal;
    }

    public void addForeignKey(VmForeignKey fk) {
        if (null == fk)
            return;
        relations.add(fk);
    }

    public void setForeignKeys(Collection<WsTable.WsForeignKey> fks) {
        if (null == fks)
            return;
        fks.forEach((fk) -> relations.add(VmForeignKey.fromWsForeignKey(fk)));
    }

    public void setForeignKeys(WsTable table) {
        if (null != table) {
            Set<WsTable.WsForeignKey> wsfks = table.parseForeignKeys();
            if (null != wsfks) {
                setForeignKeys(wsfks);
            }
        }
    }

    public void setForeignKeys(Set<VmForeignKey> keys) {
        this.relations = keys;
    }

}
