package pl.madzierski.daniel.user.model;

import java.util.UUID;

public record AuthenticatedUser(UUID id) {

    public UserQuery toUserQuery() {
        return new UserQuery(id);
    }
}
