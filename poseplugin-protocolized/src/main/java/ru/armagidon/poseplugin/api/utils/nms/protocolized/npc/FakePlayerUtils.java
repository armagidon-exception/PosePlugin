package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc;

import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

import java.lang.reflect.Constructor;

public class FakePlayerUtils
{
    @SuppressWarnings("all")
    @SneakyThrows
    public static Enum<?> getDirection(float angle) {
        angle = unsignAngle(angle);

        Class ENUM_DIRECTION = ReflectionTools.getEnum("EnumDirection");

        if (angle >= 315.0F || angle <= 45.0F) {
            return (Enum<?>) Enum.valueOf(ENUM_DIRECTION, "NORTH");
        } else if (angle >= 45.0F && angle <= 135.0F) {
            //a = EnumDirection.EAST;
            return (Enum<?>) Enum.valueOf(ENUM_DIRECTION, "EAST");
        } else if (angle >= 135.0F && angle <= 225.0F) {
            //a = EnumDirection.SOUTH;
            return (Enum<?>) Enum.valueOf(ENUM_DIRECTION, "SOUTH");
        } else if (angle >= 225.0F && angle <= 315.0F) {
            //a = EnumDirection.WEST;
            return (Enum<?>) Enum.valueOf(ENUM_DIRECTION, "WEST");
        } else {
            return (Enum<?>) Enum.valueOf(ENUM_DIRECTION, "NORTH");
        }
    }

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

    public static ItemStack getEquipmentBySlot(EntityEquipment e, Enum<?> slot){
        ItemStack item;
        if (!slot.getDeclaringClass().getSimpleName().equalsIgnoreCase("EnumItemSlot"))
            return new ItemStack(Material.AIR);
        item = switch (slot.name()) {
            case "HEAD" -> e.getHelmet();
            case "CHEST" -> e.getChestplate();
            case "LEGS" -> e.getLeggings();
            case "FEET" -> e.getBoots();
            case "OFFHAND" -> e.getItemInOffHand();
            default -> e.getItemInMainHand();
        };
        return item;
    }

    public static float unsignAngle(float rawYaw){
        rawYaw = rawYaw < 0.0F ? 360.0F + rawYaw : rawYaw;
        rawYaw = rawYaw % 360.0F;
        return rawYaw;
    }

    public static byte setBit(byte input, int k, boolean flag){
        byte output;
        if(flag){
            output = (byte) (input | (1 << k));
        } else {
            output = (byte) (input & ~(1 << k));
        }
        return output;
    }

    @SneakyThrows
    public static Object toBlockPosition(Location location) {
        Constructor<?> constructor = ReflectionTools.getNmsClass("BlockPosition").
                getDeclaredConstructor(int.class, int.class, int.class);
        return constructor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @SuppressWarnings("all")
    @SneakyThrows
    public static Enum<?> adaptToItemSlot(EquipmentSlot slotType){

        Class ENUM_ITEM_SLOT = ReflectionTools.getEnum("EnumItemSlot");

        switch (slotType){
            case HAND:
                return Enum.valueOf(ENUM_ITEM_SLOT, "MAINHAND");
            case OFF_HAND:
                return Enum.valueOf(ENUM_ITEM_SLOT, "OFFHAND");
            default:
                return Enum.valueOf(ENUM_ITEM_SLOT, slotType.name().toUpperCase());
        }
    }

    public static Location toBedLocation(Location location) {
        return location.clone().toVector().setY(0).toLocation(location.getWorld());
    }
}
