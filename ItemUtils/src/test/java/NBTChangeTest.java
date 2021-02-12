import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Test;
import ru.armagidon.armagidonapi.itemutils.nbt.NBTModifier;
import ru.armagidon.mock.v1_16_R1.MockedServer;

import java.lang.reflect.Field;

public class NBTChangeTest
{
    @Test
    public void testNBTChange() throws NoSuchFieldException, IllegalAccessException {
        Server server = new MockedServer();

        Field f = Bukkit.class.getDeclaredField("server");
        f.setAccessible(true);
        f.set(null, server);

        ItemMeta meta = CraftItemFactory.instance().getItemMeta(Material.STONE);
        ItemStack stack = new ItemStack(Material.STONE);
        stack.setItemMeta(meta);
        CraftItemStack s = CraftItemStack.asCraftCopy(stack);
        System.out.println("INIT STACK");
        System.out.println(NBTModifier.nmsVersion());
        System.out.println(NBTModifier.getString(s, "test"));
        System.out.println("CHANGE NBT");
        NBTModifier.setString(s, "test", "2");
        System.out.println("OUTPUT");
        System.out.println(NBTModifier.getString(s, "test"));
        System.out.println("REMOVE");
        NBTModifier.remove(s, "test");
        System.out.println(NBTModifier.getString(s, "test"));
    }
}
