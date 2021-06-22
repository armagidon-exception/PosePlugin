package ru.armagidon.poseplugin.api.utils.nms.protocolized.playerhider;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.nms.ToolPackage;
import ru.armagidon.poseplugin.api.utils.nms.playerhider.PlayerHider;
import ru.armagidon.poseplugin.api.utils.versions.Version;

@ToolPackage(mcVersion = "protocolized")
public class PlayerHiderProtocolized extends PlayerHider
{


    private final PlayerHider hider;

    public PlayerHiderProtocolized() {
        if (Version.getVersion() == Version.v1_15) {
            hider = new OldPlayerHiderProtocolized();
        } else {
            hider = new NewPlayerHiderProtocolized();
        }
    }

    @Override
    public void tick() {
        hider.tick();
    }

    @Override
    public void hide(Player player) {
        hider.hide(player);
    }

    @Override
    public void show(Player player) {
        hider.show(player);
    }

    @Override
    public boolean isHidden(Player player) {
        return hider.isHidden(player);
    }
}
