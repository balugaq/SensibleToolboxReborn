package io.github.thebusybiscuit.sensibletoolbox.items.itemroutermodules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;

public class VacuumModule extends DirectionalItemRouterModule {

    private static final int RADIUS = 6;
    private static final Map<UUID, List<Item>> recentItemCache = new HashMap<>();
    private static final Map<UUID, Long> cacheTime = new HashMap<>();
    public static final int CACHE_TIME = 1000;
    private static final String STB_VACUUMED = "STB_Vacuumed";

    public VacuumModule() {}

    public VacuumModule(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.BLACK_DYE;
    }

    @Override
    public String getItemName() {
        return "拾荒升级";
    }

    @Override
    public String[] getLore() {
        return new String[] { "可放入物品运输器", "捡起 " + RADIUS + " 格内的物品" };
    }

    @Override
    public Recipe getMainRecipe() {
        registerCustomIngredients(new BlankModule());
        ShapelessRecipe recipe = new ShapelessRecipe(getKey(), toItemStack());
        recipe.addIngredient(Material.PAPER);
        recipe.addIngredient(Material.HOPPER);
        recipe.addIngredient(Material.ENDER_EYE);
        return recipe;
    }

    @Nonnull
    private static List<Item> getItemEntities(@Nonnull World w) {
        // Caching the list of item entities per-world can avoid the overhead of
        // repeated getEntities() calls if there are many vacuum modules in operation.
        List<Item> list = recentItemCache.get(w.getUID());
        if (list == null) {
            list = new ArrayList<>();
            recentItemCache.put(w.getUID(), list);
            cacheTime.put(w.getUID(), 0L);
        }

        if (System.currentTimeMillis() - cacheTime.get(w.getUID()) > CACHE_TIME) {
            list.clear();
            list.addAll(w.getEntitiesByClass(Item.class));
            cacheTime.put(w.getUID(), System.currentTimeMillis());
        }

        return list;
    }

    @Override
    public boolean execute(Location l) {
        l.add(0.5, 0.5, 0.5);

        for (Entity entity : l.getWorld().getNearbyEntities(l, RADIUS, RADIUS, RADIUS, n -> n instanceof Item && n.isValid())) {
            Item item = (Item) entity;

            ItemStack onGround = item.getItemStack();
            ItemStack buffer = getItemRouter().getBufferItem();
            Location itemLoc = item.getLocation();

            if (item.getPickupDelay() <= 0 && getFilter().shouldPass(onGround) && rightDirection(itemLoc, l) && (buffer == null || buffer.isSimilar(onGround)) && STBUtil.getMetadataValue(item, STB_VACUUMED) == null) {
                double rtrY = l.getY();
                double dist = l.distanceSquared(item.getLocation());
                Vector vel = l.subtract(itemLoc).toVector().normalize().multiply(Math.min(dist * 0.06, 0.7));

                if (itemLoc.getY() < rtrY) {
                    vel.setY(vel.getY() + (rtrY - itemLoc.getY()) / 10);
                }

                item.setMetadata(STB_VACUUMED, new FixedMetadataValue(getProviderPlugin(), getItemRouter()));
                item.setVelocity(vel);

                Bukkit.getScheduler().runTaskLater(getProviderPlugin(), () -> {
                    if (item.isValid()) {
                        ItemStack newBuffer = getItemRouter().getBufferItem();
                        int toSlurp = 0;

                        if (newBuffer == null) {
                            toSlurp = onGround.getAmount();
                            getItemRouter().setBufferItem(onGround);
                            item.remove();
                        } else if (newBuffer.isSimilar(onGround)) {
                            toSlurp = Math.min(onGround.getAmount(), newBuffer.getType().getMaxStackSize() - newBuffer.getAmount());
                            getItemRouter().setBufferAmount(newBuffer.getAmount() + toSlurp);
                            onGround.setAmount(onGround.getAmount() - toSlurp);

                            if (onGround.getAmount() == 0) {
                                item.remove();
                            } else {
                                item.setItemStack(onGround);
                            }
                        }

                        if (toSlurp > 0) {
                            getItemRouter().playParticles(new java.awt.Color(0, 0, 255));
                            getItemRouter().update(false);
                        }
                    }
                }, (long) (dist / 3));
            }
        }
        return false; // any work done is deferred
    }

    private boolean rightDirection(Location itemLoc, Location rtrLoc) {
        if (getFacing() == null || getFacing() == BlockFace.SELF) {
            return true;
        }

        switch (getFacing()) {
            case NORTH:
                return itemLoc.getZ() < rtrLoc.getZ();
            case EAST:
                return itemLoc.getX() > rtrLoc.getX();
            case SOUTH:
                return itemLoc.getZ() > rtrLoc.getZ();
            case WEST:
                return itemLoc.getX() > rtrLoc.getX();
            case UP:
                return itemLoc.getY() > rtrLoc.getY();
            case DOWN:
                return itemLoc.getY() < rtrLoc.getY();
            default:
                return true;
        }
    }
}
