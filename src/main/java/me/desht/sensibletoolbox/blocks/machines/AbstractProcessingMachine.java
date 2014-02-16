package me.desht.sensibletoolbox.blocks.machines;

import me.desht.sensibletoolbox.api.ProcessingMachine;
import me.desht.sensibletoolbox.gui.ProgressMeter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a machine with a progress bar to indicate how much of this work cycle is done.
 */
public abstract class AbstractProcessingMachine extends BaseSTBMachine implements ProcessingMachine {
	private static final long PROGRESS_INTERVAL = 10;
	private double progress; // ticks remaining till this work cycle is done
	private int progressCounterId;
	private ItemStack processing;
	private String frozenProcessing;

	protected AbstractProcessingMachine() {
		super();
	}

	public AbstractProcessingMachine(ConfigurationSection conf) {
		super(conf);
		progress = conf.getDouble("progress");
		frozenProcessing = conf.getString("processing", "");
	}

	@Override
	public YamlConfiguration freeze() {
		YamlConfiguration conf = super.freeze();
		conf.set("progress", getProgress());
		if (getProgress() > 0) {
			conf.set("processing", getGUI().freezeSlots(getProgressItemSlot()));
		}
		return conf;
	}

	public abstract int getProgressItemSlot();

	public abstract int getProgressCounterSlot();

	public abstract Material getProgressIcon();

	@Override
	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = Math.max(0, progress);
		getProgressCounter().repaintNeeded();
	}

	@Override
	public ItemStack getProcessing() {
		return processing;
	}

	public void setProcessing(ItemStack processing) {
		this.processing = processing;
		getProgressCounter().repaintNeeded();
	}

	protected ProgressMeter getProgressCounter() {
		return (ProgressMeter) getGUI().getMonitor(progressCounterId);
	}

	@Override
	public void setLocation(Location loc) {
		if (loc == null && getProcessing() != null) {
			loc.getWorld().dropItemNaturally(loc, getProcessing());
			setProcessing(null);
		}
		super.setLocation(loc);
		if (loc != null) {
			getGUI().thawSlots(frozenProcessing, getProgressItemSlot());
			progressCounterId = getGUI().addMonitor(new ProgressMeter(getGUI()));
		}
	}

	@Override
	public void onServerTick() {
		if (getTicksLived() % PROGRESS_INTERVAL == 0 && isRedstoneActive() && getGUI().getViewers().size() > 0) {
			getProgressCounter().doRepaint();
		}
		super.onServerTick();
	}
}
