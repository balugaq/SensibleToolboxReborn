package io.github.thebusybiscuit.sensibletoolbox.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

import com.google.common.base.Joiner;

import io.github.bakedlibs.dough.items.ItemUtils;
import io.github.thebusybiscuit.sensibletoolbox.SensibleToolboxPlugin;
import io.github.thebusybiscuit.sensibletoolbox.api.MinecraftVersion;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.Chargeable;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBItem;

import me.desht.dhutils.DHUtilsException;
import me.desht.dhutils.MiscUtil;
import me.desht.dhutils.blocks.BlockUtil;

/**
 * A collection of miscellaneous utility methods.
 */
public final class STBUtil {

    /**
     * The block faces directly adjacent to a block.
     */
    private static final BlockFace[] DIRECT_BLOCK_FACES = { BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.DOWN, BlockFace.SOUTH, BlockFace.WEST };

    /**
     * The block faces in the four cardinal horizontal directions.
     */
    private static final BlockFace[] MAIN_HORIZONTAL_BLOCK_FACES = new BlockFace[] { BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH };

    /**
     * The block faces in all eight compass directions, plus the block itself.
     */
    private static final BlockFace[] ALL_HORIZONTAL_BLOCK_FACES = new BlockFace[] { BlockFace.SELF, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH };

    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");

    /**
     * This is our {@link DecimalFormat} for decimal values.
     * This instance is not thread-safe!
     */
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private STBUtil() {}

    /**
     * Check if the given material is a crop which can grow and/or be harvested.
     *
     * @param m
     *            the material to check
     * @return true if the material is a crop
     */
    public static boolean isCrop(@Nonnull Material m) {
        return m == Material.WHEAT || m == Material.CARROTS || m == Material.POTATOES || m == Material.PUMPKIN_STEM || m == Material.MELON_STEM || m == Material.BEETROOTS || m == Material.SWEET_BERRY_BUSH;
    }

