package com.bgsoftware.superiorskyblock.world.schematic;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.SchematicManager;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParseException;
import com.bgsoftware.superiorskyblock.api.schematic.parser.SchematicParser;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.errors.ManagerLoadException;
import com.bgsoftware.superiorskyblock.core.io.Files;
import com.bgsoftware.superiorskyblock.core.io.Resources;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.tag.*;
import com.bgsoftware.superiorskyblock.world.schematic.container.SchematicsContainer;
import com.bgsoftware.superiorskyblock.world.schematic.impl.SuperiorSchematic;
import com.bgsoftware.superiorskyblock.world.schematic.parser.DefaultSchematicParser;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.Vector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SchematicsManagerImpl extends Manager implements SchematicManager {

    private final SchematicsContainer schematicsContainer;

    public SchematicsManagerImpl(SuperiorSkyblockPlugin plugin, SchematicsContainer schematicsContainer) {
        super(plugin);
        this.schematicsContainer = schematicsContainer;
    }

    public void loadData() throws ManagerLoadException {
        File schematicsFolder = new File(plugin.getDataFolder(), "schematics");

        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
            Resources.saveResource("schematics/desert.schematic");
            Resources.saveResource("schematics/desert_nether.schematic", "schematics/normal_nether.schematic");
            Resources.saveResource("schematics/desert_the_end.schematic", "schematics/normal_the_end.schematic");
            Resources.saveResource("schematics/mycel.schematic");
            Resources.saveResource("schematics/mycel_nether.schematic", "schematics/normal_nether.schematic");
            Resources.saveResource("schematics/mycel_the_end.schematic", "schematics/normal_the_end.schematic");
            Resources.saveResource("schematics/normal.schematic");
            Resources.saveResource("schematics/normal_nether.schematic");
            Resources.saveResource("schematics/normal_the_end.schematic");
        }

        loadDefaultSchematicParsers();

        for (File schemFile : Files.listFolderFiles(schematicsFolder, false)) {
            String schemName = Files.getFileName(schemFile).toLowerCase(Locale.ENGLISH);
            Schematic schematic = loadFromFile(schemName, schemFile);
            if (schematic != null) {
                this.schematicsContainer.addSchematic(schematic);
            }
        }

        if (this.schematicsContainer.getSchematicNames().isEmpty()) {
            throw new ManagerLoadException("&cThere were no valid schematics.",
                    ManagerLoadException.ErrorLevel.SERVER_SHUTDOWN);
        }

        System.gc();
    }

    private void loadDefaultSchematicParsers() {
        if (Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) {
            try {
                Class.forName("com.boydti.fawe.object.schematic.Schematic");
                SchematicParser schematicParser = (SchematicParser) Class.forName("com.bgsoftware.superiorskyblock.world.schematic.parser.FAWESchematicParser").newInstance();
                this.schematicsContainer.addSchematicParser(schematicParser);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public Schematic getSchematic(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        return this.schematicsContainer.getSchematic(name);
    }

    @Override
    public List<String> getSchematics() {
        return this.schematicsContainer.getSchematicNames();
    }

    @Override
    public void registerSchematicParser(SchematicParser schematicParser) {
        Preconditions.checkNotNull(schematicParser, "schematicParser parameter cannot be null.");
        this.schematicsContainer.addSchematicParser(schematicParser);
    }

    @Override
    public List<SchematicParser> getSchematicParsers() {
        return this.schematicsContainer.getSchematicParsers();
    }

    @Override
    public void saveSchematic(SuperiorPlayer superiorPlayer, String schematicName) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(superiorPlayer.getLocation(), "superiorPlayer must be online.");
        Preconditions.checkNotNull(schematicName, "schematicName parameter cannot be null.");
        Preconditions.checkNotNull(schematicName, "schematicName parameter cannot be null.");

        Location pos1 = superiorPlayer.getSchematicPos1().parse();
        Location pos2 = superiorPlayer.getSchematicPos2().parse();
        Location spawn = superiorPlayer.getSchematicSpawnLocation().clone();

        Location min = new Location(pos1.getWorld(),
                Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ())
        );
        Location offset = superiorPlayer.getLocation().clone().subtract(min.clone().add(0, 1, 0));
        Location spawnSubtract = spawn.clone().subtract(min.clone()).subtract(offset);

        Log.debug(Debug.SAVE_SCHEMATIC, superiorPlayer.getSchematicSpawnLocation(), spawnSubtract, offset);

        saveSchematic(superiorPlayer.getSchematicPos1().parse(), superiorPlayer.getSchematicPos2().parse(), offset, spawnSubtract, schematicName, () ->
                Message.SCHEMATIC_SAVED.send(superiorPlayer));

        superiorPlayer.setSchematicPos1(null);
        superiorPlayer.setSchematicPos2(null);
        superiorPlayer.setSchematicSpawnLocation(null);
    }

    @Override
    public void saveSchematic(Location pos1, Location pos2, Location offset, Location spawn, String schematicName) {
        Preconditions.checkNotNull(pos1, "pos1 parameter cannot be null.");
        Preconditions.checkNotNull(pos2, "pos2 parameter cannot be null.");
        Preconditions.checkNotNull(schematicName, "schematicName parameter cannot be null.");
        saveSchematic(pos1, pos2, offset, spawn, schematicName, null);
    }

    @Override
    public void saveSchematic(Location pos1, Location pos2, Location offset, Location spawn, String schematicName, @Nullable Runnable runnable) {
        Preconditions.checkNotNull(pos1, "pos1 parameter cannot be null.");
        Preconditions.checkNotNull(pos2, "pos2 parameter cannot be null.");
        Preconditions.checkNotNull(schematicName, "schematicName parameter cannot be null.");

        Log.debug(Debug.SAVE_SCHEMATIC, pos1, pos2, offset.getX(), offset.getY(), offset.getZ(), offset.getYaw(), offset.getPitch(), schematicName);

        World world = pos1.getWorld();
        Location min = new Location(world,
                Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ())
        );
        Location max = new Location(world,
                Math.max(pos1.getX(), pos2.getX()),
                Math.max(pos1.getY(), pos2.getY()),
                Math.max(pos1.getZ(), pos2.getZ())
        );

        int xSize = max.getBlockX() - min.getBlockX();
        int ySize = max.getBlockY() - min.getBlockY();
        int zSize = max.getBlockZ() - min.getBlockZ();

        List<Tag<?>> blocks = new ArrayList<>();
        List<Tag<?>> entities = new ArrayList<>();

        for (int x = 0; x <= xSize; x++) {
            for (int z = 0; z <= zSize; z++) {
                for (int y = 0; y <= ySize; y++) {
                    Block block = world.getBlockAt(x + min.getBlockX(), y + min.getBlockY(), z + min.getBlockZ());
                    Material blockType = block.getType();
                    Location blockLocation = block.getLocation();

                    if (blockType != Material.AIR) {
                        CompoundTag tileEntity = plugin.getNMSWorld().readTileEntity(blockLocation);
                        if (tileEntity != null && block.getState() instanceof InventoryHolder)
                            tileEntity.setString("inventoryType", ((InventoryHolder) block.getState()).getInventory().getType().name());

                        //noinspection deprecation
                        blocks.add(new SchematicBuilder()
                                .withBlockOffset(SBlockOffset.fromOffsets(x, y, z))
                                .withBlockType(blockLocation, blockType, block.getData())
                                .withStates(plugin.getNMSWorld().readBlockStates(blockLocation))
                                .withLightLevels(plugin.getNMSWorld().getLightLevels(blockLocation))
                                .withTileEntity(tileEntity)
                                .build()
                        );
                    }
                }
            }
        }

        for (Entity livingEntity : getEntities(min, max)) {
            entities.add(new SchematicBuilder().applyEntity(livingEntity, min).build());
        }

        Map<String, Tag<?>> compoundValue = new HashMap<>();
        compoundValue.put("xSize", new IntTag(xSize));
        compoundValue.put("ySize", new IntTag(ySize));
        compoundValue.put("zSize", new IntTag(zSize));
        compoundValue.put("blocks", new ListTag(CompoundTag.class, blocks));
        compoundValue.put("entities", new ListTag(CompoundTag.class, entities));
        compoundValue.put("offsetX", new IntTag(offset.getBlockX()));
        compoundValue.put("offsetY", new IntTag(offset.getBlockY()));
        compoundValue.put("offsetZ", new IntTag(offset.getBlockZ()));
        compoundValue.put("spawnX", new DoubleTag(spawn.getX()));
        compoundValue.put("spawnY", new DoubleTag(spawn.getY()));
        compoundValue.put("spawnZ", new DoubleTag(spawn.getZ()));
        compoundValue.put("yaw", new FloatTag(spawn.getYaw()));
        compoundValue.put("pitch", new FloatTag(spawn.getPitch()));
        compoundValue.put("version", new StringTag(ServerVersion.getBukkitVersion()));
        if (!ServerVersion.isLegacy())
            compoundValue.put("minecraftDataVersion", new IntTag(plugin.getNMSAlgorithms().getDataVersion()));

        CompoundTag schematicTag = new CompoundTag(compoundValue);
        SuperiorSchematic schematic = new SuperiorSchematic(schematicName, schematicTag);
        this.schematicsContainer.addSchematic(schematic);
        saveIntoFile(schematicName, schematicTag);

        if (runnable != null)
            runnable.run();
    }

    public String getDefaultSchematic(Dimension dimension) {
        String suffix = "_" + dimension.getName().toLowerCase(Locale.ENGLISH);
        for (String schematicName : this.schematicsContainer.getSchematicNames()) {
            if (getSchematic(schematicName + suffix) != null)
                return schematicName;
        }

        return "";
    }

    private Schematic parseSchematic(File file, String schemName, SchematicParser schematicParser,
                                     Consumer<SchematicParseException> onSchematicParseError) {
        try (DataInputStream reader = new DataInputStream(new GZIPInputStream(java.nio.file.Files.newInputStream(file.toPath())))) {
            return schematicParser.parseSchematic(reader, schemName);
        } catch (SchematicParseException error) {
            onSchematicParseError.accept(error);
        } catch (Exception error) {
            Log.entering("SchematicsManagerImpl", "parseSchematic", "ENTER", file.getName(), schemName);
            Log.error(error, "An unexpected error occurred while loading schematic:");
        }

        return null;
    }

    private Schematic loadFromFile(String schemName, File file) {
        Schematic schematic = null;
        SchematicParser usedParser = null;

        for (SchematicParser schematicParser : this.schematicsContainer.getSchematicParsers()) {
            schematic = parseSchematic(file, schemName, schematicParser, error -> {
            });
            if (schematic != null) {
                usedParser = schematicParser;
                break;
            }
        }

        if (schematic == null) {
            schematic = parseSchematic(file, schemName, DefaultSchematicParser.getInstance(), error ->
                    Log.warn("Schematic ", file.getName(), " is not a valid schematic, ignoring...")
            );
            if (schematic != null)
                usedParser = DefaultSchematicParser.getInstance();
        }

        if (schematic != null && usedParser != null) {
            Log.info("Successfully loaded schematic ", file.getName(), " (", usedParser.getClass().getSimpleName(), ")");
        }

        return schematic;
    }

    private void saveIntoFile(String name, CompoundTag schematicTag) {
        try {
            File file = new File(plugin.getDataFolder(), "schematics/" + name + ".schematic");

            if (file.exists())
                file.delete();

            file.getParentFile().mkdirs();
            file.createNewFile();

            try (DataOutputStream writer = new DataOutputStream(new GZIPOutputStream(java.nio.file.Files.newOutputStream(file.toPath())))) {
                schematicTag.write(writer);
            }
        } catch (IOException error) {
            Log.entering("SchematicsManagerImpl", "saveIntoFile", "ENTER", name);
            Log.error(error, "An unexpected error occurred while saving schematic into file:");
        }
    }

    private List<Entity> getEntities(Location min, Location max) {
        List<Entity> livingEntities = new LinkedList<>();

        Chunk minChunk = min.getChunk();
        Chunk maxChunk = max.getChunk();
        for (int x = minChunk.getX(); x <= maxChunk.getX(); x++) {
            for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++) {
                Chunk currentChunk = min.getWorld().getChunkAt(x, z);
                for (Entity entity : currentChunk.getEntities()) {
                    if (!(entity instanceof Player) && betweenLocations(entity.getLocation(), min, max))
                        livingEntities.add(entity);
                }
            }
        }

        return livingEntities;
    }

    private boolean betweenLocations(Location location, Location min, Location max) {
        return location.getBlockX() >= min.getBlockX() && location.getBlockX() <= max.getBlockX() &&
                location.getBlockY() >= min.getBlockY() && location.getBlockY() <= max.getBlockY() &&
                location.getBlockZ() >= min.getBlockZ() && location.getBlockZ() <= max.getBlockZ();
    }

}
