package ru.armagidon.poseplugin.api.utils.nms.npc;

import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;

public class FakePlayerUtils
{

    public static byte getFixedRotation(float var1){
        return (byte) round(var1 * 256.0F / 360.0F);
    }

    private static float round(float input){
        int output = (int)input;
        return input < (float)output ? output - 1 : output;
    }

    public static boolean isKthBitSet(int n, int k)
    {
        return  ((n & (1 << (k - 1))) == 1);
    }

    public static ItemStack getEquipmentBySlot(EntityEquipment e, EquipmentSlot slot){
        if (e == null) return new ItemStack(Material.AIR);
        ItemStack s =  switch (slot) {
            case HEAD -> e.getHelmet();
            case CHEST -> e.getChestplate();
            case LEGS -> e.getLeggings();
            case FEET -> e.getBoots();
            case OFF_HAND -> e.getItemInOffHand();
            default -> e.getItemInMainHand();
        };
        return s == null ? new ItemStack(Material.AIR) : s;
    }

    public static byte setBit(byte input, int k, boolean flag){
        return flag ? (byte) (input | (1 << k)) : (byte) (input & ~(1 << k));
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> T toBlockPosition(Location location, Class<T> blockPositionClass) {
        Constructor<?> constructor = blockPositionClass.getDeclaredConstructor(int.class, int.class, int.class);
        return (T) constructor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static Location toBedLocation(Location location) {
        return location.clone().toVector().setY(0).toLocation(location.getWorld());
    }

    @SneakyThrows
    public static <T extends Enum<T>> T adaptToItemSlot(EquipmentSlot slotType, Class<T> clazz){
        return clazz.getEnumConstants()[slotType.ordinal()];
    }
}
