package de.isnow.sqlws.model.viewModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class VmTable extends VmObject {

    @JsonProperty("columns")
    Set<VmColumn> columns = new LinkedHashSet<>();

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
}
