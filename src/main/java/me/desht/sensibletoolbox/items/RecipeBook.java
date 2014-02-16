package me.desht.sensibletoolbox.items;

import me.desht.dhutils.ItemNames;
import me.desht.dhutils.LogUtils;
import me.desht.sensibletoolbox.gui.ButtonGadget;
import me.desht.sensibletoolbox.gui.InventoryGUI;
import me.desht.sensibletoolbox.recipes.CustomRecipe;
import me.desht.sensibletoolbox.recipes.CustomRecipeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.*;

public class RecipeBook extends BaseSTBItem {
	private static final MaterialData md = new MaterialData(Material.BOOK);
	private static final int ITEMS_PER_PAGE = 45;
	private static final List<ItemStack> fullItemList = new ArrayList<ItemStack>();
	private static final Map<ItemStack,Integer> itemListPos = new HashMap<ItemStack, Integer>();
	private static final ItemStack SHAPED_ICON = new ItemStack(Material.WORKBENCH);
	private static final ItemStack SHAPELESS_ICON = new ItemStack(Material.WORKBENCH);
	private static final ItemStack FURNACE_ICON = new ItemStack(Material.BURNING_FURNACE);
	private static final ItemStack GO_BACK_TEXTURE = new ItemStack(Material.IRON_DOOR);
	private static final ItemStack GO_BACK_TEXTURE_2 = new ItemStack(Material.WOOD_DOOR);
	private static final ItemStack WEB_TEXTURE = new ItemStack(Material.WEB);
	private static final int[] RECIPE_SLOTS = new int[] { 10, 11, 12, 19, 20, 21, 28, 29, 30};
	public static final int TYPE_SLOT = 23;
	public static final int RESULT_SLOT = 25;
	public static final int PAGE_LABEL_SLOT = 45;
	public static final int FILTER_BUTTON_SLOT = 46;
	private int page;
	private int viewingItem;
	private int recipeNumber;
	private String filter;
	private List<ItemStack> filteredItems;
	private InventoryGUI gui;
	private final Deque<ItemAndRecipeNumber> trail = new ArrayDeque<ItemAndRecipeNumber>();

	static {
		setLabel(SHAPED_ICON, "Shaped Recipe");
		setLabel(SHAPELESS_ICON, "Shapeless Recipe");
		setLabel(FURNACE_ICON, "Smelting Recipe");
	}

