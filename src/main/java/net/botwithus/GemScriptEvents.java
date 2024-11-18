package net.botwithus;

import net.botwithus.rs3.events.impl.InventoryUpdateEvent;
import net.botwithus.rs3.game.Item;

public class GemScriptEvents {

    private final GemScript script;

    public GemScriptEvents(GemScript script) {
        this.script = script;
        registerEvents();
    }

    private void registerEvents() {
        script.subscribe(InventoryUpdateEvent.class, this::handleInventoryUpdate);
    }

    private void handleInventoryUpdate(InventoryUpdateEvent event) {
        Item newItem = event.getNewItem();
        if (newItem != null && newItem.getInventoryType().getId() == 93) {
            String itemName = newItem.getName();
            if (itemName != null && itemName.contains("Uncut ")) {
                script.incrementGemsMined();
            }
        }
    }

}
