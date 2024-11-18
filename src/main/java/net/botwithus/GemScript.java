package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.api.game.hud.inventories.Bank;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.InventoryUpdateEvent;
import net.botwithus.rs3.game.Area;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Coordinate;
import net.botwithus.rs3.game.Item;
import net.botwithus.rs3.game.actionbar.ActionBar;
import net.botwithus.rs3.game.minimenu.MiniMenu;
import net.botwithus.rs3.game.minimenu.actions.ComponentAction;
import net.botwithus.rs3.game.movement.Movement;
import net.botwithus.rs3.game.movement.NavPath;
import net.botwithus.rs3.game.movement.TraverseEvent;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.game.vars.VarManager;
import net.botwithus.rs3.util.Regex;
import java.util.Random;
import java.util.regex.Pattern;
import net.botwithus.GemScriptUI;
import net.botwithus.rs3.script.ScriptConsole;

public class GemScript extends LoopingScript {
    private BotState botState = BotState.IDLE;
    private final Random random = new Random();
    private boolean enableCrafting = false;
    private boolean enableBankCrafting = false;
    private boolean enableFletching = false;
    private boolean useGemBag = false;
    private boolean warTeleport = false;
    private String selectedBank = "ArchChest";
    private int gemsMined = 0;
    //private long scriptStartTime = System.currentTimeMillis();
    private long scriptStartTime;

    private int startingMiningXP;
    private int startingCraftingXP;
    private int startingFletchingXP;
    
    // Constants
    private static final Pattern GEM_BAG_PATTERN = Regex.getPatternForContainingOneOf("Gem bag", "Gem bag (upgraded)");
    private static final Pattern UNCUT_GEMS = Regex.getPatternForContainingOneOf("Uncut ruby", "Uncut sapphire", "Uncut emerald", "Uncut diamond");
    private static final Pattern CUT_GEMS = Regex.getPatternForContainingOneOf("Sapphire", "Emerald", "Ruby", "Diamond");
    private Pattern UNCUT_GEM_PATTERN = Pattern.compile("Uncut (sapphire|emerald|ruby|diamond)");
    
    // Areas
    private static final Area AL_KHARID_MINE = new Area.Rectangular(new Coordinate(3297, 3311, 0), new Coordinate(3301, 3316, 0));
    private static final Area WAR_RETREAT = new Area.Rectangular(new Coordinate(3095, 10132, 0), new Coordinate(3101, 10128, 0));

    enum BotState {
        IDLE,
        RUNNING,
        MINING,
        CRAFTING,
        FLETCHING,
        BANKING,
        TRAVELING,
        WAR_TELEPORT
    }

    public GemScript(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new GemScriptUI(getConsole(), this);
        this.scriptStartTime = System.currentTimeMillis();
        setupEventHandlers();
        initializeStartingValues();
    }

     private void initializeStartingValues() {
        this.scriptStartTime = System.currentTimeMillis();
        this.startingMiningXP = Skills.MINING.getSkill().getExperience();
        this.startingCraftingXP = Skills.CRAFTING.getSkill().getExperience();
        this.startingFletchingXP = Skills.FLETCHING.getSkill().getExperience();
        
        // Log starting values
        // println("Script started. Initial XP values:");
        // println("Mining XP: " + startingMiningXP);
        // println("Crafting XP: " + startingCraftingXP);
        // println("Fletching XP: " + startingFletchingXP);
    }

    private void setupEventHandlers() {
        subscribe(InventoryUpdateEvent.class, this::handleInventoryUpdate);
    }

    private void handleInventoryUpdate(InventoryUpdateEvent event) {
        Item newItem = event.getNewItem();
        if (newItem != null && newItem.getInventoryType().getId() == 93) { // 93 is the inventory ID
            String itemName = newItem.getName();
            if (itemName != null && UNCUT_GEM_PATTERN.matcher(itemName).matches()) {
                incrementGemsMined(newItem.getStackSize());
                int gemsPerHour = calculateGemsPerHour();
                //println("Gem mined: " + itemName + " (Total: " + gemsMined + ", Per hour: " + gemsPerHour + ")");
            }
        }
    }
    private void incrementGemsMined(int count) {
        this.gemsMined += count;
    }

    private int calculateGemsPerHour() {
        long timeElapsed = System.currentTimeMillis() - scriptStartTime;
        double hoursElapsed = timeElapsed / (1000.0 * 60 * 60);
        return hoursElapsed > 0 ? (int)(gemsMined / hoursElapsed) : 0;
    }

    @Override
    public void onActivation() {
        super.onActivation();
        // Reset values if script is reactivated
        initializeStartingValues();
    }

    @Override
    public void onDeactivation() {
        unsubscribeAll();
        super.onDeactivation();
        // Reset values if script is deactivated
        //initializeStartingValues();
    }


