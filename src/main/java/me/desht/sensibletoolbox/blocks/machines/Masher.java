package me.desht.sensibletoolbox.blocks.machines;

import me.desht.dhutils.ParticleEffect;
import me.desht.sensibletoolbox.SensibleToolboxPlugin;
import me.desht.sensibletoolbox.items.GoldDust;
import me.desht.sensibletoolbox.items.IronDust;
import me.desht.sensibletoolbox.recipes.CustomRecipe;
import me.desht.sensibletoolbox.recipes.CustomRecipeCollection;
import me.desht.sensibletoolbox.recipes.CustomRecipeManager;
import me.desht.sensibletoolbox.recipes.ProcessingResult;
import me.desht.sensibletoolbox.util.STBUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

public class Masher extends AbstractIOMachine {
	private static final MaterialData md = STBUtil.makeColouredMaterial(Material.STAINED_CLAY, DyeColor.GREEN);

	public Masher() {
	}

	public Masher(ConfigurationSection conf) {
		super(conf);
	}

	@Override
	public void addCustomRecipes(CustomRecipeManager crm) {
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.COBBLESTONE), new ItemStack(Material.SAND), 120));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.COBBLESTONE), new ItemStack(Material.SAND), 120));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.GRAVEL), new ItemStack(Material.SAND), 80));
		Dye white = new Dye();
		white.setColor(DyeColor.WHITE);
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.BONE), white.toItemStack(5), 40));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.BLAZE_ROD), new ItemStack(Material.BLAZE_POWDER, 4), 80));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.COAL_ORE), new ItemStack(Material.COAL, 2), 100));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.REDSTONE_ORE), new ItemStack(Material.REDSTONE, 6), 100));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.DIAMOND_ORE), new ItemStack(Material.DIAMOND, 2), 160));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.IRON_ORE), new IronDust().toItemStack(2), 120));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.GOLD_ORE), new GoldDust().toItemStack(2), 80));
		crm.addCustomRecipe(new CustomRecipe(this, new ItemStack(Material.WOOL), new ItemStack(Material.STRING, 4), 60));
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Masher";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Grinds ores and other ", "resources into dusts" };
	}

	@Override
	public Recipe getRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(toItemStack(1));
		recipe.shape("FFF", "SIS", "RGR");
		recipe.setIngredient('F', Material.FLINT);
		recipe.setIngredient('S', Material.STONE);
		recipe.setIngredient('I', Material.IRON_BLOCK);
		recipe.setIngredient('R' ,Material.REDSTONE);
		recipe.setIngredient('G', Material.GOLD_INGOT);
		return recipe;
	}

	@Override
	public int[] getInputSlots() {
		return new int[] { 10 };
	}

	@Override
	public int[] getOutputSlots() {
		return new int[] { 14, 15 };
	}

	@Override
	public int[] getUpgradeSlots() {
		return new int[] { 41, 42, 43, 44 };
	}

	@Override
	public int getUpgradeLabelSlot() {
		return 40;
	}

	@Override
	public int getEnergyCellSlot() {
		return 36;
	}

	@Override
	public int getChargeDirectionSlot() {
		return 37;
	}

	@Override
	public int getInventoryGUISize() {
		return 45;
	}

	@Override
	public int getMaxCharge() {
		return 1000;
	}

	@Override
	public int getChargeRate() {
		return 20;
	}

//	@Override
//	public boolean acceptsItemType(ItemStack item) {
//		return CustomRecipeManager.getManager().hasRecipe(this, item);
//	}
//
//	@Override
//	protected ProcessingResult getCustomRecipeFor(ItemStack stack) {
//		return CustomRecipeManager.getManager().getRecipe(this, stack);
//	}

	@Override
	public int getProgressItemSlot() {
		return 12;
	}

	@Override
	public int getProgressCounterSlot() {
		return 3;
	}

	@Override
	public Material getProgressIcon() {
		return Material.GOLD_PICKAXE;
	}

	@Override
	protected void playStartupSound() {
		getLocation().getWorld().playSound(getLocation(), Sound.HORSE_SKELETON_IDLE, 1.0f, 0.5f);
	}

	@Override
	protected void playActiveParticleEffect() {
		if (SensibleToolboxPlugin.getInstance().isProtocolLibEnabled() && getTicksLived() % 20 == 0) {
			ParticleEffect.LARGE_SMOKE.play(getLocation().add(0.5, 1.0, 0.5), 0.2f, 1.0f, 0.2f, 0.001f, 5);
		}
	}
}
