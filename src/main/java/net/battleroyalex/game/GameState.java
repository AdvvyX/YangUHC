package net.battleroyalex.game;

public enum GameState {
    LOBBY("Esperando", "<yellow>Lobby</yellow>"),
    SCATTERING("Teleportando", "<aqua>Scatter</aqua>"),
    GRACE_PERIOD("Periodo de Gracia", "<green>Gracia</green>"),
    INGAME("PvP Activo", "<red>En Juego</red>"),
    DEATHMATCH("Deathmatch", "<dark_red>Deathmatch</dark_red>"),
    ENDED("Finalizado", "<gold>Finalizado</gold>");

    private final String displayName;
    private final String formattedTag;

    GameState(String displayName, String formattedTag) {
        this.displayName = displayName;
        this.formattedTag = formattedTag;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFormattedTag() {
        return formattedTag;
    }

    public boolean isGameActive() {
        return this == GRACE_PERIOD || this == INGAME || this == DEATHMATCH;
    }
}
