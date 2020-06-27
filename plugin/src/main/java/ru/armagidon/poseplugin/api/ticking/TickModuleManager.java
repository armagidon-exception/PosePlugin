package ru.armagidon.poseplugin.api.ticking;

public interface TickModuleManager {

     void addTickModule(TickModule module);

    boolean containsTickModule(TickModule module);

    void removeTickModule(TickModule module);

}
