package me.george.playerdata.yml;

import me.george.playerdata.api.IPlayerDataService;
import me.george.playerdata.api.PlayerProfile;
import me.george.playerdata.api.PlayerProfileListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.UnaryOperator;

public class PlayerDataServiceYml implements IPlayerDataService {
    private final File dataFile;
    private final FileConfiguration config;
    private final Set<PlayerProfileListener> listeners = new CopyOnWriteArraySet<>();
    private final JavaPlugin plugin;

    public PlayerDataServiceYml(JavaPlugin plugin) {
        this.plugin = plugin;
        var dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder: " + dataFolder.getAbsolutePath());
        }

        this.dataFile = new File(dataFolder, "player-profiles.yml");
        this.config = YamlConfiguration.loadConfiguration(dataFile);
    }

    @Override
    public Optional<PlayerProfile> getProfile(UUID uuid) {
        var basePath = profilePath(uuid);
        if (!config.contains(basePath)) {
            return Optional.empty();
        }

        plugin.getLogger().info("Retrieved profile for player " + uuid + ": " +
                "team=" + config.getString(basePath + ".team", PlayerProfile.NONE) +
                ", role=" + config.getString(basePath + ".role", PlayerProfile.NONE) +
                ", race=" + config.getString(basePath + ".race", PlayerProfile.NONE));

        return Optional.of(new PlayerProfile(
                config.getString(basePath + ".team", PlayerProfile.NONE),
                config.getString(basePath + ".role", PlayerProfile.NONE),
                config.getString(basePath + ".race", PlayerProfile.NONE)
        ));
    }

    @Override
    public void saveProfile(UUID uuid, PlayerProfile playerProfile) {
        var basePath = profilePath(uuid);
        config.set(basePath + ".team", playerProfile.team());
        config.set(basePath + ".role", playerProfile.role());
        config.set(basePath + ".race", playerProfile.race());

        saveToDisk();
        notifyChanged(uuid, playerProfile);
        plugin.getLogger().info("Saved profile for player " + uuid + ": " + playerProfile);
    }

    @Override
    public PlayerProfile updateProfile(UUID uuid, UnaryOperator<PlayerProfile> unaryOperator) {
        var current = getProfile(uuid).orElse(PlayerProfile.empty());
        var updated = unaryOperator.apply(current);
        saveProfile(uuid, updated);
        return updated;
    }

    @Override
    public void deleteProfile(UUID uuid) {
        var basePath = profilePath(uuid);
        if (!config.contains(basePath)) {
            return;
        }

        config.set(basePath, null);
        saveToDisk();
        notifyChanged(uuid, PlayerProfile.empty());
    }

    @Override
    public boolean exists(UUID uuid) {
        return config.contains(profilePath(uuid));
    }

    @Override
    public void addPlayerProfileEventListener(PlayerProfileListener playerProfileListener) {
        listeners.add(playerProfileListener);
    }

    @Override
    public void removePlayerProfileEventListener(PlayerProfileListener playerProfileListener) {
        listeners.remove(playerProfileListener);
    }

    private String profilePath(UUID uuid) {
        return "profiles." + uuid;
    }

    private void notifyChanged(UUID uuid, PlayerProfile profile) {
        for (var listener : listeners) {
            listener.onPlayerProfileChanged(uuid, profile);
        }
    }

    private void saveToDisk() {
        try {
            config.save(dataFile);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save YAML data to " + dataFile.getAbsolutePath(), e);
        }
    }
}
