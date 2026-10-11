package me.george.playerdata.yml;


import org.bukkit.plugin.java.JavaPlugin;

public class PlayerDataStoragePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        var dataService = new PlayerDataServiceYml(this);

        getServer().getServicesManager().register(me.george.playerdata.api.IPlayerDataService.class, dataService, this, org.bukkit.plugin.ServicePriority.Normal);
        getLogger().info("PlayerDataStoragePlugin has been enabled and registered the PlayerDataServiceYaml.");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getServicesManager().unregister(me.george.playerdata.api.IPlayerDataService.class, this);
        getLogger().info("PlayerDataStoragePlugin has been disabled and unregistered the PlayerDataServiceYaml.");
    }
}
