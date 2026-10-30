package net.yanguhc.scenario;

import net.yanguhc.YangUHC;
import net.yanguhc.scenario.impl.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ScenarioManager {

    private final YangUHC plugin;
    private final Map<String, Scenario> scenarios = new LinkedHashMap<>();
    private File file;
    private FileConfiguration config;

    public ScenarioManager(YangUHC plugin) {
        this.plugin = plugin;
        registerDefaults();
        loadFile();
    }

    private void registerDefaults() {
        register(new CutCleanScenario(plugin));
        register(new TimeBombScenario(plugin));
        register(new TimberScenario(plugin));
        register(new DiamondlessScenario(plugin));
        register(new GoldlessScenario(plugin));
        register(new FirelessScenario(plugin));
        register(new RodlessScenario(plugin));
        register(new BowlessScenario(plugin));
        register(new AbsorptionlessScenario(plugin));
        register(new GoldenRetrieverScenario(plugin));
        register(new HasteyBoysScenario(plugin));
        register(new HorselessScenario(plugin));
        register(new NoCleanScenario(plugin));
        register(new BleedingSweetsScenario(plugin));
        register(new SoupScenario(plugin));
    }

    public void register(Scenario scenario) {
        scenarios.put(scenario.getName().toLowerCase(), scenario);
    }

    private void loadFile() {
        this.file = new File(plugin.getDataFolder(), "scenarios.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            createDefaultConfig();
        }
        this.config = YamlConfiguration.loadConfiguration(file);

        List<String> enabledList = config.getStringList("enabled-scenarios");
        for (String name : enabledList) {
            Scenario scenario = scenarios.get(name.toLowerCase());
            if (scenario != null) {
                scenario.setEnabled(true);
            }
        }
    }

    private void createDefaultConfig() {
        YamlConfiguration initial = new YamlConfiguration();
        // CutClean, Timber, and GoldenRetriever enabled by default
        initial.set("enabled-scenarios", Arrays.asList("cutclean", "timber", "goldenretriever"));
        try {
            initial.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo crear scenarios.yml: " + e.getMessage());
        }
    }

    public void saveConfig() {
        List<String> enabledList = new ArrayList<>();
        for (Scenario s : scenarios.values()) {
            if (s.isEnabled()) {
                enabledList.add(s.getName().toLowerCase());
            }
        }
        config.set("enabled-scenarios", enabledList);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar scenarios.yml: " + e.getMessage());
        }
    }

    public Scenario getScenario(String name) {
        return name == null ? null : scenarios.get(name.toLowerCase());
    }

    public Collection<Scenario> getAllScenarios() {
        return Collections.unmodifiableCollection(scenarios.values());
    }

    public List<Scenario> getEnabledScenarios() {
        List<Scenario> enabled = new ArrayList<>();
        for (Scenario s : scenarios.values()) {
            if (s.isEnabled()) {
                enabled.add(s);
            }
        }
        return enabled;
    }

    public boolean toggleScenario(String name) {
        Scenario scenario = getScenario(name);
        if (scenario == null) return false;

        boolean newState = !scenario.isEnabled();
        scenario.setEnabled(newState);
        saveConfig();

        String status = newState ? plugin.getMessages().getRaw("scenarios.status-enabled") : plugin.getMessages().getRaw("scenarios.status-disabled");
        plugin.getMessages().broadcast("scenarios.toggled", "scenario", scenario.getName(), "status", status);
        return newState;
    }
}
