package ru.armagidon.poseplugin.api.utils.nms;

import net.minecraft.server.v1_17_R1.MockedServer;
import org.bukkit.Bukkit;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.nms.classes.MarkedSuperClass;
import ru.armagidon.poseplugin.api.utils.nms.classes.MockedPlugin;

import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class ToolfinderTest
{
    @Test
    public void testToolfinderProtocolized() {
        Reflections reflections = new Reflections(new ConfigurationBuilder().addScanners(new TypeAnnotationsScanner()).forPackages("ru.armagidon"));
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(ToolPackage.class);
        classes.forEach(clazz -> System.out.println(clazz.getSimpleName()));



        MockedServer server = new MockedServer();
        Bukkit.setServer(server);
        server.getPluginManager().disablePlugin(null);

        PosePluginAPI.initialize(new MockedPlugin(server, "PosePlugin"));

        ToolFactory.scanTools();
        assertNotNull("ToolFactory didn't find any packages", ToolFactory.create(MarkedSuperClass.class));
    }


}