    /**
     * Check if the given material is any growing plant (not including leaves or trees)
     *
     * @param type
     *            the material to check
     * @return true if the material is a plant
     */
    public static boolean isPlant(@Nonnull Material type) {
        if (isCrop(type)) {
            return true;
        }

        if (Tag.SAPLINGS.isTagged(type)) {
            return true;
        }

        if (Tag.SMALL_FLOWERS.isTagged(type)) {
            return true;
        }

        if (Tag.TALL_FLOWERS.isTagged(type)) {
            return true;
        }

        switch (type) {
            case TALL_GRASS:
            case SUGAR_CANE:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case DEAD_BUSH:
            case SWEET_BERRY_BUSH:
            case CACTUS:
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * Check if the given material is a wearable (armour) item.
     *
     * @param type
     *            the material to check
     * @return true if the material is wearable
     */
    public static boolean isWearable(@Nonnull Material type) {
        switch (type) {
            case LEATHER_HELMET:
            case LEATHER_BOOTS:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case IRON_HELMET:
            case IRON_BOOTS:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case GOLDEN_HELMET:
            case GOLDEN_BOOTS:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case DIAMOND_HELMET:
            case DIAMOND_BOOTS:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the blocks horizontally surrounding the given block.
     *
     * @param b
     *            the centre block
     * @return array of all blocks around the given block, including the given block as the first element
     */
    public static Block[] getSurroundingBlocks(Block b) {
        Block[] result = new Block[ALL_HORIZONTAL_BLOCK_FACES.length];
        for (int i = 0; i < ALL_HORIZONTAL_BLOCK_FACES.length; i++) {
            result[i] = b.getRelative(ALL_HORIZONTAL_BLOCK_FACES[i]);
        }
        return result;
    }

    @Nullable
    public static Material getCropType(@Nonnull Material seedType) {
        return switch (seedType) {
            case WHEAT_SEEDS -> Material.WHEAT;
            case POTATO -> Material.POTATOES;
            case CARROT -> Material.CARROTS;
            case PUMPKIN_SEEDS -> Material.PUMPKIN_STEM;
            case MELON_SEEDS -> Material.MELON_STEM;
            case BEETROOT_SEEDS -> Material.BEETROOTS;
            default -> null;
        };
    }

    /**
     * Convenience wrapper to get the metadata value set by STB.
     *
     * @param m
     *            the metadatable object
     * @param key
     *            the metadata key
     * @return the metadata value, or null if there is none
     */
    @Nullable
    public static Object getMetadataValue(@Nonnull Metadatable m, @Nonnull String key) {
        for (MetadataValue mv : m.getMetadata(key)) {
            if (mv.getOwningPlugin() == SensibleToolboxPlugin.getInstance()) {
                return mv.value();
            }
        }

        return null;
    }

    /**
     * Convert a dye color to the nearest matching chat color.
     *
     * @param dyeColor
     *            the dye color
     * @return the nearest matching chat color
     */
    @Nonnull
    public static ChatColor dyeColorToChatColor(@Nonnull DyeColor dyeColor) {
        switch (dyeColor) {
            case BLACK:
            case GRAY:
                return ChatColor.DARK_GRAY;
            case BLUE:
                return ChatColor.DARK_BLUE;
            case BROWN:
            case ORANGE:
                return ChatColor.GOLD;
            case CYAN:
                return ChatColor.AQUA;
            case GREEN:
                return ChatColor.DARK_GREEN;
            case LIGHT_BLUE:
                return ChatColor.BLUE;
            case LIME:
                return ChatColor.GREEN;
            case MAGENTA:
            case PINK:
                return ChatColor.LIGHT_PURPLE;
            case PURPLE:
                return ChatColor.DARK_PURPLE;
            case RED:
                return ChatColor.DARK_RED;
            case LIGHT_GRAY:
                return ChatColor.GRAY;
            case WHITE:
                return ChatColor.WHITE;
            case YELLOW:
                return ChatColor.YELLOW;
            default:
                throw new IllegalArgumentException("unknown dye color" + dyeColor);
        }
    }

    /**
     * Check if the given material is colorable. Ideally the relevant MaterialData
     * should implement the Colorable interface, but Bukkit doesn't have that yet...
     *
     * @param mat
     *            the material to check
     * @return true if the material is colorable; false otherwise
     */
    public static boolean isColorable(Material mat) {
        switch (mat) {
            case WHITE_WOOL:
            case ORANGE_WOOL:
            case MAGENTA_WOOL:
            case LIGHT_BLUE_WOOL:
            case YELLOW_WOOL:
            case LIME_WOOL:
            case PINK_WOOL:
            case GRAY_WOOL:
            case LIGHT_GRAY_WOOL:
            case CYAN_WOOL:
            case PURPLE_WOOL:
            case BLUE_WOOL:
            case BROWN_WOOL:
            case GREEN_WOOL:
            case RED_WOOL:
            case BLACK_WOOL:
            case GLASS:
            case WHITE_STAINED_GLASS:
            case ORANGE_STAINED_GLASS:
            case MAGENTA_STAINED_GLASS:
            case LIGHT_BLUE_STAINED_GLASS:
            case YELLOW_STAINED_GLASS:
            case LIME_STAINED_GLASS:
            case PINK_STAINED_GLASS:
            case GRAY_STAINED_GLASS:
            case LIGHT_GRAY_STAINED_GLASS:
            case CYAN_STAINED_GLASS:
            case PURPLE_STAINED_GLASS:
            case BLUE_STAINED_GLASS:
            case BROWN_STAINED_GLASS:
            case GREEN_STAINED_GLASS:
            case RED_STAINED_GLASS:
            case BLACK_STAINED_GLASS:
            case WHITE_CARPET:
            case ORANGE_CARPET:
            case MAGENTA_CARPET:
            case LIGHT_BLUE_CARPET:
            case YELLOW_CARPET:
            case LIME_CARPET:
            case PINK_CARPET:
            case GRAY_CARPET:
            case LIGHT_GRAY_CARPET:
            case CYAN_CARPET:
            case PURPLE_CARPET:
            case BLUE_CARPET:
            case BROWN_CARPET:
            case GREEN_CARPET:
            case RED_CARPET:
            case BLACK_CARPET:
            case GLASS_PANE:
            case WHITE_STAINED_GLASS_PANE:
            case ORANGE_STAINED_GLASS_PANE:
            case MAGENTA_STAINED_GLASS_PANE:
            case LIGHT_BLUE_STAINED_GLASS_PANE:
            case YELLOW_STAINED_GLASS_PANE:
            case LIME_STAINED_GLASS_PANE:
            case PINK_STAINED_GLASS_PANE:
            case GRAY_STAINED_GLASS_PANE:
            case LIGHT_GRAY_STAINED_GLASS_PANE:
            case CYAN_STAINED_GLASS_PANE:
            case PURPLE_STAINED_GLASS_PANE:
            case BLUE_STAINED_GLASS_PANE:
            case BROWN_STAINED_GLASS_PANE:
            case GREEN_STAINED_GLASS_PANE:
            case RED_STAINED_GLASS_PANE:
            case BLACK_STAINED_GLASS_PANE:
            case TERRACOTTA:
            case WHITE_TERRACOTTA:
            case ORANGE_TERRACOTTA:
            case MAGENTA_TERRACOTTA:
            case LIGHT_BLUE_TERRACOTTA:
            case YELLOW_TERRACOTTA:
            case LIME_TERRACOTTA:
            case PINK_TERRACOTTA:
            case GRAY_TERRACOTTA:
            case LIGHT_GRAY_TERRACOTTA:
            case CYAN_TERRACOTTA:
            case PURPLE_TERRACOTTA:
            case BLUE_TERRACOTTA:
            case BROWN_TERRACOTTA:
            case GREEN_TERRACOTTA:
            case RED_TERRACOTTA:
            case BLACK_TERRACOTTA:
            case WHITE_CONCRETE:
            case ORANGE_CONCRETE:
            case MAGENTA_CONCRETE:
            case LIGHT_BLUE_CONCRETE:
            case YELLOW_CONCRETE:
            case LIME_CONCRETE:
            case PINK_CONCRETE:
            case GRAY_CONCRETE:
            case LIGHT_GRAY_CONCRETE:
            case CYAN_CONCRETE:
            case PURPLE_CONCRETE:
            case BLUE_CONCRETE:
            case BROWN_CONCRETE:
            case GREEN_CONCRETE:
            case RED_CONCRETE:
            case BLACK_CONCRETE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Given a yaw value: get the closest block face for the given yaw. The yaw value
     * is usually obtained via {@link org.bukkit.Location#getYaw()}
     *
     * @param yaw
     *            the yaw to convert
     * @return the nearest block face
     */
    @Nonnull
    public static BlockFace getFaceFromYaw(float yaw) {
        double rot = yaw % 360;

        if (rot < 0) {
            rot += 360;
        }

        if ((0 <= rot && rot < 45) || (315 <= rot && rot < 360.0)) {
            return BlockFace.SOUTH;
        } else if (45 <= rot && rot < 135) {
            return BlockFace.WEST;
        } else if (135 <= rot && rot < 225) {
            return BlockFace.NORTH;
        } else if (225 <= rot && rot < 315) {
            return BlockFace.EAST;
        } else {
            throw new IllegalArgumentException("impossible rotation: " + rot);
        }
    }

    /**
     * Get a display-formatted string for the given chargeable item or block's current
     * charge level.
     *
     * @param ch
     *            the chargeable item or block
     * @return a formatted string showing the chargeable's charge
     */
    @Nonnull
    public static String getChargeString(@Nonnull Chargeable ch) {
        double d = ch.getCharge() / ch.getMaxCharge();
        ChatColor cc;

        if (d < 0.333) {
            cc = ChatColor.RED;
        } else if (d < 0.666) {
            cc = ChatColor.GOLD;
        } else {
            cc = ChatColor.GREEN;
        }

        return ChatColor.WHITE + "\u2301 " + cc + Math.round(ch.getCharge()) + "/" + ch.getMaxCharge() + " SCU";
    }

    /**
     * Round up the given value to given nearest multiple.
     *
     * @param n
     *            the value to round up
     * @param nearestMultiple
     *            the nearest multiple to round to
     * @return the rounded value
     */
    public static int roundUp(int n, int nearestMultiple) {
        return n + nearestMultiple - 1 - (n - 1) % nearestMultiple;
    }

    /**
     * Get a display-formatted string for the given ItemStack. The item stack's metadata is
     * taken into account, as it the size of the stack.
     *
     * @param s
     *            the item stack
     * @return a formatted description of the item stack
     */
    @Nonnull
    public static String describeItemStack(@Nullable ItemStack s) {
        if (s == null) {
            return "无物品";
        }

        return s.getAmount() + " x " + ItemUtils.getItemName(s);
    }

    /**
     * Convenience method to create an item stack with defined title and lore data
     *
     * @param material
     *            the material to create the stack from
     * @param title
     *            the item's title, may be null
     * @param lore
     *            the item's lore, may be empty
     * @return a new ItemStack with the given title and lore
     */
    public static ItemStack makeStack(Material material, String title, String... lore) {
        ItemStack s = new ItemStack(material);

        if (title != null) {
            ItemMeta meta = s.getItemMeta();
            meta.setDisplayName(title);
            List<String> newLore = new ArrayList<>(lore.length);

            for (String l : lore) {
                newLore.add(BaseSTBItem.LORE_COLOR + l);
            }

            meta.setLore(newLore);
            s.setItemMeta(meta);
        }
        return s;
    }

    /**
     * Check if the given block is a water source block.
     *
     * @param block
     *            the block to check
     * @return true if the block is a water source block
     */
    public static boolean isLiquidSourceBlock(@Nonnull Block block) {
        return block.isLiquid() && block.getData() == 0;
    }

    /**
     * Check if the given block is an infinite water source. It must be a non-flowing
     * source water block with at least 2 non-flowing source water block neighbours.
     *
     * @param block
     *            the block to check
     *
     * @return true if the block is an infinite water source
     */
    public static boolean isInfiniteWaterSource(@Nonnull Block block) {
        if (isLiquidSourceBlock(block) && block.getType() != Material.LAVA) {
            int n = 0;

            for (BlockFace face : MAIN_HORIZONTAL_BLOCK_FACES) {
                Block neighbour = block.getRelative(face);

                if (isLiquidSourceBlock(neighbour)) {
                    n++;

                    if (n >= 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check if the given block can be used to fabricate items via vanilla
     * recipes.
     *
     * @param block
     *            the block to check
     * @return true if the block can be used to fabricate with, false otherwise
     */
    public static boolean canFabricateWith(@Nullable Block block) {
        return block != null && block.getType() == Material.CRAFTING_TABLE;
    }

    /**
     * Check if the given item can be used to fabricate items via vanilla
     * recipes.
     *
     * @param s
     *            the item stack to check
     * @return true if the item can be used to fabricate with, false otherwise
     */
    public static boolean canFabricateWith(@Nullable ItemStack s) {
        return s != null && s.getType() == Material.CRAFTING_TABLE;
    }

    /**
     * Send an audible alert to the given player indicating a problem of some
     * kind.
     *
     * @param p
     *            the player
     */
    public static void complain(@Nonnull Player p) {
        Preconditions.checkArgument(p != null, "Cannot complain to nobody");
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
    }

    /**
     * Send an error message to the given player, along with an audible alert.
     *
     * @param p
     *            the player
     * @param message
     *            the message text
     */
    public static void complain(Player p, String message) {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
        MiscUtil.errorMessage(p, message);
    }

    public static void complain(HumanEntity p, String message) {
        if (p instanceof Player) {
            complain((Player) p, message);
        }
    }

    /**
     * Give the items to a player, dropping any excess items on the ground.
     *
     * @param p
     *            the player
     * @param stacks
     *            one or more item stacks
     */
    public static void giveItems(HumanEntity p, ItemStack stacks) {
        Map<Integer, ItemStack> excess = p.getInventory().addItem(stacks);

        for (ItemStack s : excess.values()) {
            p.getWorld().dropItemNaturally(p.getLocation(), s);
        }
    }

    @Nonnull
    public static List<String> dumpItemStack(@Nullable ItemStack s) {
        if (s == null) {
            return Collections.emptyList();
        }

        List<String> l = new ArrayList<>();
        l.add("数量: " + s.getAmount());
        l.add("材质: " + s.getType() + ":" + s.getDurability());

        if (s.hasItemMeta()) {
            ItemMeta meta = s.getItemMeta();
            l.add("显示名字: " + meta.getDisplayName());

            if (meta.hasLore()) {
                l.add("描述: [" + Joiner.on(",").join(meta.getLore()) + "]");
            }

            if (meta.hasEnchants()) {
                for (Map.Entry<Enchantment, Integer> e : meta.getEnchants().entrySet()) {
                    l.add("附魔 " + e.getKey() + " = " + e.getValue());
                }
            }
        } else {
            l.add("没有 metadata");
        }

        return l;
    }

    /**
     * Given a string specification, try to get an ItemStack.
     * <p>
     * The spec. is of the form "material-name[:data-byte][,amount][,glow]" where
     * material-name is a valid Bukkit material name as understoood by
     * {@link Material#matchMaterial(String)}, data-byte is a numeric byte value,
     * amount is an optional item quantity, and "glow" if present indicates that
     * the item should glow if possible.
     * <p>
     * No item metadata is considered by this method.
     *
     * @param spec
     *            the specification
     * @return the return ItemStack
     * @throws DHUtilsException
     *             if the specification is invalid
     */
    public static ItemStack parseMaterialSpec(String spec) {
        if (spec == null || spec.isEmpty()) {
            return null;
        }

        String[] fields = spec.split(",");
        Material material = Material.matchMaterial(fields[0]);

        int amount = 1;
        boolean glowing = false;

        for (int i = 1; i < fields.length; i++) {
            // I went with STBUtil here, I couldn't figure out an alternative.
            if (STBUtil.isNumeric(fields[i])) {
                amount = Integer.parseInt(fields[i]);
            } else if (fields[i].equalsIgnoreCase("glow")) {
                glowing = true;
            }
        }

        ItemStack s = new ItemStack(material, amount);

        if (glowing && SensibleToolboxPlugin.getInstance().isProtocolLibEnabled()) {
            ItemGlow.setGlowing(s, true);
        }

        return s;
    }

    /**
     * Get the relative rotations of two block faces; the number of clockwise
     * 90-degree rotations from the first face to the second.
     *
     * @param face1
     *            the first face
     * @param face2
     *            the second face
     * @return the number of 90-degree rotations between the faces
     */
    public static int getFaceRotation(BlockFace face1, BlockFace face2) {
        Preconditions.checkArgument(face1.getModY() == 0 && Math.abs(face1.getModX() + face1.getModZ()) == 1, "invalid face " + face1);
        Preconditions.checkArgument(face2.getModY() == 0 && Math.abs(face2.getModX() + face2.getModZ()) == 1, "invalid face " + face2);

        if (face1 == face2) {
            return 0;
        } else if (face1 == face2.getOppositeFace()) {
            return 2;
        } else {
            return face2 == BlockUtil.getLeft(face1) ? 3 : 1;
        }
    }

    /**
     * Given a BlockFace and a rotation in units of 90 degrees, return the
     * face obtained by a clockwise rotation.
     *
     * @param face
     *            the original face
     * @param rotation
     *            number of clockwise 90-degree rotations
     * @return the rotated face
     */
    public static BlockFace getRotatedFace(BlockFace face, int rotation) {
        Preconditions.checkArgument(face.getModY() == 0 && Math.abs(face.getModX() + face.getModZ()) == 1, "invalid face " + face);

        switch (rotation) {
            case 0:
                return face;
            case 1:
                return BlockUtil.getLeft(face).getOppositeFace();
            case 2:
                return face.getOppositeFace();
            case 3:
                return BlockUtil.getLeft(face);
            default:
                throw new IllegalArgumentException("invalid rotation" + rotation);
        }
    }

    /**
     * Check if the given block can be used as cable for the purposes of
     * transferring energy (SCU).
     *
     * @param block
     *            the block to check
     * @return true if the block can be used as a cable; false otherwise
     */
    public static boolean isCable(Block block) {
        return block.getType() == Material.IRON_BARS;
    }

    /**
     * Encode the given level as a proportion of the given maximum as an item
     * durability; useful for displaying in the item's durability bar.
     *
     * @param s
     *            the item stack
     * @param val
     *            the value to encode, between 0 and <em>max</em>
     * @param max
     *            the maximum value
     */
    public static void levelToDurability(ItemStack s, int val, int max) {
        short maxDur = s.getType().getMaxDurability();
        Preconditions.checkArgument(maxDur > 0, "Item stack " + s + " does not have a durability bar!");
        Preconditions.checkArgument(val >= 0 && val <= max, "Value " + val + " out of range 0.." + max);
        float d = val / (float) max;
        short dur = (short) (maxDur * d);
        s.setDurability((short) (Math.max(1, maxDur - dur)));
    }

    /**
     * Get the data byte for various items which have a direction: ladders,
     * wall signs, chests, furnaces, droppers, hoppers, dispensers, banners.
     *
     * @param face
     *            the direction the block is facing
     * @return the data byte to use for that direction
     */
    public static byte getDirectionData(BlockFace face) {
        switch (face) {
            case SELF:
            case DOWN:
                return 0;
            case UP:
                return 1;
            case NORTH:
                return 2;
            case SOUTH:
                return 3;
            case WEST:
                return 4;
            case EAST:
                return 5;
            default:
                throw new IllegalArgumentException("invalid direction " + face);
        }
    }

    public static boolean isPotionIngredient(Material type) {
        // TODO Fix Potion Ingredient lookup
        return false;
    }

    /**
     * Get the wall sign version of the sign material e.g. OAK_SIGN -> OAK_WALL_SIGN
     *
     * @param signType
     *            The material of the sign
     * @return The wall sign version of that sign
     */
    @Nullable
    public static Material getWallSign(@Nonnull Material signType) {
        Preconditions.checkArgument(signType != null, "The Sign Type cannot be null");

        if (SensibleToolboxPlugin.getMinecraftVersion().isAtLeast(MinecraftVersion.MINECRAFT_1_16)) {
            if (signType == Material.CRIMSON_SIGN) {
                return Material.CRIMSON_WALL_SIGN;
            } else if (signType == Material.WARPED_SIGN) {
                return Material.WARPED_WALL_SIGN;
            }
        }

        switch (signType) {
            case OAK_SIGN:
                return Material.OAK_WALL_SIGN;
            case SPRUCE_SIGN:
                return Material.SPRUCE_WALL_SIGN;
            case BIRCH_SIGN:
                return Material.BIRCH_WALL_SIGN;
            case JUNGLE_SIGN:
                return Material.JUNGLE_WALL_SIGN;
            case ACACIA_SIGN:
                return Material.ACACIA_WALL_SIGN;
            case DARK_OAK_SIGN:
                return Material.DARK_OAK_WALL_SIGN;
            default:
                return null;
        }
    }

    public static boolean isDye(@Nonnull Material type) {
        return getColorFromDye(type) != null;
    }

    @Nullable
    public static DyeColor getColorFromDye(@Nonnull Material type) {
        switch (type) {
            case WHITE_DYE:
                return DyeColor.WHITE;
            case ORANGE_DYE:
                return DyeColor.ORANGE;
            case MAGENTA_DYE:
                return DyeColor.MAGENTA;
            case LIGHT_BLUE_DYE:
                return DyeColor.LIGHT_BLUE;
            case YELLOW_DYE:
                return DyeColor.YELLOW;
            case LIME_DYE:
                return DyeColor.LIME;
            case PINK_DYE:
                return DyeColor.PINK;
            case GRAY_DYE:
                return DyeColor.GRAY;
            case LIGHT_GRAY_DYE:
                return DyeColor.LIGHT_GRAY;
            case PURPLE_DYE:
                return DyeColor.PURPLE;
            case BLUE_DYE:
                return DyeColor.BLUE;
            case BROWN_DYE:
                return DyeColor.BROWN;
            case GREEN_DYE:
                return DyeColor.GREEN;
            case RED_DYE:
                return DyeColor.RED;
            case BLACK_DYE:
                return DyeColor.BLACK;
            default:
                return null;
        }
    }

    /**
     * Given a UUID, attempt to get the player name for that UUID. This will
     * only succeed if that player has previously connected to this server,
     * in which case the last known name for the UUID will be returned. If
     * the player has changed their name since last logging in, this change
     * will not be reflected in the return value for this method.
     * <p/>
     * This method does not require contacting any external servers, so is
     * safe to call from the main server thread.
     *
     * @param uuid
     *            the UUID to check
     * @return the last known player name for this UUID, if any (null if name not known)
     */
    @Nullable
    public static String getPlayerNameFromUUID(@Nonnull UUID uuid) {
        Preconditions.checkArgument(uuid != null, "UUID cannot be null!");
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    public static boolean isNumeric(@Nonnull String string) {
        return NUMBER_PATTERN.matcher(string).matches();
    }

    @Nonnull
    public static String getCompactDouble(double value) {
        if (value < 0) {
            // Negative numbers are a special case
            return '-' + getCompactDouble(-value);
        }

        if (value < 1000.0) {
            // Below 1K
            return DECIMAL_FORMAT.format(value);
        } else if (value < 1000000.0) {
            // Thousands
            return DECIMAL_FORMAT.format(value / 1000.0) + 'K';
        } else if (value < 1000000000.0) {
            // Million
            return DECIMAL_FORMAT.format(value / 1000000.0) + 'M';
        } else if (value < 1000000000000.0) {
            // Billion
            return DECIMAL_FORMAT.format(value / 1000000000.0) + 'B';
        } else if (value < 1000000000000000.0) {
            // Trillion
            return DECIMAL_FORMAT.format(value / 1000000000000.0) + 'T';
        } else {
            // Quadrillion
            return DECIMAL_FORMAT.format(value / 1000000000000000.0) + 'Q';
        }
    }

    @Nonnull
    public static BlockFace[] getDirectBlockFaces() {
        return DIRECT_BLOCK_FACES;
    }

    @Nonnull
    public static BlockFace[] getMainHorizontalFaces() {
        return MAIN_HORIZONTAL_BLOCK_FACES;
    }

    @Nonnull
    public static BlockFace[] getAllHorizontalFaces() {
        return ALL_HORIZONTAL_BLOCK_FACES;
    }

    public static String getDirectionString(BlockFace face) {
        return switch (face) {
            case NORTH -> "北";
            case SOUTH -> "南";
            case EAST -> "东";
            case WEST -> "西";
            case UP -> "上";
            case DOWN -> "下";
            default -> "未知方向";
        };
    }

}
