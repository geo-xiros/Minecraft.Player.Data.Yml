# player-data-yml

`player-data-yml` is a storage plugin that registers an implementation of `IPlayerDataService` with Bukkit's `ServicesManager`.

This lets other plugins retrieve the service at runtime instead of depending on the implementation class.

## What this plugin registers

On startup, this plugin registers:

- Service interface: `me.george.playerdata.api.IPlayerDataService`
- Provider implementation: `me.george.playerdata.yml.PlayerDataServiceYml`
- Priority: `ServicePriority.Normal`

## Using it from another plugin

### 1) Add a dependency in your `plugin.yml`

Use `depend` if your plugin requires this service to start, or `softdepend` if it is optional.

```yaml
name: your-plugin
version: 1.0.0
main: your.package.YourPlugin
api-version: '26.1.1'

depend:
  - player-data-yml
```

### 2) Depend on the API artifact (not this implementation jar)

Your plugin should compile against `player-data-api`.

```xml
<dependency>
    <groupId>me.george.playerdata</groupId>
    <artifactId>player-data-api</artifactId>
    <version>2.0</version>
    <scope>provided</scope>
</dependency>
```

### 3) Retrieve the registered service instance

In your plugin's `onEnable`:

```java
import me.george.playerdata.api.IPlayerDataService;
import org.bukkit.plugin.java.JavaPlugin;

public class YourPlugin extends JavaPlugin {
    private IPlayerDataService playerDataService;

    @Override
    public void onEnable() {
        playerDataService = getServer()
                .getServicesManager()
                .load(IPlayerDataService.class);

        if (playerDataService == null) {
            getLogger().severe("player-data-yml service is not available.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Connected to player-data-yml service.");
    }
}
```

## Optional: retrieve provider metadata

If you also want the plugin/provider metadata:

```java
import me.george.playerdata.api.IPlayerDataService;
import org.bukkit.plugin.RegisteredServiceProvider;

RegisteredServiceProvider<IPlayerDataService> registration =
        getServer().getServicesManager().getRegistration(IPlayerDataService.class);

if (registration != null) {
    IPlayerDataService service = registration.getProvider();
    String providerPlugin = registration.getPlugin().getName();
}
```

## Notes

- Registered plugin name is `player-data-yml`.
- The service is unregistered when `player-data-yml` is disabled.
- Handle `null` service lookups to avoid startup errors in dependent plugins.

