package net.botwithus;

import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.game.Item;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.minimenu.MiniMenu;
import net.botwithus.rs3.game.minimenu.actions.ComponentAction;
import net.botwithus.rs3.game.movement.Movement;
import net.botwithus.rs3.game.movement.NavPath;
import net.botwithus.rs3.game.movement.TraverseEvent;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.game.vars.VarManager;
import net.botwithus.rs3.game.inventories.Backpack;


import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;
import net.botwithus.rs3.script.ScriptConsole;

public class GemBagHandler {
        private static final Random random = new Random();
    private static final int REGULAR_GEM_BAG_LIMIT = 100;
    private static final int UPGRADED_GEM_BAG_LIMIT_PER_GEM = 60;

    // Varbit IDs for upgraded gem bag
    private static final int SAPPHIRE_VARBIT = 22581;
    private static final int EMERALD_VARBIT = 22582;
    private static final int RUBY_VARBIT = 22583;
    private static final int DIAMOND_VARBIT = 22584;

    public static boolean isGemBagUpgraded(Item gemBag) {
        return gemBag != null && gemBag.getName().contains("upgraded");
    }

    public static boolean isGemBagEmpty(Item gemBag) {
        if (gemBag == null) return true;

        int[] gemCounts = getGemCounts(gemBag);
        return getTotalGems(gemCounts) == 0;
    }

    public static long handleGemBag(LocalPlayer player) {
        Item gemBag = InventoryItemQuery.newQuery(93)
            .name(Pattern.compile("Gem bag( \\(upgraded\\))?"))
            .results()
            .first();

        if (gemBag == null) {
            ScriptConsole.println("No gem bag found");
            return -1;
        }

        boolean isUpgraded = isGemBagUpgraded(gemBag);
        int[] gemCounts = getGemCounts(gemBag);
        
        // Log current contents
        logGemBagContents(gemCounts, isUpgraded);
        
        if (isGemBagFull(gemCounts, isUpgraded)) {
            ScriptConsole.println("Gem bag is full (" + (isUpgraded ? "upgraded" : "regular") + "), banking required");
            return -1;
        }

        // Check if we have gems to fill
        boolean hasGemsToFill = Backpack.contains(GemConstants.UNCUT_SAPPHIRE) ||
                               Backpack.contains(GemConstants.UNCUT_EMERALD) ||
                               Backpack.contains(GemConstants.UNCUT_RUBY) ||
                               Backpack.contains(GemConstants.UNCUT_DIAMOND);

        if (!hasGemsToFill) {
            ScriptConsole.println("No gems to fill in backpack");
            return random.nextLong(750, 1250);
        }

        Component gemBagComp = ComponentQuery.newQuery(1473)
            .componentIndex(5)
            .itemName(gemBag.getName())
            .option("Fill")
            .results()
            .first();

        if (gemBagComp != null && gemBagComp.interact("Fill")) {
            ScriptConsole.println("Filling " + (isUpgraded ? "upgraded" : "regular") + " gem bag");
            Execution.delay(random.nextLong(750, 1250));
            return random.nextLong(750, 1250);
        }

        return random.nextLong(750, 1250);
    }

    public static int[] getGemCounts(Item gemBag) {
        if (gemBag == null) return new int[4];

        if (isGemBagUpgraded(gemBag)) {
            // For upgraded gem bag, get individual varbit values
            return new int[] {
                VarManager.getVarbitValue(SAPPHIRE_VARBIT),
                VarManager.getVarbitValue(EMERALD_VARBIT),
                VarManager.getVarbitValue(RUBY_VARBIT),
                VarManager.getVarbitValue(DIAMOND_VARBIT)
            };
        } else {
            // For regular gem bag, decode from single varbit
            int progress = VarManager.getInvVarbit(gemBag.getInventoryType().getId(), gemBag.getSlot(), 2154);
            return decodeGemBag(progress);
        }
    }

    public static boolean isGemBagFull(int[] gemCounts, boolean isUpgraded) {
        if (isUpgraded) {
            // For upgraded bag, check if any gem type has reached 60
            for (int count : gemCounts) {
                if (count >= UPGRADED_GEM_BAG_LIMIT_PER_GEM) {
                    return true;
                }
            }
            return false;
        } else {
            // For regular bag, check total against 100
            return getTotalGems(gemCounts) >= REGULAR_GEM_BAG_LIMIT;
        }
    }

    public static String getGemBagStatus(Item gemBag) {
        if (gemBag == null) return "No gem bag";

        int[] counts = getGemCounts(gemBag);
        boolean upgraded = isGemBagUpgraded(gemBag);
        
        if (upgraded) {
            return String.format("Upgraded Bag - S:%d E:%d R:%d D:%d", 
                counts[0], counts[1], counts[2], counts[3]);
        } else {
            return String.format("Regular Bag - Total: %d/100", getTotalGems(counts));
        }
    }

    private static int[] decodeGemBag(int packedValue) {
        int[] gems = new int[4];
        for (int i = 0; i < 4; i++) {
            gems[i] = (packedValue >> (i * 8)) & 0xFF;
        }
        return gems;
    }

    public static int getTotalGems(int[] gemCounts) {
        return Arrays.stream(gemCounts).sum();
    }

    private static void logGemBagContents(int[] counts, boolean isUpgraded) {
        ScriptConsole.println("Gem Bag Contents (" + (isUpgraded ? "upgraded" : "regular") + "):");
        ScriptConsole.println("Sapphires: " + counts[0]);
        ScriptConsole.println("Emeralds: " + counts[1]);
        ScriptConsole.println("Rubies: " + counts[2]);
        ScriptConsole.println("Diamonds: " + counts[3]);
        ScriptConsole.println("Total: " + getTotalGems(counts));
    }

    public static String getDetailedStatus(Item gemBag) {
        if (gemBag == null) return "No gem bag";

        int[] counts = getGemCounts(gemBag);
        boolean upgraded = isGemBagUpgraded(gemBag);
        StringBuilder status = new StringBuilder();

        if (upgraded) {
            status.append("Upgraded Gem Bag:\n");
            status.append(String.format("Sapphires: %d/60\n", counts[0]));
            status.append(String.format("Emeralds: %d/60\n", counts[1]));
            status.append(String.format("Rubies: %d/60\n", counts[2]));
            status.append(String.format("Diamonds: %d/60", counts[3]));
        } else {
            int total = Arrays.stream(counts).sum();
            status.append("Regular Gem Bag:\n");
            status.append(String.format("Total: %d/100\n", total));
            status.append(String.format("Sapphires: %d\n", counts[0]));
            status.append(String.format("Emeralds: %d\n", counts[1]));
            status.append(String.format("Rubies: %d\n", counts[2]));
            status.append(String.format("Diamonds: %d", counts[3]));
        }

        return status.toString();
    }

    public static int getTotalGems(Item gemBag) {
        if (gemBag == null) return 0;
        return Arrays.stream(getGemCounts(gemBag)).sum();
    }
}
