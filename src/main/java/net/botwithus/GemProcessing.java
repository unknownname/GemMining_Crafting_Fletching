package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.game.skills.Skills;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Item;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.game.minimenu.MiniMenu;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.minimenu.actions.ComponentAction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GemProcessing {
    private static final Random random = new Random();

    private static final Map<Integer, BoltTipInfo> GEM_BOLT_INFO = new HashMap<>();
    static {
        // Format: GemID -> (BoltTipID, FletchingLevel)
        GEM_BOLT_INFO.put(GemConstants.SAPPHIRE, new BoltTipInfo(9189, 56, "Sapphire bolt tips"));
        GEM_BOLT_INFO.put(GemConstants.EMERALD, new BoltTipInfo(9190, 58, "Emerald bolt tips"));
        GEM_BOLT_INFO.put(GemConstants.RUBY, new BoltTipInfo(9191, 63, "Ruby bolt tips"));
        GEM_BOLT_INFO.put(GemConstants.DIAMOND, new BoltTipInfo(9192, 65, "Diamond bolt tips"));
    }

    
    public static ProcessingResult handleCrafting(LocalPlayer player, boolean enableFletching) {
        
        List<Item> cutGems = InventoryItemQuery.newQuery(93)
        .ids(GemConstants.SAPPHIRE, GemConstants.EMERALD, GemConstants.RUBY, GemConstants.DIAMOND)
        .results()
        .stream()
        .toList();

    if (!cutGems.isEmpty()) {
        if (enableFletching) {
            // Check if we can fletch any of the gems
            boolean canFletchAny = false;
            int fletchingLevel = Skills.FLETCHING.getLevel();
            
            for (Item gem : cutGems) {
                BoltTipInfo boltInfo = GEM_BOLT_INFO.get(gem.getId());
                if (boltInfo != null && fletchingLevel >= boltInfo.levelRequired) {
                    canFletchAny = true;
                    break;
                }
            }

            if (!canFletchAny) {
                ScriptConsole.println("Fletching level too low for any gems, dropping them instead");
                handleDropProcessedGems();
                    // If no uncut gems, return to mining
                    if (!Backpack.contains(Pattern.compile("Uncut.*"))) {
                        ScriptConsole.println("No uncut gems remaining, returning to mining");
                        return new ProcessingResult(true, random.nextLong(500, 750));
                    }
            }
        } else {
            // If fletching is disabled, drop the gems
            handleDropProcessedGems();
                // If no uncut gems, return to mining
                if (!Backpack.contains(Pattern.compile("Uncut.*"))) {
                    ScriptConsole.println("No uncut gems remaining, returning to mining");
                    return new ProcessingResult(true, random.nextLong(500, 750));
                }
            }
    }     
        
        int craftingLevel = Skills.CRAFTING.getLevel();

        // First check if we have cut gems to drop (when fletching is disabled)
        // if (!enableFletching) {
        //     long dropResult = handleDropProcessedGems();
        //     if (dropResult > 0) {
        //         return dropResult;
        //     }
        // }
        
        // Process gems in order: diamond, ruby, emerald, sapphire
        int[] gemPriority = {
            GemConstants.UNCUT_DIAMOND,
            GemConstants.UNCUT_RUBY,
            GemConstants.UNCUT_EMERALD,
            GemConstants.UNCUT_SAPPHIRE
        };

        for (int gemId : gemPriority) {
            // Skip if we don't meet the level requirement
            if (!GemScriptUtils.isWithinLevel(gemId, craftingLevel)) {
                continue;
            }

            if (Backpack.contains(gemId)) {
                //return processGem(gemId, "Craft");
                return new ProcessingResult(false, processGem(gemId, "Craft"));
            }
        }

        ScriptConsole.println("No gems to process, returning to mining");
        return new ProcessingResult(true, random.nextLong(500, 750));
    }

    public static long handleDropProcessedGems() {
        // Get all cut gems in inventory
        List<Item> cutGems = InventoryItemQuery.newQuery(93)
            .ids(
                GemConstants.SAPPHIRE,
                GemConstants.EMERALD,
                GemConstants.RUBY,
                GemConstants.DIAMOND
            )
            .results().stream().toList();

        if (!cutGems.isEmpty()) {
            ScriptConsole.println("Found " + cutGems.size() + " cut gems to drop");
            
            // Drop each gem with a slight delay between drops
            for (Item gem : cutGems) {
                Component gemComponent = ComponentQuery.newQuery(GemConstants.INVENTORY_INTERFACE)
                    .item(gem.getId())
                    .results()
                    .first();

                if (gemComponent != null) {
                    String gemName = getGemName(gem.getId());
                    ScriptConsole.println("Dropping " + gemName);
                    
                    if (gemComponent.interact("Drop")) {
                        // Add a small random delay between drops
                        Execution.delay(random.nextLong(800, 1200));
                    }
                }
            }
            return random.nextLong(500, 750);
        }
        return 0;
    }

    public static long handleFletching(LocalPlayer player) {

        long dropResult = handleDropBoltTips();
        if (dropResult > 0) {
            return dropResult;
        }

        int[] gemPriority = {
            GemConstants.DIAMOND,
            GemConstants.RUBY,
            GemConstants.EMERALD,
            GemConstants.SAPPHIRE
        };

        for (int gemId : gemPriority) {
            if (Backpack.contains(gemId)) {
                return processGem(gemId, "Cut into bolt tips");
            }
        }

        return random.nextLong(750, 1250);
    }

    

    public static long handleDropBoltTips() {
        // Get all bolt tips in inventory
        List<Item> boltTips = InventoryItemQuery.newQuery(93)
            .ids(GEM_BOLT_INFO.values().stream().mapToInt(info -> info.boltId).toArray())
            .results().stream().toList();

        if (!boltTips.isEmpty()) {
            ScriptConsole.println("Found " + boltTips.size() + " bolt tips to drop");
            
            for (Item bolt : boltTips) {
                Component boltComponent = ComponentQuery.newQuery(GemConstants.INVENTORY_INTERFACE)
                    .item(bolt.getId())
                    .results()
                    .first();

                if (boltComponent != null) {
                    String boltName = getBoltName(bolt.getId());
                    ScriptConsole.println("Dropping " + boltName);
                    
                    if (boltComponent.interact("Drop")) {
                        // Add random delay between drops
                        Execution.delay(random.nextLong(100, 250));
                    }
                }
            }
            return random.nextLong(500, 750);
        }
        return 0;
    }

    private static long processGem(int gemId, String action) {
        Component gemComponent = ComponentQuery.newQuery(GemConstants.INVENTORY_INTERFACE)
            .item(gemId)
            .option(action)
            .results()
            .first();

        if (gemComponent != null && gemComponent.interact(action)) {
            ScriptConsole.println(action + "ing " + getGemName(gemId));
            Execution.delayUntil(15000, () -> Interfaces.isOpen(GemConstants.CRAFTING_INTERFACE));
            MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 89784350);
            Execution.delayUntil(15000, () -> !Backpack.contains(gemId));
            return random.nextLong(1000, 2000);
        }

        return random.nextLong(750, 1250);
    }

    private static String getGemName(int gemId) {
        switch (gemId) {
            case GemConstants.UNCUT_SAPPHIRE:
            case GemConstants.SAPPHIRE:
                return "Sapphire";
            case GemConstants.UNCUT_EMERALD:
            case GemConstants.EMERALD:
                return "Emerald";
            case GemConstants.UNCUT_RUBY:
            case GemConstants.RUBY:
                return "Ruby";
            case GemConstants.UNCUT_DIAMOND:
            case GemConstants.DIAMOND:
                return "Diamond";
            default:
                return "Unknown";
        }
    }

    private static String getBoltName(int boltId) {
        switch (boltId) {
            case 9189:
                return "Sapphire bolt tips";
            case 9190:
                return "Emerald bolt tips";
            case 9191:
                return "Ruby bolt tips";
            case 9192:
                return "Diamond bolt tips";
            default:
                return "Unknown bolt tips";
        }
    }

    public static boolean canProcessGem(int gemId) {
        int craftingLevel = Skills.CRAFTING.getLevel();
        Integer requirement = GemConstants.CRAFTING_REQUIREMENTS.get(gemId);
        return requirement != null && craftingLevel >= requirement;
    }

    private static class GemInfo {
        final String uncutName;
        final int uncutId;
        final int cutId;
        final int craftLevel;

        GemInfo(String uncutName, int uncutId, int cutId, int craftLevel) {
            this.uncutName = uncutName;
            this.uncutId = uncutId;
            this.cutId = cutId;
            this.craftLevel = craftLevel;
        }
    }

    private static class BoltTipInfo {
        final int boltId;
        final int levelRequired;
        final String name;

        BoltTipInfo(int boltId, int levelRequired, String name) {
            this.boltId = boltId;
            this.levelRequired = levelRequired;
            this.name = name;
        }
    }

      // Result class to handle both state transition and delay
      public static class ProcessingResult {
        public final boolean shouldMine;
        public final long delay;

        public ProcessingResult(boolean shouldMine, long delay) {
            this.shouldMine = shouldMine;
            this.delay = delay;
        }
    }
}