    @Override
    public void onLoop() {
        this.loopDelay = 550;
        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == BotState.IDLE) {
            Execution.delay(random.nextLong(1750, 3000));
            return;
        }

        switch (botState) {
            case IDLE -> {
                ScriptConsole.println("IDLE");
                Execution.delay(random.nextLong(1000, 3000));
            }
            case RUNNING -> {
                //ScriptConsole.println("RUNNING");
                if(Backpack.isFull()) {
                    botState = BotState.BANKING;
                }
                else {
                    botState = BotState.TRAVELING;
                }
            }
            case MINING -> {
                //ScriptConsole.println("MINING");
                Execution.delay(handleMining(player));
            }
            case CRAFTING -> {
                //ScriptConsole.println("CRAFTING");
                //Execution.delay(GemProcessing.handleCrafting(player, enableFletching));
                Execution.delay(handleCrafting(player));
            }
            case FLETCHING -> {
                //ScriptConsole.println("FLETCHING");
                Execution.delay(handleFletching(player));
            }
            case BANKING -> {
                //ScriptConsole.println("BANKING");
                Execution.delay(handleBanking(player));
            }
            case TRAVELING -> {
                //ScriptConsole.println("TRAVELING");
                Execution.delay(handleTravel(player));
            }
            case WAR_TELEPORT -> {
                //ScriptConsole.println("WAR TELEPORT");
                Execution.delay(handleWarTeleport(player));
            }
        }
    }

    private long handleMining(LocalPlayer player) {
        if (player.isMoving()) {
            return random.nextLong(1500, 2250);
        }

        if (Backpack.isFull()) {

        if (useGemBag && Backpack.contains(GEM_BAG_PATTERN)) {
            long gemBagResult = GemBagHandler.handleGemBag(player);
            if (gemBagResult == -1) {
                if (enableCrafting) {
                    println("Backpack full, switching to crafting");
                    botState = BotState.CRAFTING;
                    return random.nextLong(300, 500); // Reduced delay for state transition
                } else {
                    println("Backpack full, switching to banking");
                    botState = warTeleport ? BotState.WAR_TELEPORT : BotState.BANKING;
                    return random.nextLong(300, 500);
                }
            }
            return gemBagResult;
        } else {
            if (enableCrafting) {
                println("Backpack full, switching to crafting");
                botState = BotState.CRAFTING;
                return random.nextLong(300, 500);
            } else {
                println("Backpack full, switching to banking");
                botState = warTeleport ? BotState.WAR_TELEPORT : BotState.BANKING;
                return random.nextLong(300, 500);
            }
        }
    }

        return GemMining.mineGems(player);
    }

    private long handleCrafting(LocalPlayer player) {
        GemProcessing.ProcessingResult result = GemProcessing.handleCrafting(player, enableFletching);
        
        if (result.shouldMine) {
            println("Switching to mining state");
            botState = BotState.MINING;
        } else if (enableFletching && Backpack.contains(CUT_GEMS)) {
            println("Switching to fletching state");
            botState = BotState.FLETCHING;
        }

        return result.delay;
    }

    private long handleFletching(LocalPlayer player) {
        long result = GemProcessing.handleFletching(player);
        if (result == -1) {
            // If we can't fletch anymore, check if we should go back to crafting or mining
            if (Backpack.contains(Pattern.compile("Uncut.*"))) {
                botState = BotState.CRAFTING;
            } else {
                botState = BotState.MINING;
            }
            return random.nextLong(500, 750);
        }
        return result;
    }

    private long handleBanking(LocalPlayer player) {
        if (!Bank.isOpen()) {
            Area bankArea = BankAPI.BankAreas.getBankArea(selectedBank);
            
            if (bankArea == null) {
                println("Invalid bank selection, defaulting to ArchChest");
                bankArea = BankAPI.BankAreas.getBankArea("ArchChest");
            }
            
            if (!warTeleport && !bankArea.contains(player)) {
                return handleBankTravel(player, bankArea);
            }
            
            if (warTeleport && player.getCoordinate().distanceTo(WAR_RETREAT.getRandomWalkableCoordinate()) < 20) {
                SceneObject bankObj = SceneObjectQuery.newQuery()
                    .name("Bank chest")
                    .results()
                    .nearest();
                    
                if (bankObj != null && bankObj.interact("Bank")) {
                    Execution.delayUntil(5000, Bank::isOpen);
                }
            } else if(BankAPI.BankAreas.getBankArea("ArchChest").contains(player)) 
            {
                BankAPI.bank(selectedBank, "Bank chest", "Use", false);
            }
            else {
                BankAPI.bank(selectedBank, "Bank booth", "Bank", false);

            }

            
            return random.nextLong(750, 1500);
        }
        

        // Handle gem bag emptying if needed
        if (useGemBag && Backpack.contains(GEM_BAG_PATTERN)) {
            Item gemBag = InventoryItemQuery.newQuery(93)
                .name(GEM_BAG_PATTERN)
                .results()
                .first();

            if (gemBag != null && !GemBagHandler.isGemBagEmpty(gemBag)) {
                Component emptyOption = ComponentQuery.newQuery(517)
                    .componentIndex(15)
                    .option("Empty")
                    .results()
                    .first();

                if (emptyOption != null) {
                    println("Emptying " + (GemBagHandler.isGemBagUpgraded(gemBag) ? "upgraded" : "regular") + " gem bag");
                    emptyOption.interact("Empty");
                    return random.nextLong(750, 1250);
                }
            }
        }

        // Handle regular banking
        Bank.depositAllExcept(18338,31455);

        // Determine next state and withdraw items
        if (enableBankCrafting) {
            Bank.withdraw(UNCUT_GEMS, 1);
            botState = BotState.CRAFTING;
        } else {
            botState = BotState.MINING;
        }

        botState = BotState.RUNNING;

        return random.nextLong(750, 1500);
    }

    private long handleBankTravel(LocalPlayer player, Area bankArea) {
        if (!bankArea.contains(player)) {
            println("Traveling to " + selectedBank + " bank");
            moveTo(bankArea.getRandomWalkableCoordinate());
            return random.nextLong(1000, 2000);
        }
        return random.nextLong(500, 1000);
    }

    private long handleTravel(LocalPlayer player) {
        if (!AL_KHARID_MINE.contains(player)) {
            moveTo(AL_KHARID_MINE.getRandomWalkableCoordinate());
            return random.nextLong(600, 1200);
        }
        botState = BotState.MINING;
        return random.nextLong(500, 950);
    }

    private long handleWarTeleport(LocalPlayer player) {
        if (!WAR_RETREAT.contains(player)) {
            ActionBar.useAbility("War's Retreat Teleport");
            Execution.delayUntil(5000, () -> WAR_RETREAT.contains(player));
        }
        botState = BotState.BANKING;
        return random.nextLong(750, 1250);
    }

    

        static boolean moveTo(Coordinate location) {
        ScriptConsole.println("moveTo");
        LocalPlayer player = Client.getLocalPlayer();

        if (location.distanceTo(player.getCoordinate()) < 4) {
            ScriptConsole.println("moveTo | Already at the target location.");
            return true;
        }


        ScriptConsole.println("moveTo | Traversing to location: " + location);
        NavPath path = NavPath.resolve(location).interrupt(event -> (VarManager.getVarbitValue(21222) == 1));
        TraverseEvent.State moveState = Movement.traverse(path);

        switch (moveState) {
            case INTERRUPTED -> {
                ScriptConsole.println("moveTo | Return false.");
                return false;
            }
            case FINISHED -> {
                ScriptConsole.println("moveTo | Successfully moved to the area.");
                return true;
            }
            case NO_PATH -> {
                ScriptConsole.println("moveTo | Path failed: " + moveState.toString());
                return false;
            }
            case FAILED -> {
                ScriptConsole.println("moveTo | Path state: " + moveState.toString());
                ScriptConsole.println("moveTo | No path found or movement failed.");
                return false;
            }
            default -> {
                ScriptConsole.println("moveTo | Unexpected state: " + moveState.toString());
                return false;
            }
        }
    }


    // Getters and setters
    public BotState getBotState() { return botState; }
    public void setBotState(BotState botState) { this.botState = botState; }
    public boolean isEnableCrafting() { return enableCrafting; }
    public void setEnableCrafting(boolean enableCrafting) { this.enableCrafting = enableCrafting; }
    public boolean isEnableBankCrafting() { return enableBankCrafting; }
    public void setEnableBankCrafting(boolean enableBankCrafting) { this.enableBankCrafting = enableBankCrafting; }
    public boolean isEnableFletching() { return enableFletching; }
    public void setEnableFletching(boolean enableFletching) { this.enableFletching = enableFletching; }
    public boolean isUseGemBag() { return useGemBag; }
    public void setUseGemBag(boolean useGemBag) { this.useGemBag = useGemBag; }
    public boolean isWarTeleport() { return warTeleport; }
    public void setWarTeleport(boolean warTeleport) { this.warTeleport = warTeleport; }
    public String getSelectedBank() { return selectedBank; }
    public void setSelectedBank(String selectedBank) { this.selectedBank = selectedBank; }
    public int getGemsMined() { return gemsMined; }
    public void incrementGemsMined() { this.gemsMined++; }
    public long getScriptStartTime() { return scriptStartTime; }

     // Getters for XP values
     public int getStartingMiningXP() { return startingMiningXP; }
     public int getStartingCraftingXP() { return startingCraftingXP; }
     public int getStartingFletchingXP() { return startingFletchingXP; }
    
}