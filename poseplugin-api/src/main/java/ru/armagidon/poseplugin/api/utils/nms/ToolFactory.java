package ru.armagidon.poseplugin.api.utils.nms;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.versions.Version;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ToolFactory
{

    private static final Map<Class<?>, Class<?>> toolPackages = new HashMap<>();
    private static boolean initialized = false;

    public static void scanTools() {
        if (!initialized) {
            Plugin protocolLib = Bukkit.getPluginManager().getPlugin("ProtocolLib");
            Reflections reflections = new Reflections(new ConfigurationBuilder().addScanners(new TypeAnnotationsScanner()).forPackages("ru.armagidon.poseplugin.api"));
            Set<Class<?>> classes;

            Version version = Version.getVersion();
            if (version.isForceload()) {
                PosePluginAPI.getAPI().getLogger().info(Version.getCurrentVersionString() + " is forced to use versioned packages. Loading...");
                classes = reflections.getTypesAnnotatedWith(ToolPackage.class).stream().filter(clazz -> {
                    String v = clazz.getAnnotation(ToolPackage.class).mcVersion();
                    int priority = Version.getVersionPriority(v);
                    return priority == Version.getCurrentVersionPriority();
                }).collect(Collectors.toSet());
            } else if (protocolLib != null && protocolLib.isEnabled()) {
                PosePluginAPI.getAPI().getLogger().info("ProtocolLib is enabled! Running protocolized package");
                classes = reflections.getTypesAnnotatedWith(ToolPackage.class).stream().filter(clazz ->
                        clazz.getAnnotation(ToolPackage.class).mcVersion().equalsIgnoreCase("protocolized")).collect(Collectors.toSet());
            } else {
                PosePluginAPI.getAPI().getLogger().warning("ProtocolLib seems to be down. Using versioned package...");
                classes = reflections.getTypesAnnotatedWith(ToolPackage.class).stream().filter(clazz -> {
                    String v = clazz.getAnnotation(ToolPackage.class).mcVersion();
                    int priority = Version.getVersionPriority(v);
                    return priority == Version.getCurrentVersionPriority();
                }).collect(Collectors.toSet());
            }
            if (classes.isEmpty()) {
                throw new RuntimeException("Could not find any packages! Disabling...");
            } else {
                classes.forEach(clazz -> PosePluginAPI.getAPI().getLogger().info("Loaded " + clazz.getName()));
            }

            classes.forEach(clazz -> toolPackages.put(clazz.getSuperclass(), clazz));
            initialized = true;
        }
    }

    @Nullable
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> clazz, Class<?>[] types, Object... params) {
        if (!toolPackages.containsKey(clazz)) return null;
        Class<?> packageClass = toolPackages.get(clazz);

        Constructor<?> constructor = packageClass.getDeclaredConstructor(types);
        constructor.setAccessible(true);
        return (T) constructor.newInstance(params);
    }
}
