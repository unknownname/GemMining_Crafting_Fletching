package net.botwithus;

import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

public class SkeletonScriptGraphicsContext extends ScriptGraphicsContext {

    private SkeletonScript script;

    private long scriptstartTime;
    private int startingXP;
    boolean isScriptRunning = false;


    public SkeletonScriptGraphicsContext(ScriptConsole scriptConsole, SkeletonScript script) {
        super(scriptConsole);
        this.script = script;
        this.startingXP = Skills.MINING.getSkill().getExperience();
        this.scriptstartTime = System.currentTimeMillis();
    }



    @Override
    public void drawSettings() {
             if (ImGui.Begin("Gem Mining", ImGuiWindowFlag.None.getValue())) {
                long elapsedTimeMillis = System.currentTimeMillis() - scriptstartTime;
                long elapsedSeconds = elapsedTimeMillis / 1000;
                long hours = elapsedSeconds / 3600;
                long minutes = (elapsedSeconds % 3600) / 60;
                long seconds = elapsedSeconds % 60;


                if (ImGui.BeginTabBar("Bar", ImGuiWindowFlag.None.getValue())) {
                    if (ImGui.BeginTabItem("Play", ImGuiWindowFlag.None.getValue())) {
                        ImGui.Text("Welcome to Al Kharid Mining Script");
                        ImGui.Text("Scripts state is: " + script.getBotState());
                        if (ImGui.Button("Start")) {
                            //button has been clicked
                            script.setBotState(SkeletonScript.BotState.RUNNING);
                        }
                        ImGui.SameLine();
                        if (ImGui.Button("Stop")) {
                            //has been clicked
                            script.setBotState(SkeletonScript.BotState.IDLE);
                        }
                        ImGui.EndTabItem();
                    }
                    if (ImGui.BeginTabItem("Stats", ImGuiWindowFlag.None.getValue())) {
                        String displayTimeRunning = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        ImGui.SeparatorText("Time Running  " + displayTimeRunning);
                        ImGui.SeparatorText("Mining");
                        ImGui.Text("Mining Level: " + Skills.MINING.getLevel());
                        displayXPGained(Skills.MINING);
                        displayXpPerHour(Skills.MINING);
                        XPtillNextLevel(Skills.MINING);
                        String timetolevel = calculateTimeTillNextLevel();
                        ImGui.Text(timetolevel);
                        ImGui.Separator();
                        ImGui.Text("Number of Gem Mined: " + script.GemMined);
                        //ImGui.Separator();
                        ImGui.Text("Gem Mined Per Hour: " + script.GemMinedPerHour);
                        ImGui.Separator();

                        ImGui.EndTabItem();
                    }
                    if (ImGui.BeginTabItem("Other", ImGuiWindowFlag.None.getValue())) {
                        ImGui.Text("Please configure Available unlock:");
                        script.setwarTeleport(ImGui.Checkbox("War's Retreat Teleport", script.iswarTeleport()));
                        script.setmysticalseed(ImGui.Checkbox("Mystical Sand Seed", script.ismysticalseed()));
                        ImGui.EndTabItem();
                    }
                    ImGui.EndTabBar();
                }
                ImGui.End();
            }


    }

    private void displayXPGained(Skills skill)
    {
        int currentXP = skill.getSkill().getExperience();
        int xpGained = currentXP - startingXP;
        ImGui.Text("Xp Gained: " + xpGained);
    }
    private void displayXpPerHour(Skills skill)
    {
        long timeelapsed   = System.currentTimeMillis() - scriptstartTime;
        double hourElapsed = timeelapsed / (1000.0 * 60 * 60);
        int currentXP = skill.getSkill().getExperience();
        int xpGained = currentXP - startingXP;
        double xpPerHour = hourElapsed >0 ? xpGained/hourElapsed :0;

        String forXpPerHour = formatNumberForDisplay(xpPerHour);  //formatted xp per hour
        ImGui.Text( "XP Per Hour: " + forXpPerHour );

    }
    private String formatNumberForDisplay(double number) {
        if (number < 1000) {
            return String.format("%.0f", number); // No suffix
        } else if (number < 1000000) {
            return String.format("%.1fk", number / 1000); // Thousands
        } else if (number < 1000000000) {
            return String.format("%.1fM", number / 1000000); // Millions
        } else {
            return String.format("%.1fB", number / 1000000000); // Billions
        }
    }
    private void XPtillNextLevel(Skills skill) {

        // Get the current XP in RUNECRAFTING
        int currentXP = Skills.MINING.getSkill().getExperience();
        // Get the current level in RUNECRAFTING
        int currentLevel = Skills.MINING.getSkill().getLevel();
        // Calculate the XP required for the next level
        int xpForNextLevel = Skills.MINING.getExperienceAt(currentLevel + 1);
        // Calculate the difference between the XP required for the next level and the current XP
        int xpTillNextLevel = xpForNextLevel - currentXP;

        ImGui.Text("XP Till Next Level: " + xpTillNextLevel);
    }
    private String calculateTimeTillNextLevel() {
        int currentXP = Skills.MINING.getSkill().getExperience();
        int currentLevel = Skills.MINING.getSkill().getLevel();
        int xpForNextLevel = Skills.MINING.getExperienceAt(currentLevel + 1);
        int xpForCurrentLevel = Skills.MINING.getExperienceAt(currentLevel);
        int xpGainedTowardsNextLevel = currentXP - xpForCurrentLevel;

        long currentTime = System.currentTimeMillis();
        int xpGained = currentXP - startingXP;
        long timeElapsed = currentTime - scriptstartTime; // Time elapsed since tracking started in milliseconds

        if (xpGained > 0 && timeElapsed > 0) {
            // Calculate the XP per millisecond
            double xpPerMillisecond = xpGained / (double) timeElapsed;
            // Estimate the time to level up in milliseconds
            long timeToLevelMillis = (long) ((xpForNextLevel - currentXP) / xpPerMillisecond);

            // Convert milliseconds to hours, minutes, and seconds
            long timeToLevelSecs = timeToLevelMillis / 1000;
            long hours = timeToLevelSecs / 3600;
            long minutes = (timeToLevelSecs % 3600) / 60;
            long seconds = timeToLevelSecs % 60;

            return String.format("Time to level: %02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return "Time to level: calculating...";
        }
    }
    private void displayTimeRunning() {
        long elapsedTimeMillis = System.currentTimeMillis() - scriptstartTime;
        long elapsedSeconds = elapsedTimeMillis / 1000;
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;

        String timeRunningFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        ImGui.Text(timeRunningFormatted);
    }

    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }
}
