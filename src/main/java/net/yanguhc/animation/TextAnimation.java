package net.yanguhc.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextAnimation {

    private final String name;
    private final int changeIntervalMs;
    private final List<String> texts;

    public TextAnimation(String name, int changeIntervalMs, List<String> texts) {
        this.name = name;
        this.changeIntervalMs = Math.max(10, changeIntervalMs);
        this.texts = texts != null ? new ArrayList<>(texts) : new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getChangeIntervalMs() {
        return changeIntervalMs;
    }

    public List<String> getTexts() {
        return Collections.unmodifiableList(texts);
    }

    public String getCurrentFrame() {
        if (texts.isEmpty()) return "";
        if (texts.size() == 1 || changeIntervalMs <= 0) return texts.get(0);

        long time = System.currentTimeMillis();
        int index = (int) ((time / changeIntervalMs) % texts.size());
        return texts.get(index);
    }
}
