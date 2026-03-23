package br.com.beegacha.manager;

import br.com.beegacha.model.Reward;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Loads reward definitions from {@code config.yml} and handles weighted-random
 * selection.
 */
public class RewardManager {

    private final List<Reward> rewards = new ArrayList<>();
    private final Random random = new Random();

    /**
     * (Re-)loads all rewards from the provided plugin configuration.
     */
    public void load(FileConfiguration config) {
        rewards.clear();
        ConfigurationSection section = config.getConfigurationSection("rewards");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String matName    = section.getString(key + ".material", "STONE");
            String name       = section.getString(key + ".name", key);
            double chance     = section.getDouble(key + ".chance", 10);
            String command    = section.getString(key + ".command", "");

            Material material;
            try {
                material = Material.valueOf(matName.toUpperCase());
            } catch (IllegalArgumentException e) {
                material = Material.STONE;
            }

            rewards.add(new Reward(key, material, name, chance, command));
        }
    }

    /**
     * Selects a reward using weighted random selection.
     * Each reward's {@code chance} field is used as its weight.
     *
     * @return a randomly selected {@link Reward}, or {@code null} if none are loaded.
     */
    public Reward selectReward() {
        if (rewards.isEmpty()) return null;

        double totalWeight = rewards.stream().mapToDouble(Reward::getChance).sum();
        double roll = random.nextDouble() * totalWeight;

        double cumulative = 0;
        for (Reward reward : rewards) {
            cumulative += reward.getChance();
            if (roll < cumulative) {
                return reward;
            }
        }
        // Fallback – return last reward
        return rewards.get(rewards.size() - 1);
    }

    /** Returns a defensive copy of the loaded reward list. */
    public List<Reward> getRewards() {
        return new ArrayList<>(rewards);
    }
}
