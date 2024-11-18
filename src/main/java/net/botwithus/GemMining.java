package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.scene.entities.characters.Headbar;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.game.vars.VarManager;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.game.minimenu.MiniMenu;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.minimenu.actions.ComponentAction;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.game.Item;


public class GemMining {
    private static final Random random = new Random();

    public static long mineGems(LocalPlayer player) {
        int miningLevel = Skills.MINING.getLevel();
        SceneObject gemRock;

        if (miningLevel < 20) {
            gemRock = SceneObjectQuery.newQuery()
                .name("Common gem rock")
                .option("Mine")
                .results()
                .nearest();
        } else {
            gemRock = SceneObjectQuery.newQuery()
                .name("Uncommon gem rock")
                .option("Mine")
                .results()
                .nearest();
        }
        
        if (gemRock != null && !Backpack.isFull()) {
            List<Headbar> headbars = player.getHeadbars().stream().filter(headbar -> headbar.getId() == 5).toList();
            if (!headbars.isEmpty()) {
                Headbar progressBar = headbars.get(0);
                
                if (progressBar.getId() == 5 && progressBar.getWidth() <= random.nextInt(0, 30)) {
                    if (gemRock.interact("Mine")) {
                        ScriptConsole.println("Mining " + gemRock.getName());
                        
                        Execution.delay(random.nextLong(750, 1250));
                        return random.nextLong(35000, 42000);
                    }
                }
            } 
            else {
                
                if (gemRock.interact("Mine")) {
                    ScriptConsole.println("Starting to mine " + gemRock.getName());
                    
                    Execution.delay(random.nextLong(750, 1250));
                    return random.nextLong(35000, 42000);
                }
            }
        }

        return random.nextLong(1500, 3000);
    }
}