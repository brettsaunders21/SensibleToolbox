package me.desht.sensibletoolbox.recipes;

import me.desht.dhutils.Debugger;
import me.desht.sensibletoolbox.api.STBItem;
import me.desht.sensibletoolbox.blocks.machines.BaseSTBMachine;
import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CustomRecipeManager {
    // maps a STB item ID to the custom recipes that STB item knows about
    private static final Map<String, CustomRecipeCollection> map = new HashMap<String, CustomRecipeCollection>();
    // maps an item to all the custom recipes for it
    private static final Map<ItemStack, List<CustomRecipe>> reverseMap = new HashMap<ItemStack, List<CustomRecipe>>();
    private static CustomRecipeManager instance;

    public static synchronized CustomRecipeManager getManager() {
        if (instance == null) {
            instance = new CustomRecipeManager();
        }
        return instance;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void addCustomRecipe(CustomRecipe recipe) {
        if (!map.containsKey(recipe.getProcessorID())) {
            map.put(recipe.getProcessorID(), new CustomRecipeCollection());
        }
        CustomRecipeCollection collection = map.get(recipe.getProcessorID());
        collection.addCustomRecipe(recipe);

        ItemStack result = makeSingle(recipe.getResult());
        if (!reverseMap.containsKey(result)) {
            reverseMap.put(result, new ArrayList<CustomRecipe>());
        }
        reverseMap.get(result).add(recipe);
    }

    public List<CustomRecipe> getRecipesFor(ItemStack result) {
        List<CustomRecipe> res = reverseMap.get(makeSingle(result));
        return res == null ? new ArrayList<CustomRecipe>() : new ArrayList<CustomRecipe>(res);
    }

    public ProcessingResult getRecipe(BaseSTBMachine machine, ItemStack ingredient) {
        CustomRecipeCollection collection = map.get(machine.getItemTypeID());
        return collection == null ? null : collection.get(makeSingle(ingredient));
    }

    public boolean hasRecipe(BaseSTBMachine machine, ItemStack item) {
        return map.containsKey(machine.getItemTypeID()) && map.get(machine.getItemTypeID()).hasRecipe(item);
    }

    public Set<ItemStack> getAllResults() {
        return reverseMap.keySet();
    }

    public static ItemStack makeSingle(ItemStack stack) {
        if (stack.getAmount() == 1) {
            return stack;
        } else {
            ItemStack stack2 = stack.clone();
            stack2.setAmount(1);
            return stack2;
        }
    }

    /**
     * Validate that the given item stack is actually smeltable via a custom STB smelting recipe.
     * E.g. this will prevent the smelting of plain gunpowder as if it were Iron Dust, even though
     * Iron Dust uses gunpowder for its material representation.
     *
     * @param stack the stack to check
     * @return true if this item is smeltable
     */
    public static boolean validateCustomSmelt(ItemStack stack) {
        Class<? extends STBItem> klass = BaseSTBItem.getCustomSmelt(stack);
        if (klass != null) {
            STBItem item = BaseSTBItem.getItemFromItemStack(stack);
            if (!klass.isInstance(item)) {
                Debugger.getInstance().debug("stopped smelting of vanilla item: " + stack);
                return false;
            }
        }
        return true;
    }
}
