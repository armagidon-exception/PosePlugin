package ru.armagidon.armagidonapi.itemutils;

import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.function.Consumer;

public class ItemModifingPipeline
{
    private final LinkedList<Consumer<ItemStack>> operations = new LinkedList<>();

    public void addLast(Consumer<ItemStack> operation){
        if( operation == null ) return;
        operations.add(operation);
    }

    public void pushThrough(ItemStack item){
        for (Consumer<ItemStack> operation : operations) {
            operation.accept(item);
        }
    }
}
