package ru.armagidon.poseplugin.api.utils.nms.npc;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

public abstract class NPCMetadataEditor<T>
{
    protected final T dataWatcher;
    protected boolean invisible;
    protected final FakePlayer<T> fakePlayer;

    public NPCMetadataEditor(FakePlayer<T> fakePlayer) {
        this.fakePlayer = fakePlayer;
        this.dataWatcher = fakePlayer.getDataWatcher();
    }

    public abstract byte getLivingEntityTags();

    public abstract void setLivingEntityTags(byte tags);

    public abstract void showPlayer(Player receiver);

    public abstract void setPose(Pose pose);

    public abstract void setBedPosition(Location location);

    public abstract void setInvisible(boolean flag);

    public abstract void setOverlays(byte overlays);

    public abstract void setActiveHand(boolean main);

    public abstract void disableHand();

    public abstract Pose getPose();

    public abstract boolean isInvisible();

    public abstract void merge(boolean append);

    public abstract boolean isHandActive();

    public abstract void setMainHand(boolean main);

    public abstract HandType whatHandIsMain();
}
