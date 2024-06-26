package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import javax.annotation.Nullable;
import java.util.Objects;

public class SBlockPosition implements BlockPosition {

    @Nullable
    private final WorldsRegistry.SyncedWorld world;
    private final int x;
    private final int y;
    private final int z;

    public SBlockPosition(LazyWorldLocation location) {
        this(location.getWorldName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public SBlockPosition(Location location) {
        this(LazyWorldLocation.getWorldName(location), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public SBlockPosition(String world, int x, int y, int z) {
        this.world = world == null ? null : WorldsRegistry.getWorld(world);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    @Deprecated
    public String getWorldName() {
        return world == null ? null : world.getWorldName();
    }

    @Override
    @Deprecated
    public World getWorld() {
        return world == null ? null : world.getBukkitWorld();
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public BlockPosition offset(int x, int y, int z) {
        return new SBlockPosition(this.world == null ? null : this.world.getWorldName(), this.x + x, this.y + y, this.z + z);
    }

    @Override
    @Deprecated
    public Block getBlock() {
        return parse().getBlock();
    }

    @Override
    @Deprecated
    public Location parse(World world) {
        return new Location(world, getX(), getY(), getZ());
    }

    @Override
    @Deprecated
    public Location parse() {
        World world = getWorld();
        return world == null ? new LazyWorldLocation(this.world == null ? null : this.world.getWorldName(), getX(), getY(), getZ(), 0, 0) :
                new Location(world, getX(), getY(), getZ());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SBlockPosition that = (SBlockPosition) o;
        return x == that.x && y == that.y && z == that.z && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, world);
    }

    @Override
    public String toString() {
        String worldName = world == null ? "null" : world.getWorldName();
        return worldName + ", " + x + ", " + y + ", " + z;
    }

}
