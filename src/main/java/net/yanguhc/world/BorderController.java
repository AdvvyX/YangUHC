package net.yanguhc.world;

import net.yanguhc.YangUHC;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BorderController {

    private final YangUHC plugin;
    private final World world;
    private double currentRadius;
    private int shrinkStageIndex = 0;
    private final List<Double> shrinkStages = new ArrayList<>();
    private boolean shrinkEnabled = true;

    public BorderController(YangUHC plugin, World world) {
        this.plugin = plugin;
        this.world = world;
        loadStages();
    }

    public void loadStages() {
        shrinkStages.clear();
        List<Double> configured = plugin.getConfig().getDoubleList("border.stages");
        if (configured == null || configured.isEmpty()) {
            shrinkStages.add(1000.0);
            shrinkStages.add(750.0);
            shrinkStages.add(500.0);
            shrinkStages.add(250.0);
            shrinkStages.add(100.0);
            shrinkStages.add(50.0);
            shrinkStages.add(25.0);
        } else {
            shrinkStages.addAll(configured);
        }

        double initialRadius = plugin.getConfig().getDouble("border.initial-radius", 1000.0);
        this.currentRadius = initialRadius;
        resetBorder(initialRadius);
    }

    public void resetBorder(double radius) {
        this.currentRadius = radius;
        this.shrinkStageIndex = 0;
        if (world != null) {
            WorldBorder border = world.getWorldBorder();
            border.setCenter(0.0, 0.0);
            border.setSize(radius * 2.0);
            border.setDamageAmount(0.5);
            border.setDamageBuffer(2.0);
            border.setWarningDistance(5);
        }
    }

    public void shrinkTo(double targetRadius, long seconds) {
        this.currentRadius = targetRadius;
        if (world != null) {
            WorldBorder border = world.getWorldBorder();
            border.setSize(targetRadius * 2.0, seconds);
        }

        plugin.getMessages().broadcast("border.shrinking", "radius", (int) targetRadius, "seconds", seconds);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.5f);
        }
    }

    public boolean shrinkNextStage(long seconds) {
        if (!shrinkEnabled) {
            return false;
        }
        if (shrinkStageIndex < shrinkStages.size()) {
            double nextRadius = shrinkStages.get(shrinkStageIndex++);
            shrinkTo(nextRadius, seconds);
            return true;
        }
        return false;
    }

    public boolean isShrinkEnabled() {
        return shrinkEnabled;
    }

    public void setShrinkEnabled(boolean shrinkEnabled) {
        this.shrinkEnabled = shrinkEnabled;
    }

    public double getCurrentRadius() {
        return currentRadius;
    }

    public double getNextStageRadius() {
        if (shrinkStageIndex < shrinkStages.size()) {
            return shrinkStages.get(shrinkStageIndex);
        }
        return currentRadius;
    }
}
