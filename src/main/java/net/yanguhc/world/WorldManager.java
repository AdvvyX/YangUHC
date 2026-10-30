package net.yanguhc.world;

import net.yanguhc.YangUHC;
import net.yanguhc.util.TextUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;

public class WorldManager {

    private final YangUHC plugin;
    private World uhcWorld;
    private World lobbyWorld;
    private BorderController borderController;
    private ScatterManager scatterManager;

    public WorldManager(YangUHC plugin) {
        this.plugin = plugin;
    }

    public void init() {
        String worldName = plugin.getConfig().getString("world.name", "uhc_world");
        this.uhcWorld = Bukkit.getWorld(worldName);

        if (this.uhcWorld == null) {
            this.uhcWorld = createUHCWorld(worldName, World.Environment.NORMAL);
        }

        String lobbyName = plugin.getConfig().getString("world.lobby-name", "world");
        this.lobbyWorld = Bukkit.getWorld(lobbyName);
        if (this.lobbyWorld == null && !Bukkit.getWorlds().isEmpty()) {
            this.lobbyWorld = Bukkit.getWorlds().get(0);
        }

        setupGamerules(uhcWorld);

        this.borderController = new BorderController(plugin, uhcWorld);
        this.scatterManager = new ScatterManager(plugin, this);
    }

    public World createUHCWorld(String name, World.Environment environment) {
        plugin.getLogger().info("Generando o cargando mundo UHC: " + name);
        WorldCreator creator = new WorldCreator(name);
        creator.environment(environment);
        creator.type(WorldType.NORMAL);
        creator.generateStructures(true);

        long seed = plugin.getConfig().getLong("world.seed", 0);
        if (seed != 0) {
            creator.seed(seed);
        }

        World world = creator.createWorld();
        if (world != null) {
            setupGamerules(world);
        }
        return world;
    }

    public void setupGamerules(World world) {
        if (world == null) return;
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, true);
        world.setPVP(false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setTime(1000);
        world.setDifficulty(Difficulty.HARD);
    }

    public World getUhcWorld() {
        return uhcWorld;
    }

    public World getLobbyWorld() {
        return lobbyWorld;
    }

    public BorderController getBorderController() {
        return borderController;
    }

    public ScatterManager getScatterManager() {
        return scatterManager;
    }

    public void setLobbySpawn(Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        this.lobbyWorld = loc.getWorld();
        plugin.getConfig().set("lobby.spawn.world", loc.getWorld().getName());
        plugin.getConfig().set("lobby.spawn.x", loc.getX());
        plugin.getConfig().set("lobby.spawn.y", loc.getY());
        plugin.getConfig().set("lobby.spawn.z", loc.getZ());
        plugin.getConfig().set("lobby.spawn.yaw", (double) loc.getYaw());
        plugin.getConfig().set("lobby.spawn.pitch", (double) loc.getPitch());
        plugin.saveConfig();
    }

    public Location getLobbySpawn() {
        if (plugin.getConfig().contains("lobby.spawn.world")) {
            String wName = plugin.getConfig().getString("lobby.spawn.world");
            World w = Bukkit.getWorld(wName != null ? wName : "world");
            if (w != null) {
                double x = plugin.getConfig().getDouble("lobby.spawn.x");
                double y = plugin.getConfig().getDouble("lobby.spawn.y");
                double z = plugin.getConfig().getDouble("lobby.spawn.z");
                float yaw = (float) plugin.getConfig().getDouble("lobby.spawn.yaw");
                float pitch = (float) plugin.getConfig().getDouble("lobby.spawn.pitch");
                return new Location(w, x, y, z, yaw, pitch);
            }
        }

        if (lobbyWorld != null) {
            return lobbyWorld.getSpawnLocation().add(0.5, 0, 0.5);
        }
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }
}
