package net.battleroyalex;

import net.battleroyalex.animation.AnimationManager;
import net.battleroyalex.command.*;
import net.battleroyalex.config.Messages;
import net.battleroyalex.game.ConfigGUI;
import net.battleroyalex.game.GameManager;
import net.battleroyalex.kit.KitEditorGUI;
import net.battleroyalex.kit.KitGUI;
import net.battleroyalex.kit.KitManager;
import net.battleroyalex.kit.KitManagerGUI;
import net.battleroyalex.kit.KitSelectorGUI;
import net.battleroyalex.listener.GameListener;
import net.battleroyalex.scenario.ScenarioGUI;
import net.battleroyalex.scenario.ScenarioManager;
import net.battleroyalex.scoreboard.ScoreboardManager;
import net.battleroyalex.team.TeamGUI;
import net.battleroyalex.team.TeamManager;
import net.battleroyalex.world.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BattleRoyaleX extends JavaPlugin {

    private static BattleRoyaleX instance;

    private Messages messages;
    private AnimationManager animationManager;
    private WorldManager worldManager;
    private TeamManager teamManager;
    private KitManager kitManager;
    private ScenarioManager scenarioManager;
    private GameManager gameManager;
    private ScoreboardManager scoreboardManager;

    private ScenarioGUI scenarioGUI;
    private TeamGUI teamGUI;
    private KitGUI kitGUI;
    private KitManagerGUI kitManagerGUI;
    private KitEditorGUI kitEditorGUI;
    private ConfigGUI configGUI;
    private KitSelectorGUI kitSelectorGUI;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.messages = new Messages(this);
        this.animationManager = new AnimationManager(this);

        this.worldManager = new WorldManager(this);
        this.worldManager.init();

        this.teamManager = new TeamManager(this);
        this.kitManager = new KitManager(this);
        this.scenarioManager = new ScenarioManager(this);
        this.gameManager = new GameManager(this);
        this.scoreboardManager = new ScoreboardManager(this);

        this.scenarioGUI = new ScenarioGUI(this);
        this.teamGUI = new TeamGUI(this);
        this.kitGUI = new KitGUI(this);
        this.kitManagerGUI = new KitManagerGUI(this);
        this.kitEditorGUI = new KitEditorGUI(this);
        this.configGUI = new ConfigGUI(this);
        this.kitSelectorGUI = new KitSelectorGUI(this);

        getServer().getPluginManager().registerEvents(new GameListener(this), this);

        BattleRoyaleCommand brCmd = new BattleRoyaleCommand(this);
        if (getCommand("battleroyale") != null) {
            getCommand("battleroyale").setExecutor(brCmd);
            getCommand("battleroyale").setTabCompleter(brCmd);
        } else if (getCommand("uhc") != null) {
            getCommand("uhc").setExecutor(brCmd);
            getCommand("uhc").setTabCompleter(brCmd);
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
        getLogger().info("  BattleRoyaleX v" + getDescription().getVersion() + " iniciado exitosamente!");
        getLogger().info("  Escenarios: " + scenarioManager.getEnabledScenarios().size() + " activos");
        getLogger().info("  Animaciones: " + animationManager.getAllAnimations().size() + " cargadas");
        getLogger().info("  Mundo Battle Royale: " + (worldManager.getUhcWorld() != null ? worldManager.getUhcWorld().getName() : "Pendiente"));
        getLogger().info("======================================");
    }

    @Override
    public void onDisable() {
        if (scoreboardManager != null) {
            scoreboardManager.shutdown();
        }
        getLogger().info("BattleRoyaleX ha sido desactivado.");
    }

    public static BattleRoyaleX getInstance() {
        return instance;
    }

    public Messages getMessages() {
        return messages;
    }

    public AnimationManager getAnimationManager() {
        return animationManager;
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

    public KitManagerGUI getKitManagerGUI() {
        return kitManagerGUI;
    }

    public KitEditorGUI getKitEditorGUI() {
        return kitEditorGUI;
    }

    public ConfigGUI getConfigGUI() {
        return configGUI;
    }

    public KitSelectorGUI getKitSelectorGUI() {
        return kitSelectorGUI;
    }
}
