package ru.armagidon.poseplugin.api.utils.npc;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

public interface FakePlayerMetadataAccessor
{

    void showPlayer(Player receiver);

    void setPose(Pose pose);

    void setBedPosition(Location location);

    void setInvisible(boolean flag);

    void setOverlays(byte overlays);

    void setActiveHand(boolean main);

    void disableHand();

    Pose getPose();

    boolean isInvisible();

    void merge(boolean append);

    boolean isHandActive();

    void setMainHand(boolean main);

    HandType whatHandIsMain();
}
