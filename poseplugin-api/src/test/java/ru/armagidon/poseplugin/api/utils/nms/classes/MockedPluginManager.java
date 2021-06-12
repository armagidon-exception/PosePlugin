package ru.armagidon.poseplugin.api.utils.nms.classes;

import lombok.AllArgsConstructor;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Set;

@AllArgsConstructor
public class MockedPluginManager implements PluginManager
{

    private final Server server;
    public final Plugin PROTOCOLLIB;

    public MockedPluginManager(Server server) {
        this.server = server;
        PROTOCOLLIB = new MockedPlugin(server, "ProtocolLib");
    }

    @Override
    public void registerInterface(@NotNull Class<? extends PluginLoader> aClass) throws IllegalArgumentException {

    }

    @Override
    public @Nullable Plugin getPlugin(@NotNull String s) {
        return PROTOCOLLIB;
    }

    @Override
    public @NotNull
    Plugin[] getPlugins() {
        return new Plugin[0];
    }

    @Override
    public boolean isPluginEnabled(@NotNull String s) {
        return false;
    }

    @Override
    public boolean isPluginEnabled(@Nullable Plugin plugin) {
        return false;
    }

    @Override
    public @Nullable Plugin loadPlugin(@NotNull File file) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        return null;
    }

    @Override
    public @NotNull
    Plugin[] loadPlugins(@NotNull File file) {
        return new Plugin[0];
    }

    @Override
    public void disablePlugins() {

    }

    @Override
    public void clearPlugins() {

    }

    @Override
    public void callEvent(@NotNull Event event) throws IllegalStateException {

    }

    @Override
    public void registerEvents(@NotNull Listener listener, @NotNull Plugin plugin) {

    }

    @Override
    public void registerEvent(@NotNull Class<? extends Event> aClass, @NotNull Listener listener, @NotNull EventPriority eventPriority, @NotNull EventExecutor eventExecutor, @NotNull Plugin plugin) {

    }

    @Override
    public void registerEvent(@NotNull Class<? extends Event> aClass, @NotNull Listener listener, @NotNull EventPriority eventPriority, @NotNull EventExecutor eventExecutor, @NotNull Plugin plugin, boolean b) {

    }

    @Override
    public void enablePlugin(@NotNull Plugin plugin) {

    }

    @Override
    public void disablePlugin(@NotNull Plugin plugin) {
        ((MockedPlugin)PROTOCOLLIB).setEnabled(false);
    }

    @Override
    public void disablePlugin(@NotNull Plugin plugin, boolean b) {

    }

    @Override
    public @Nullable Permission getPermission(@NotNull String s) {
        return null;
    }

    @Override
    public void addPermission(@NotNull Permission permission) {

    }

    @Override
    public void removePermission(@NotNull Permission permission) {

    }

    @Override
    public void removePermission(@NotNull String s) {

    }

    @Override
    public @NotNull Set<Permission> getDefaultPermissions(boolean b) {
        return null;
    }

    @Override
    public void recalculatePermissionDefaults(@NotNull Permission permission) {

    }

    @Override
    public void subscribeToPermission(@NotNull String s, @NotNull Permissible permissible) {

    }

    @Override
    public void unsubscribeFromPermission(@NotNull String s, @NotNull Permissible permissible) {

    }

    @Override
    public @NotNull Set<Permissible> getPermissionSubscriptions(@NotNull String s) {
        return null;
    }

    @Override
    public void subscribeToDefaultPerms(boolean b, @NotNull Permissible permissible) {

    }

    @Override
    public void unsubscribeFromDefaultPerms(boolean b, @NotNull Permissible permissible) {

    }

    @Override
    public @NotNull Set<Permissible> getDefaultPermSubscriptions(boolean b) {
        return null;
    }

    @Override
    public @NotNull Set<Permission> getPermissions() {
        return null;
    }

    @Override
    public boolean useTimings() {
        return false;
    }
}
