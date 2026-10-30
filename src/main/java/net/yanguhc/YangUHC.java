package net.yanguhc;

import net.yanguhc.command.*;
import net.yanguhc.game.GameManager;
import net.yanguhc.kit.KitGUI;
import net.yanguhc.kit.KitManager;
import net.yanguhc.listener.GameListener;
import net.yanguhc.scenario.ScenarioGUI;
import net.yanguhc.scenario.ScenarioManager;
import net.yanguhc.scoreboard.ScoreboardManager;
import net.yanguhc.team.TeamGUI;
import net.yanguhc.team.TeamManager;
import net.yanguhc.world.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;

public class YangUHC extends JavaPlugin {

    private static YangUHC instance;

    private WorldManager worldManager;
    private TeamManager teamManager;
    private KitManager kitManager;
    private ScenarioManager scenarioManager;
    private GameManager gameManager;
    private ScoreboardManager scoreboardManager;

    private ScenarioGUI scenarioGUI;
    private TeamGUI teamGUI;
    private KitGUI kitGUI;

    @Override
    public void onEnable() {
        instance = this;

        // Save default configuration
        saveDefaultConfig();

        // Initialize Managers
        this.worldManager = new WorldManager(this);
        this.worldManager.init();

        this.teamManager = new TeamManager(this);
        this.kitManager = new KitManager(this);
        this.scenarioManager = new ScenarioManager(this);
        this.gameManager = new GameManager(this);
        this.scoreboardManager = new ScoreboardManager(this);

        // Initialize GUIs
        this.scenarioGUI = new ScenarioGUI(this);
        this.teamGUI = new TeamGUI(this);
        this.kitGUI = new KitGUI(this);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new GameListener(this), this);

        // Register Commands
        if (getCommand("uhc") != null) {
            UHCCommand uhcCmd = new UHCCommand(this);
            getCommand("uhc").setExecutor(uhcCmd);
            getCommand("uhc").setTabCompleter(uhcCmd);
        }

        if (getCommand("team") != null) {
            TeamCommand teamCmd = new TeamCommand(this);
            getCommand("team").setExecutor(teamCmd);
            getCommand("team").setTabCompleter(teamCmd);
        }

        if (getCommand("tc") != null) {
            getCommand("tc").setExecutor(new TeamChatCommand(this));
        }

        if (getCommand("scenarios") != null) {
            getCommand("scenarios").setExecutor(new ScenariosCommand(this));
        }

        getLogger().info("======================================");
        getLogger().info("  YangUHC v" + getDescription().getVersion() + " iniciado exitosamente!");
        getLogger().info("  Escenarios: " + scenarioManager.getEnabledScenarios().size() + " activos");
        getLogger().info("  Mundo UHC: " + (worldManager.getUhcWorld() != null ? worldManager.getUhcWorld().getName() : "Pendiente"));
        getLogger().info("======================================");
    }

    @Override
    public void onDisable() {
        if (scoreboardManager != null) {
            scoreboardManager.shutdown();
        }

        getLogger().info("YangUHC ha sido desactivado.");
    }

    public static YangUHC getInstance() {
        return instance;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public ScenarioManager getScenarioManager() {
        return scenarioManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public ScenarioGUI getScenarioGUI() {
        return scenarioGUI;
    }

    public TeamGUI getTeamGUI() {
        return teamGUI;
    }

    public KitGUI getKitGUI() {
        return kitGUI;
    }
}
