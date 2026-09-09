package pl.madzierski.daniel.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserQuery {
    private String id;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        UserQuery userQuery = (UserQuery) o;
        return Objects.equals(id, userQuery.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
