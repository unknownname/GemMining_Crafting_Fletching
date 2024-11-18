package net.botwithus;

import java.util.HashMap;
import java.util.Map;

public class GemConstants {
    public static final int UNCUT_SAPPHIRE = 1623;
    public static final int UNCUT_EMERALD = 1621;
    public static final int UNCUT_RUBY = 1619;
    public static final int UNCUT_DIAMOND = 1617;
    
    public static final int SAPPHIRE = 1607;
    public static final int EMERALD = 1605;
    public static final int RUBY = 1603;
    public static final int DIAMOND = 1601;

     // Bolt tip IDs
     public static final int SAPPHIRE_BOLT_TIPS = 9189;
     public static final int EMERALD_BOLT_TIPS = 9190;
     public static final int RUBY_BOLT_TIPS = 9191;
     public static final int DIAMOND_BOLT_TIPS = 9192;
    
    // Crafting Requirements
    public static final Map<Integer, Integer> CRAFTING_REQUIREMENTS = new HashMap<>();
    static {
        CRAFTING_REQUIREMENTS.put(UNCUT_SAPPHIRE, 20);
        CRAFTING_REQUIREMENTS.put(UNCUT_EMERALD, 27);
        CRAFTING_REQUIREMENTS.put(UNCUT_RUBY, 34);
        CRAFTING_REQUIREMENTS.put(UNCUT_DIAMOND, 43);
    }
    
    // Interface IDs
    public static final int CRAFTING_INTERFACE = 1370;
    public static final int BANK_INTERFACE = 517;
    public static final int INVENTORY_INTERFACE = 1473;
    
    // Mining Progress Headbar ID
    public static final int MINING_HEADBAR_ID = 5;

}
