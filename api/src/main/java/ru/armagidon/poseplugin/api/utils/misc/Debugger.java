package ru.armagidon.poseplugin.api.utils.misc;


import ru.armagidon.poseplugin.api.PosePluginAPI;

public class Debugger
{
    private boolean enabled = false;

    public void debug(String msg) {
        if (enabled) PosePluginAPI.getAPI().getLogger().info(msg);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
