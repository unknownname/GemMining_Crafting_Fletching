package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.api.game.hud.inventories.Bank;
import net.botwithus.api.game.hud.inventories.Equipment;
import net.botwithus.rs3.game.Area;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Coordinate;
import net.botwithus.rs3.game.minimenu.MiniMenu;
import net.botwithus.rs3.game.minimenu.actions.ComponentAction;
import net.botwithus.rs3.game.movement.Movement;
import net.botwithus.rs3.game.movement.NavPath;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.input.GameInput;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.queries.builders.objects.*;


import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class BankAPI implements Runnable {
    private static LocalPlayer player = Client.getLocalPlayer();

    // private static final Map<String, Integer> actionToComponentIndex = new HashMap<>();
    // static {
    //     actionToComponentIndex.put("items", 33882151);
    //     actionToComponentIndex.put("gear", 33882154);
    //     actionToComponentIndex.put("bob", 33882157);
    //     actionToComponentIndex.put("coins", 33882163);
    // }
  
    public static class BankAreas {
        private static final Map<String, Area> banks = new HashMap<>();
        static {
            banks.put("GrandExchange", new Area.Rectangular(new Coordinate(3156, 3500, 0), new Coordinate(3173, 3483, 0)));
            banks.put("Catherby", new Area.Rectangular(new Coordinate(2794, 3438, 0), new Coordinate(2796, 3441, 0)));
            banks.put("Seers", new Area.Rectangular(new Coordinate(2723, 3495, 0), new Coordinate(2727, 3492, 0)));
            banks.put("ArdougneNorth", new Area.Rectangular(new Coordinate(2612, 3336, 0), new Coordinate(2621, 3331, 0)));
            banks.put("ArdougneSouth", new Area.Rectangular(new Coordinate(2649, 3287, 0), new Coordinate(2656, 3280, 0)));
            banks.put("Yanille", new Area.Rectangular(new Coordinate(2609, 3097, 0), new Coordinate(2616, 3088, 0)));
            banks.put("Burthorpe", new Area.Rectangular(new Coordinate(2885, 3538, 0), new Coordinate(2891, 3534, 0)));
            banks.put("Taverly", new Area.Rectangular(new Coordinate(2872, 3421, 0), new Coordinate(2878, 3414, 0)));
            banks.put("FaladorWest", new Area.Rectangular(new Coordinate(2943, 3373, 0), new Coordinate(2949, 3367, 0)));
            banks.put("FaladorEast", new Area.Rectangular(new Coordinate(3009, 3358, 0), new Coordinate(3018, 3354, 0)));
            banks.put("Draynor", new Area.Rectangular(new Coordinate(3090, 3246, 0), new Coordinate(3094, 3244, 0)));
            banks.put("AlKharid", new Area.Rectangular(new Coordinate(3269, 3169, 0), new Coordinate(3271, 3167, 0)));
            banks.put("Menaphos", new Area.Rectangular(new Coordinate(3233, 2761, 0), new Coordinate(3239, 2755, 0)));
            banks.put("Edgeville", new Area.Rectangular(new Coordinate(3090, 3500, 0), new Coordinate(3098, 3491, 0)));
            banks.put("VarrockWest", new Area.Rectangular(new Coordinate(3181, 3446, 0), new Coordinate(3190, 3435, 0)));
            banks.put("VarrockEast", new Area.Rectangular(new Coordinate(3250, 3423, 0), new Coordinate(3257, 3418, 0)));
            banks.put("Canifis", new Area.Rectangular(new Coordinate(3508, 3483, 0), new Coordinate(3513, 3478, 0)));
            banks.put("PortPhasmatys", new Area.Rectangular(new Coordinate(3686, 3471, 0), new Coordinate(3691, 3463, 0)));
            banks.put("Lumbridge", new Area.Rectangular(new Coordinate(3214, 3258, 0), new Coordinate(3215, 3256, 0)));
            banks.put("DeepSeaBoat", new Area.Rectangular(new Coordinate(2097, 7116, 0), new Coordinate(2102, 7112, 0)));
            banks.put("FishingGuild", new Area.Rectangular(new Coordinate(2584, 3420, 0), new Coordinate(2587, 3424, 0)));
            banks.put("DeepSeaNet", new Area.Rectangular(new Coordinate(2112, 7125, 0), new Coordinate(2122, 7116, 0)));
            banks.put("ArchChest", new Area.Rectangular(new Coordinate(3362, 3397, 0), new Coordinate(3363, 3396, 0)));
            banks.put("RangeGuild", new Area.Rectangular(new Coordinate(2672, 3408, 0), new Coordinate(2676, 3403, 0)));
        }

        public static Area getBankArea(String name) {
            return banks.get(name);
        }
    }


    public static void bank(String bankName, String bankStationType, String interactionType, boolean logMovement) {
        Area bankLocation = BankAreas.getBankArea(bankName);

        if (bankLocation == null) {
            ScriptConsole.println("Bank location '" + bankName + "' is not recognized.");
            return;
        }

        if (!bankLocation.contains(player)) {
            if (logMovement) {
                ScriptConsole.println("Moving to " + bankName + "...");
            }
            Movement.traverse(NavPath.resolve(bankLocation.getRandomWalkableCoordinate()));
            Execution.delayUntil(150000, () -> bankLocation.contains(player));
            interactWithBankStation(bankStationType, interactionType, bankLocation, logMovement);
            return;
        }
        interactWithBankStation(bankStationType, interactionType, bankLocation, logMovement);
    }

    private static void interactWithBankStation(String bankStationType, String interactionType, Area bankLocation, boolean logMovement) {
        if (logMovement) {
            ScriptConsole.println("Looking for the " + bankStationType + " in " + bankLocation + "...");
        }

            //SceneObject bankStation = SceneObjectQuery.newQuery().name(bankStationType).inside(bankLocation).results().nearest();
        List<SceneObject> bankStations = SceneObjectQuery.newQuery()
                .name(bankStationType)
                .inside(bankLocation)
                .results()
                .stream()
                .filter(station -> station.getOptions().contains(interactionType))
                .collect(Collectors.toList());

        if (!bankStations.isEmpty()) {
            SceneObject randomBankStation = bankStations.get((int) (Math.random() * bankStations.size()));
            if (randomBankStation.interact(interactionType)) {
                if (logMovement) {
                    ScriptConsole.println("Interacting with " + bankStationType + " using '" + interactionType + "' at random location...");
                }
                Execution.delayUntil(1500, () -> Backpack.isEmpty());
            } else {
                if (logMovement) {
                    ScriptConsole.println("Failed to interact with " + bankStationType + " using '" + interactionType + "' at random location.");
                }
            }
        } else {
            if (logMovement) {
                ScriptConsole.println("No suitable " + bankStationType + " available in " + bankLocation + ". Waiting before retrying...");
            }
            Execution.delay(10000);
        }
    }
}
