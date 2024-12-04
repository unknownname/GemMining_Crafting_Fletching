package net.botwithus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Item;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;
import java.util.regex.Pattern;




public class GemScriptUI extends ScriptGraphicsContext {

    private GemScript script;
    private Item gemBag;
    private long scriptstartTime;
    private int startingXP;
    boolean isScriptRunning = false;

    private Map<String, Boolean> selectedGems;
    private final List<String> availableBanks;
    private int selectedBankIndex = 0;
    private long scriptStartTime;

    // XP tracking fields
    private final int startingMiningXP;
    private final int startingCraftingXP;
    private final int startingFletchingXP;

    private long lastGemBagUpdate = 0;
    private static final long UPDATE_INTERVAL = 500; //


    public GemScriptUI (ScriptConsole scriptConsole, GemScript script) {
        super(scriptConsole);
        this.script = script;
        this.startingXP = Skills.MINING.getSkill().getExperience();
        this.scriptstartTime = System.currentTimeMillis();
         this.selectedGems = new HashMap<>();
        selectedGems.put("Sapphire", true);
        selectedGems.put("Emerald", true);
        selectedGems.put("Ruby", true);
        selectedGems.put("Diamond", true);


        this.availableBanks = new ArrayList<>();
        availableBanks.add("ArchChest");
        availableBanks.add("GrandExchange");
        availableBanks.add("Catherby");
        availableBanks.add("Seers");
        availableBanks.add("ArdougneNorth");
        availableBanks.add("ArdougneSouth");
        availableBanks.add("Yanille");
        availableBanks.add("Burthorpe");
        availableBanks.add("Taverly");
        availableBanks.add("FaladorWest");
        availableBanks.add("FaladorEast");
        availableBanks.add("Draynor");
        availableBanks.add("AlKharid");
        availableBanks.add("Menaphos");
        availableBanks.add("Edgeville");
        availableBanks.add("VarrockWest");
        availableBanks.add("VarrockEast");
        availableBanks.add("Canifis");
        availableBanks.add("PortPhasmatys");
        availableBanks.add("Lumbridge");
        availableBanks.add("FishingGuild");
        availableBanks.add("DeepSeaBoat");
        availableBanks.add("DeepSeaNet");
        availableBanks.add("RangeGuild");


        // Initialize starting XP values
        this.startingMiningXP = Skills.MINING.getSkill().getExperience();
        this.startingCraftingXP = Skills.CRAFTING.getSkill().getExperience();
        this.startingFletchingXP = Skills.FLETCHING.getSkill().getExperience();
    }

    

    @Override
 public void drawSettings() {
        if (ImGui.Begin("Gem Mining & Processing", ImGuiWindowFlag.None.getValue())) {
            if (ImGui.BeginTabBar("Bar", ImGuiWindowFlag.None.getValue())) {
                if (ImGui.BeginTabItem("Controls", ImGuiWindowFlag.None.getValue())) {
                    drawControlsTab();
                    ImGui.EndTabItem();
                }
                
                if (ImGui.BeginTabItem("Stats", ImGuiWindowFlag.None.getValue())) {
                    drawStatsTab();
                    ImGui.EndTabItem();
                }
                
                if (ImGui.BeginTabItem("Debug", ImGuiWindowFlag.None.getValue())) {
                    drawDebugTab();
                    ImGui.EndTabItem();
                }
                
                ImGui.EndTabBar();
            }
            ImGui.End();
        }
    }

    private void drawControlsTab() {
        ImGui.Text("Script Controls");
        ImGui.Text("Scripts state is: " + script.getBotState());
        if (ImGui.Button("Start")) {
            script.setBotState(GemScript.BotState.RUNNING);
        }
        ImGui.SameLine();
        if (ImGui.Button("Stop")) {
            script.setBotState(GemScript.BotState.IDLE);
        }
        
        ImGui.Separator();
        
        // Banking options
        ImGui.Text("Banking Options:");
        String[] bankArray = availableBanks.toArray(new String[0]);
        int newSelectedBankIndex = ImGui.Combo("Select Bank", selectedBankIndex, bankArray);
        if (newSelectedBankIndex != selectedBankIndex) {
            selectedBankIndex = newSelectedBankIndex;
            script.setSelectedBank(bankArray[selectedBankIndex]);
        }
        
        //script.setWarTeleport(ImGui.Checkbox("Use War's Retreat", script.isWarTeleport()));
        
        ImGui.Separator();
        
        // Processing options
        ImGui.Text("Processing Options:");
        script.setUseGemBag(ImGui.Checkbox("Use Gem Bag", script.isUseGemBag()));
        script.setEnableCrafting(ImGui.Checkbox("Enable Crafting", script.isEnableCrafting()));
        script.setEnableFletching(ImGui.Checkbox("Enable Fletching", script.isEnableFletching()));
        script.setEnableBankCrafting(ImGui.Checkbox("Enable Crafting at Bank", script.isEnableBankCrafting()));
    }

