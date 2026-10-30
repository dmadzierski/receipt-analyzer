package pl.madzierski.daniel.user.model;

import java.util.UUID;

public record UserDto(UUID id, String userSub) { }
