package ru.armagidon.poseplugin.api.utils.nms;

import net.minecraft.server.v1_17_R1.MockedServer;
import org.bukkit.Bukkit;
import org.junit.Test;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.nms.classes.MockedPlugin;
import ru.armagidon.poseplugin.api.utils.nms.v1_17.MarkedSuperClass;

import static org.junit.Assert.assertNotNull;

public class ToolfinderTest
{
    @Test
    public void testToolfinderProtocolized() {
        MockedServer server = new MockedServer();
        Bukkit.setServer(server);
        server.getPluginManager().disablePlugin(null);

        PosePluginAPI.initialize(new MockedPlugin(server, "PosePlugin"));

        ToolFactory.scanTools();
        assertNotNull("ToolFactory didn't find any packages", ToolFactory.create(MarkedSuperClass.class));
    }


}
