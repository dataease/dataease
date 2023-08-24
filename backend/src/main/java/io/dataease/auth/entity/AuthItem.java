package io.dataease.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuthItem implements Serializable {

    private static final long serialVersionUID = 7909546616315767531L;

    private String authSource;

    private Integer level;




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthItem authItem = (AuthItem) o;
        return Objects.equals(authSource, authItem.authSource) &&
                Objects.equals(level, authItem.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authSource, level);
    }


}
