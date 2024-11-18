package net.botwithus;

public class GemScriptUtils {

    public static boolean isWithinLevel(int itemId, int playerLevel) {
        Integer requirement = GemConstants.CRAFTING_REQUIREMENTS.get(itemId);
        return requirement != null && playerLevel >= requirement;
    }

    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String formatNumber(int number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return String.format("%.1fK", number / 1000.0);
        } else {
            return String.format("%.2fM", number / 1000000.0);
        }
    }

    public static int calculatePerHour(int amount, long startTime) {
        long timeElapsed = System.currentTimeMillis() - startTime;
        double hoursElapsed = timeElapsed / (1000.0 * 60 * 60);
        return hoursElapsed > 0 ? (int)(amount / hoursElapsed) : 0;
    }

}