	private static void setLabel(ItemStack stack, String label) {
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + label);
		stack.setItemMeta(meta);
	}
	public RecipeBook() {
		page = 0;
		viewingItem = -1;
		recipeNumber = 0;
		filter = "";
		filteredItems = fullItemList;
	}

	public RecipeBook(ConfigurationSection conf) {
		page = conf.getInt("page");
		viewingItem = conf.getInt("viewingItem");
		recipeNumber = conf.getInt("recipeNumber");
		filter = conf.getString("filter", "");
		buildFilteredList();
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("page", page);
		conf.set("viewingItem", viewingItem);
		conf.set("recipeNumber", recipeNumber);
		conf.set("filter", filter);
		return conf;
	}

	public static void buildRecipes() {
		Iterator<Recipe> iter = Bukkit.recipeIterator();
		Set<ItemStack> itemSet = new HashSet<ItemStack>();
		while (iter.hasNext()) {
			Recipe recipe = iter.next();
			ItemStack stack = recipe.getResult().clone();
			stack.setAmount(1);
			itemSet.add(stack);
		}
		CustomRecipeManager crm = CustomRecipeManager.getManager();
		for (ItemStack stack : crm.getAllResults()) {
			itemSet.add(stack);
		}
		fullItemList.addAll(itemSet);
		Collections.sort(fullItemList, new StackComparator());
		for (int i = 0; i < fullItemList.size(); i++) {
			itemListPos.put(fullItemList.get(i), i);
		}
	}

	public void buildFilteredList() {
		if (filter != null && !filter.isEmpty()) {
			String f = filter.toLowerCase();
			filteredItems = new ArrayList<ItemStack>();
			for (ItemStack stack : fullItemList) {
				if (ItemNames.lookup(stack).toLowerCase().contains(f)) {
					filteredItems.add(stack);
				}
			}
			System.out.println("built filtered list: " + filteredItems.size() + " items");
		} else {
			filteredItems = fullItemList;
		}
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	@Override
	public MaterialData getMaterialData() {
		return md;
	}

	@Override
	public String getItemName() {
		return "Recipe Book";
	}

	@Override
	public String[] getLore() {
		return new String[] { "Allows browsing of all", "known recipes" };
	}

	@Override
	public Recipe getRecipe() {
		ShapelessRecipe recipe = new ShapelessRecipe(toItemStack(1));
		recipe.addIngredient(Material.BOOK);
		recipe.addIngredient(Material.WORKBENCH);
		return recipe;
	}

	@Override
	public void onInteractItem(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			openBook(event.getPlayer());
			event.setCancelled(true);
		}
	}

	public void openBook(Player player) {
		gui = new InventoryGUI(player, this, 54, "Recipe Book");
		buildFilteredList();
		if (viewingItem < 0) {
			drawItemsPage();
		} else {
			drawRecipePage();
		}
		gui.show(player);
	}

	@Override
	public boolean onSlotClick(int slot, ClickType click, ItemStack inSlot, ItemStack onCursor) {
		if (viewingItem == -1) {
			// switch to viewing the item's recipe
			if (itemListPos.containsKey(inSlot)) {
				viewingItem = itemListPos.get(inSlot);
			} else {
				LogUtils.warning("could not find item " + inSlot + " in the recipe list!");
			}
			recipeNumber = 0;
			System.out.println("viewingItem now = " + viewingItem + " - " + fullItemList.get(viewingItem));
			drawRecipePage();
		} else {
			// in the recipe view - clicking an ingredient?
			if (gui.getSlotType(slot) == InventoryGUI.SlotType.ITEM && slot != RESULT_SLOT) {
				if (itemListPos.containsKey(inSlot)) {
					trail.push(new ItemAndRecipeNumber(viewingItem, recipeNumber));
					System.out.println("add " + viewingItem + "," + recipeNumber + " to trail - " + trail.size());
					viewingItem = itemListPos.get(inSlot);
					recipeNumber = 0;
					drawRecipePage();
				}
			}
		}
		return false;
	}

	private void showShapedRecipe(ShapedRecipe recipe) {
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(recipe.getResult());
		String[] shape = recipe.getShape();
		Map<Character,ItemStack> map = recipe.getIngredientMap();
		for (int i = 0; i < shape.length; i++) {
			for (int j = 0; j < shape[i].length(); j++) {
				char c = shape[i].charAt(j);
				int slot = 10 + i * 9 + j;
				ItemStack ingredient = getIngredient(item, map.get(c));
				gui.getInventory().setItem(slot, ingredient);
			}
		}
		gui.getInventory().setItem(TYPE_SLOT, SHAPED_ICON);
	}

	private void showShapelessRecipe(ShapelessRecipe recipe) {
		BaseSTBItem item = BaseSTBItem.getItemFromItemStack(recipe.getResult());
		List<ItemStack> ingredients = recipe.getIngredientList();
		for (int i = 0; i < ingredients.size(); i++) {
			ItemStack ingredient = getIngredient(item, ingredients.get(i));
			gui.getInventory().setItem(RECIPE_SLOTS[i], ingredient);
		}
		gui.getInventory().setItem(TYPE_SLOT, SHAPELESS_ICON);
	}

	private void showFurnaceRecipe(FurnaceRecipe recipe) {
		ItemStack ingredient = getSmeltingIngredient(recipe.getInput());
		gui.getInventory().setItem(RECIPE_SLOTS[4], ingredient); // 4 is the middle of the 9 item slots
		gui.getInventory().setItem(TYPE_SLOT, FURNACE_ICON);
	}

	private void showCustomRecipe(CustomRecipe recipe) {
		gui.getInventory().setItem(RESULT_SLOT, recipe.getResult());
		BaseSTBItem item = BaseSTBItem.getItemById(recipe.getItemId());
		gui.getInventory().setItem(TYPE_SLOT, item.toItemStack(1));
		gui.getInventory().setItem(RECIPE_SLOTS[4], recipe.getIngredient()); // 4 is the middle of the 9 item slots
	}

	private ItemStack getIngredient(BaseSTBItem item, ItemStack stack) {
		if (stack == null) {
			return null;
		}
		if (item != null) {
			Class<? extends BaseSTBItem> c = item.getCraftingRestriction(stack.getType());
			if (c != null) {
				try {
					BaseSTBItem item2 = c.getDeclaredConstructor().newInstance();
					return item2.toItemStack(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (stack.getDurability() == 32767) {
			ItemStack stack2 = stack.clone();
			stack2.setDurability((short)0);
			return stack2;
		} else {
			return stack;
		}
	}

	private ItemStack getSmeltingIngredient(ItemStack stack) {
		Class<? extends BaseSTBItem> c = BaseSTBItem.getCustomSmelt(stack.getType());
		if (c != null) {
			try {
				BaseSTBItem item2 = c.getDeclaredConstructor().newInstance();
				return item2.toItemStack(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (stack.getDurability() == 32767) {
			ItemStack stack2 = stack.clone();
			stack2.setDurability((short)0);
			return stack2;
		} else {
			return stack;
		}
	}

	@Override
	public void onGUIClosed(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			InventoryGUI gui = InventoryGUI.getOpenGUI(player);
			if (gui != null) {
				player.setItemInHand(toItemStack(1));
			} else {
				LogUtils.warning("Player " + player + " should be holding recipe book, but is not!");
			}
		}
		gui = null;
	}

	private void drawRecipePage() {
		final ItemStack result = fullItemList.get(viewingItem);
		final List<Recipe> recipes = new ArrayList<Recipe>();

		// the isSimilar() checks are necessary to ensure we don't pick up recipes for items
		// of the same material but with different item meta information...
		for (Recipe recipe : Bukkit.getRecipesFor(result)) {
			if (recipe.getResult().isSimilar(result)) {
				recipes.add(recipe);
			}
		}
		for (CustomRecipe customRecipe : CustomRecipeManager.getManager().getRecipesFor(result)) {
			if (customRecipe.getResult().isSimilar(result)) {
				recipes.add(customRecipe);
			}
		}

		for (int slot = 0; slot < 54; slot++) {
			gui.setSlotType(slot, InventoryGUI.SlotType.BACKGROUND);
		}
		for (int slot : RECIPE_SLOTS) {
			gui.setSlotType(slot, InventoryGUI.SlotType.ITEM);
		}
		gui.setSlotType(RESULT_SLOT, InventoryGUI.SlotType.ITEM);

		final int nRecipes = recipes.size();
		if (nRecipes == 0) {
			return;
		}
		if (recipeNumber >= nRecipes) {
			recipeNumber = nRecipes - 1;
		} else if (recipeNumber < 0) {
			recipeNumber = 0;
		}

		Recipe recipe = recipes.get(recipeNumber);
		gui.getInventory().setItem(RESULT_SLOT, recipe.getResult());
		if (recipe instanceof FurnaceRecipe) {
			showFurnaceRecipe((FurnaceRecipe) recipe);
		} else if (recipe instanceof ShapedRecipe) {
			showShapedRecipe((ShapedRecipe) recipe);
		} else if (recipe instanceof ShapelessRecipe) {
			showShapelessRecipe((ShapelessRecipe) recipe);
		} else if (recipe instanceof CustomRecipe) {
			showCustomRecipe((CustomRecipe) recipe);
		}

		if (nRecipes > 1) {
			gui.addGadget(new ButtonGadget(gui, "< Prev Recipe", new String[0], null, new Runnable() {
				@Override
				public void run() {
					recipeNumber--;
					if (recipeNumber < 0) recipeNumber = nRecipes - 1;
					drawRecipePage();
				}
			}), 18);
			gui.addGadget(new ButtonGadget(gui, "Next Recipe >", new String[0], null, new Runnable() {
				@Override
				public void run() {
					recipeNumber++;
					if (recipeNumber >= nRecipes) recipeNumber = 0;
					drawRecipePage();
				}
			}), 26);
		}
		gui.addGadget(new ButtonGadget(gui, "< Back to Item List", new String[0], GO_BACK_TEXTURE, new Runnable() {
			@Override
			public void run() {
				trail.clear();
				viewingItem = -1;
				drawItemsPage();
			}
		}), 53);
		if (!trail.isEmpty()) {
			gui.addGadget(new ButtonGadget(gui, "< Back to Last Recipe", new String[0], GO_BACK_TEXTURE_2, new Runnable() {
				@Override
				public void run() {
					ItemAndRecipeNumber ir = trail.pop();
					viewingItem = ir.item;
					recipeNumber = ir.recipe;
					drawRecipePage();
				}
			}), 52);
		}
		ItemStack pageStack = new ItemStack(Material.PAPER, recipeNumber + 1);
		gui.addLabel("Recipe " + (recipeNumber + 1) + "/" + nRecipes, 45, pageStack);
	}

	private void drawItemsPage() {
		final int totalPages = (filteredItems.size() / ITEMS_PER_PAGE) + 1;
		page = Math.min(page, totalPages - 1);
		int start = page * ITEMS_PER_PAGE;
		for (int i = start, slot = 0; i < start + ITEMS_PER_PAGE; i++, slot++) {
			if (i < filteredItems.size()) {
				gui.setSlotType(slot, InventoryGUI.SlotType.ITEM);
				gui.getInventory().setItem(slot, filteredItems.get(i));
			} else {
				gui.setSlotType(slot, InventoryGUI.SlotType.BACKGROUND);
			}
		}
		gui.addGadget(new ButtonGadget(gui, "< Prev Page", new String[0], null, new Runnable() {
			@Override
			public void run() {
				page--;
				if (page < 0) page = totalPages - 1;
				drawItemsPage();
			}
		}), 52);
		gui.addGadget(new ButtonGadget(gui, "Next Page >", new String[0], null, new Runnable() {
			@Override
			public void run() {
				page++;
				if (page >= totalPages) page = 0;
				drawItemsPage();
			}
		}), 53);
		if (filter != null && !filter.isEmpty()) {
			gui.addGadget(new ButtonGadget(gui, "Filter:" + ChatColor.YELLOW + " " + filter,
					new String[] { "Click to clear filter "}, WEB_TEXTURE, new Runnable() {
				@Override
				public void run() {
					filter = "";
					filteredItems = fullItemList;
					drawItemsPage();
				}
			}), FILTER_BUTTON_SLOT);
		} else {
			gui.setSlotType(FILTER_BUTTON_SLOT, InventoryGUI.SlotType.BACKGROUND);
		}
		ItemStack pageStack = new ItemStack(Material.PAPER, page + 1);
		gui.addLabel("Page " + (page + 1) + "/" + totalPages, PAGE_LABEL_SLOT, pageStack);
	}

	private static class StackComparator implements Comparator<ItemStack> {
		@Override
		public int compare(ItemStack o1, ItemStack o2) {
			return ChatColor.stripColor(ItemNames.lookup(o1)).compareTo(ChatColor.stripColor(ItemNames.lookup(o2)));
		}
	}

	private class ItemAndRecipeNumber {
		private final int item;
		private final int recipe;
		private ItemAndRecipeNumber(int item, int recipe) {
			this.item = item;
			this.recipe = recipe;
		}
	}
}
