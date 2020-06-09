package ru.armagidon.poseplugin.api.poses.sit;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;

public class SitPose extends PluginPose {

    private SitDriver driver;

    public SitPose(Player player) {
        super(player);
        this.driver = new SitDriver(player, ()-> stop(true));
        initTickModules();
    }

    @Override
    protected void initTickModules() {
        addTickModule(driver::tick);
    }

    public void play(Player receiver, boolean log){
        super.play(receiver,log);
        driver.takeASeat();
    }

    public void stop(boolean log){
        super.stop(log);
        driver.standUp();
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.SITTING;
    }

    @Override
    public String getSectionName() {
        return "sit";
    }


}
