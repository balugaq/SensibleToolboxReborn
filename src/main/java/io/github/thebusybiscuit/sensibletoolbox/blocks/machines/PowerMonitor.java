package io.github.thebusybiscuit.sensibletoolbox.blocks.machines;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import io.github.thebusybiscuit.sensibletoolbox.api.SensibleToolbox;
import io.github.thebusybiscuit.sensibletoolbox.api.energy.EnergyNet;
import io.github.thebusybiscuit.sensibletoolbox.api.items.BaseSTBBlock;
import io.github.thebusybiscuit.sensibletoolbox.items.energycells.TenKEnergyCell;
import io.github.thebusybiscuit.sensibletoolbox.utils.STBUtil;

public class PowerMonitor extends BaseSTBBlock {

    public PowerMonitor() {}

    public PowerMonitor(ConfigurationSection conf) {
        super(conf);
    }

    @Override
    public Material getMaterial() {
        return Material.ORANGE_STAINED_GLASS;
    }

    @Override
    public String getItemName() {
        return "能源监视器";
    }

    @Override
    public String[] getLore() {
        return new String[] { "在此机器贴上告示牌", "会显示能源增长与减少" };
    }

    @Override
    public Recipe getMainRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(getKey(), toItemStack());
        recipe.shape("GGG", "RCR", "GGG");
        TenKEnergyCell cell = new TenKEnergyCell();
        cell.setCharge(0.0);
        registerCustomIngredients(cell);
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('C', cell.getMaterial());
        recipe.setIngredient('R', Material.REDSTONE);
        return recipe;
    }

    @Override
    public int getTickRate() {
        return 100;
    }

    @Override
    public void onServerTick() {
        updateAttachedLabelSigns();
        super.onServerTick();
    }

    @Override
    protected String[] getSignLabel(BlockFace face) {
        String[] label = super.getSignLabel(face);

        label[2] = ChatColor.DARK_RED + "未连接能源网";

        for (BlockFace f : STBUtil.getMainHorizontalFaces()) {
            EnergyNet net = SensibleToolbox.getEnergyNet(getRelativeLocation(f).getBlock());

            if (net != null) {
                double stat = net.getSupply() - net.getDemand();
                String prefix;

                if (stat > 0) {
                    prefix = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "+ " + ChatColor.GREEN;
                } else {
                    prefix = ChatColor.DARK_RED + "" + ChatColor.BOLD + "- " + ChatColor.RED;
                }

                label[2] = prefix + STBUtil.getCompactDouble(Math.abs(stat)) + " SCU/t";
                break;
            }
        }

        return label;
    }
}