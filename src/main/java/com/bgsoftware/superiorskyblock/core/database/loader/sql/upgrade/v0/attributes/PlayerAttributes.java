package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes;

public class PlayerAttributes extends AttributesRegistry<PlayerAttributes.Field> {

    public PlayerAttributes() {
        super(Field.class);
    }

    @Override
    public PlayerAttributes setValue(Field field, Object value) {
        return (PlayerAttributes) super.setValue(field, value);
    }

    public enum Field {

        UUID,
        ISLAND_LEADER,
        LAST_USED_NAME,
        LAST_USED_SKIN,
        ISLAND_ROLE,
        DISBANDS,
        LAST_TIME_UPDATED,
        LAST_ISLAND_CREATED,
        COMPLETED_MISSIONS,
        TOGGLED_PANEL,
        ISLAND_FLY,
        BORDER_COLOR,
        LANGUAGE,
        TOGGLED_BORDER

    }

}
