package io.github.thebusybiscuit.sensibletoolbox.items;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

public class AdvancedMoistureChecker extends MoistureChecker {

    public AdvancedMoistureChecker() {}

    public AdvancedMoistureChecker(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public String getItemName() {
        return "高级成熟度检查器";
    }

    @Override
    public Recipe getMainRecipe() {
        MoistureChecker mc = new MoistureChecker();
        registerCustomIngredients(mc);
        ShapelessRecipe recipe = new ShapelessRecipe(getKey(), toItemStack());
        recipe.addIngredient(mc.getMaterial());
        recipe.addIngredient(Material.DIAMOND);
        return recipe;
    }

    @Override
    protected int getRadius() {
        return 2;
    }
}
