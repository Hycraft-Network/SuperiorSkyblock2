package com.bgsoftware.superiorskyblock.player;

import java.util.EnumMap;

public final class PlayerAttributes {

    private final EnumMap<Field, Object> fieldValues = new EnumMap<>(Field.class);

    public PlayerAttributes(){

    }

    public PlayerAttributes setValue(Field field, Object value){
        fieldValues.put(field, value);
        return this;
    }

    public <T> T getValue(Field field){
        Object value = fieldValues.get(field);
        // noinspection all
        return (T) value;
    }

    public <T> T getValue(Field field, Class<T> type){
        Object value = fieldValues.get(field);
        // noinspection all
        return type.cast(value);
    }

    public enum Field {

        UUID,
        ISLAND_LEADER,
        LAST_USED_NAME,
        LAST_USED_SKIN,
        ISLAND_ROLE,
        DISBANDS,
        LAST_TIME_UPDATED,
        COMPLETED_MISSIONS,
        TOGGLED_PANEL,
        ISLAND_FLY,
        BORDER_COLOR,
        LANGUAGE,
        TOGGLED_BORDER

    }

}
