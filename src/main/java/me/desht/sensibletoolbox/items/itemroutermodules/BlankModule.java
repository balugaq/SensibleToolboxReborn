package me.desht.sensibletoolbox.items.itemroutermodules;

import me.desht.sensibletoolbox.items.BaseSTBItem;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

public class BlankModule extends BaseSTBItem {
	private static final MaterialData md = new MaterialData(Material.PAPER);

	public BlankModule() {
	}

	public BlankModule(ConfigurationSection conf) {
//		super(conf);
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Blank Item Router Module";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Used for crafting active", " Item Router Modules "};
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(8));
		recipe.shape("PPP", "PRP", "PBP");
		recipe.setIngredient('P', Material.PAPER);
		recipe.setIngredient('R', Material.REDSTONE);
		Dye d = new Dye();
		d.setColor(DyeColor.BLUE);
		recipe.setIngredient('B', d);
		return recipe;
	}

	@Override
	public boolean isIngredientFor(ItemStack result) {
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(result);
		return item != null && item instanceof ItemRouterModule;
	}
}
