package net.battleroyalex.util;

public final class TimeUtil {

    private TimeUtil() {
    }

    public static String formatSeconds(int totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public static int parseTimeString(String input) {
        if (input == null || input.isEmpty()) return 0;
        input = input.toLowerCase().trim();
        try {
            if (input.endsWith("s")) {
                return Integer.parseInt(input.substring(0, input.length() - 1));
            } else if (input.endsWith("m")) {
                return Integer.parseInt(input.substring(0, input.length() - 1)) * 60;
            } else if (input.endsWith("h")) {
                return Integer.parseInt(input.substring(0, input.length() - 1)) * 3600;
            } else {
                return Integer.parseInt(input);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
