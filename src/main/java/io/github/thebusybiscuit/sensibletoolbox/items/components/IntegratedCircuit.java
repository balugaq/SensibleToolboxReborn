package io.github.thebusybiscuit.sensibletoolbox.items.components;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

public class IntegratedCircuit extends BaseSTBItem {

    public IntegratedCircuit() {}

    public IntegratedCircuit(ConfigurationSection conf) {}

    @Override
    public Material getMaterial() {
        return Material.COMPARATOR;
    }

    @Override
    public String getItemName() {
        return "集成电路";
    }

    @Override
    public String[] getLore() {
        return new String[] { "用于制作一些高级机械" };
    }

    @Override
    public Recipe getMainRecipe() {
        SimpleCircuit sc = new SimpleCircuit();
        EnergizedGoldIngot eg = new EnergizedGoldIngot();
        SiliconWafer si = new SiliconWafer();
        registerCustomIngredients(sc, eg, si);
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("SCG");
        recipe.setIngredient('C', sc.getMaterial());
        recipe.setIngredient('G', eg.getMaterial());
        recipe.setIngredient('S', si.getMaterial());
        return recipe;
    }
}
