package ru.armagidon.sit.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.scheduler.BukkitRunnable;
import ru.armagidon.sit.SitPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker extends BukkitRunnable
{

    @Override
    public void run() {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spiget.org/v2/resources/76990/versions?size=1&sort=-releaseDate").openConnection();
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());

            JsonElement element = new JsonParser().parse(reader);

            reader.close();

            JsonObject versionObject = element.getAsJsonArray().get(0).getAsJsonObject();
            String current = SitPlugin.getInstance().getDescription().getVersion(), newest = versionObject.get("name").getAsString();

            if (!current.equalsIgnoreCase(newest)) {
                SitPlugin.getInstance().getLogger().warning("Plugin Out of Date");
                sendMessage("§bNew version "+newest+" is available now!",true);
            } else {
                SitPlugin.getInstance().getLogger().info("Plugin Up to Date");
            }
        } catch (IOException var7) {
            sendMessage("§cCheck update fail: " + var7.getMessage(),false);
        }

    }

    private void sendMessage(String message, boolean f){
        Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(p-> {
            if(!f) {
                p.sendMessage(message);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            } else {
                TextComponent c = new TextComponent("§bOpen");
                TextComponent msg = new TextComponent(message);
                msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new BaseComponent[]{c}));
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://www.spigotmc.org/resources/poseplugin-choose-your-favorite-pose.76990/"));
                p.spigot().sendMessage(msg);
            }
        });
    }
}
