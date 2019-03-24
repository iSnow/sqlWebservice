package de.isnow.sqlws.model.viewModel;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import javax.validation.constraints.NotNull;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)

@Data
@EqualsAndHashCode(of={"name", "fullName"})
public class VmObject implements Comparable<VmObject>{

    protected String name;

    protected String fullName;

    protected boolean visible = true;

    protected int position = 0;

    @Override
    public int compareTo(@NotNull VmObject o) {
        if ((null != fullName) && (null != o.fullName))
            return fullName.compareTo(o.fullName);
        if ((null != name) && (null != o.name))
            return name.compareTo(o.name);
        return 0;
    }
}
