package de.isnow.sqlws.model.viewModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.isnow.sqlws.model.WsColumn;
import de.isnow.sqlws.model.WsTable;
import lombok.Data;
import lombok.Getter;
import schemacrawler.schema.Column;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.ForeignKeyColumnReference;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class VmTable extends VmObject {

    @JsonProperty("columns")
    Set<VmColumn> columns = new LinkedHashSet<>();

    @JsonProperty("primaryForeignKeys")
    List<VmForeignKey> fkList = new ArrayList<>();

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

    public static VmTable fromWsTable(WsTable wst) {
        if (null == wst)
            return null;
        VmTable vmt = new VmTable();
        Set<VmColumn> cols = new HashSet<>();
        int cnt = 0;
        if (null != wst.getColumns()) {
            for (WsColumn c : wst.getColumns()) {
                VmColumn col = new VmColumn();
                col.setName(c.getName());
                col.setFullName(c.getFullName());
                //if ((c.isPrimaryKey()) || (c.isForeignKey())) {
                if (c.isForeignKey()) {
                    col.setVisible(false);
                } else {
                    col.setPosition(cnt++);
                }
                cols.add(col);
            }
        };
        vmt.setColumns(cols);
        return vmt;
    }

    public void setForeignKeys(List<WsTable.WsForeignKey> fks) {
        if (null == fks)
            return;
        fks.forEach((fk) -> fkList.add(VmForeignKey.fromWsForeignKey(fk)));
    }

    public void setForeignKeys(WsTable table) {
        if (null != table) {
           List<WsTable.WsForeignKey> wsfks = table.parseForeignKeys();
           if (null != wsfks) {
               setForeignKeys(wsfks);
           }
        }
    }

}