    private void drawStatsTab() {
        long elapsedTimeMillis = System.currentTimeMillis() - scriptStartTime;
        long elapsedSeconds = elapsedTimeMillis / 1000;
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;

        String timeRunning = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        ImGui.Text("Time Running: " + timeRunning);
        
        ImGui.Separator();
        
        ImGui.Text("Mining Level: " + Skills.MINING.getLevel());
        if (script.isEnableCrafting()) {
            ImGui.Text("Crafting Level: " + Skills.CRAFTING.getLevel());
        }
        if (script.isEnableFletching()) {
            ImGui.Text("Fletching Level: " + Skills.FLETCHING.getLevel());
        }
        
        ImGui.Separator();
         // Gem statistics
         int gemsMined = script.getGemsMined();
         int gemsPerHour = calculateGemsPerHour();
         
         ImGui.Text("Gems Mined: " + formatNumber(gemsMined));
         ImGui.Text("Gems/Hour: " + formatNumber(gemsPerHour));
        
        if (script.isUseGemBag()) {
            ImGui.Separator();
            displayGemBagStatus();
        }

        displayExperienceStats();
    }

    private void displayGemBagStatus() {
        if (!script.isUseGemBag()) {
            return;
        }

        ImGui.Separator();
        ImGui.Text("Gem Bag Status:");
        
        if (GemBagHandler.isGemBagUpgraded(gemBag)) {
            // Display individual gem counts for upgraded bag
            ImGui.Text("Sapphires: " + GemBagHandler.getGemCounts(gemBag)[0] + "/60");
            ImGui.Text("Emeralds: " + GemBagHandler.getGemCounts(gemBag)[1] + "/60");
            ImGui.Text("Rubies: " + GemBagHandler.getGemCounts(gemBag)[2] + "/60");
            ImGui.Text("Diamonds: " + GemBagHandler.getGemCounts(gemBag)[3] + "/60");
            
                // Show warning if any gem type is near full
                for (int count : GemBagHandler.getGemCounts(gemBag)) {
                    if (count >= 55) { // Warning at 55+ gems
                        ImGui.Text("Warning: Nearly Full!");
                        break;
                    }
                }
            } else {
                // Display total gems for regular bag
                int totalGems = Arrays.stream(GemBagHandler.getGemCounts(gemBag)).sum();
                ImGui.Text("Total Gems: " + totalGems + "/100");
                
                // Show warning if nearly full
                if (GemBagHandler.getTotalGems(GemBagHandler.getGemCounts(gemBag)) >= 90) {
                    ImGui.Text("Warning: Nearly Full!");
                }
            }
        }
    


    private void drawDebugTab() {
        ImGui.Text("Current State: " + script.getBotState());
        ImGui.Text("Selected Bank: " + script.getSelectedBank());
        ImGui.Text("Current Location: " + Client.getLocalPlayer().getCoordinate().toString());
        
    }

    private String formatNumber(int number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return String.format("%.1fK", number / 1000.0);
        } else {
            return String.format("%.2fM", number / 1000000.0);
        }
    }

    private int calculateGemsPerHour() {
        long timeElapsed = System.currentTimeMillis() - scriptStartTime;
        double hoursElapsed = timeElapsed / (1000.0 * 60 * 60);
        return hoursElapsed > 0 ? (int)(script.getGemsMined() / hoursElapsed) : 0;
    }

    private void displayExperienceStats() {
        ImGui.Separator();
        ImGui.Text("Experience Stats:");
        
        // Mining XP
        int currentMiningXP = Skills.MINING.getSkill().getExperience();
        int miningXPGained = currentMiningXP - startingMiningXP;
        int miningXPPerHour = calculateXPPerHour(miningXPGained);
        
        ImGui.Text("Mining XP Gained: " + formatNumber(miningXPGained));
        ImGui.Text("Mining XP/Hour: " + formatNumber(miningXPPerHour));
        
        // If crafting enabled, show crafting XP
        if (script.isEnableCrafting()) {
            int currentCraftingXP = Skills.CRAFTING.getSkill().getExperience();
            int craftingXPGained = currentCraftingXP - startingCraftingXP;
            int craftingXPPerHour = calculateXPPerHour(craftingXPGained);
            
            ImGui.Text("Crafting XP Gained: " + formatNumber(craftingXPGained));
            ImGui.Text("Crafting XP/Hour: " + formatNumber(craftingXPPerHour));
        }
        
        // If fletching enabled, show fletching XP
        if (script.isEnableFletching()) {
            int currentFletchingXP = Skills.FLETCHING.getSkill().getExperience();
            int fletchingXPGained = currentFletchingXP - startingFletchingXP;
            int fletchingXPPerHour = calculateXPPerHour(fletchingXPGained);
            
            ImGui.Text("Fletching XP Gained: " + formatNumber(fletchingXPGained));
            ImGui.Text("Fletching XP/Hour: " + formatNumber(fletchingXPPerHour));
        }
    }

    // private int calculateGemsPerHour() {
    //     long timeElapsed = System.currentTimeMillis() - script.getScriptStartTime();
    //     double hoursElapsed = timeElapsed / (1000.0 * 60 * 60);
    //     return hoursElapsed > 0 ? (int)(script.getGemsMined() / hoursElapsed) : 0;
    // }

    private int calculateXPPerHour(int xpGained) {
        long timeElapsed = System.currentTimeMillis() - script.getScriptStartTime();
        double hoursElapsed = timeElapsed / (1000.0 * 60 * 60);
        return hoursElapsed > 0 ? (int)(xpGained / hoursElapsed) : 0;
    }

    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }
}
