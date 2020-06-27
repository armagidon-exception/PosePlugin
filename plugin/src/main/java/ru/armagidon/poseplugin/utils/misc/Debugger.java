package ru.armagidon.poseplugin.utils.misc;

import ru.armagidon.poseplugin.PosePlugin;

public class Debugger
{
    public boolean enabled = false;

    public void debug(String msg) {
        if (enabled) PosePlugin.getInstance().getLogger().info(msg);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
