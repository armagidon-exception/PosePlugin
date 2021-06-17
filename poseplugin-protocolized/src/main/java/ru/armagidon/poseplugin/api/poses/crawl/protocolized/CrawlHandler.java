package ru.armagidon.poseplugin.api.poses.crawl.protocolized;

import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.poses.crawl.PressingBlock;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;
import ru.armagidon.poseplugin.api.utils.nms.ToolPackage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static ru.armagidon.poseplugin.api.utils.nms.ReflectionTools.getNmsClass;


@ToolPackage(mcVersion = "protocolized")
public class CrawlHandler extends ru.armagidon.poseplugin.api.poses.crawl.CrawlHandler
{

    private static Object aabb;
    private final Object packet;


    public CrawlHandler(Player player) {
        super(player);
        this.packet = NMSUtils.createPosePacket(player, Pose.SWIMMING);
        if (aabb == null)
            aabb = NMSUtils.getSwimmingAABB(player);
    }

    @Override
    protected void updateBoundingBox() {
        NMSUtils.setAABB(getPlayer(), aabb);
    }

    @Override
    protected void updatePose(Player receiver) {
        NMSUtils.sendPacket(receiver, packet);
    }

    @Override
    protected PressingBlock createPressingBlock(Location above, boolean isSlab) {
        if (!isSlab) {
            return new PressingBlock.BarrierPressingBlock(above, getPlayer());
        } else {
            return new ShulkerPressingBlock(above, getPlayer());
        }
    }

    public static class ShulkerPressingBlock extends PressingBlock {

        private int id;
        private Object shulker;

        public ShulkerPressingBlock(Location location, Player player) {
            super(location, player);
        }

        @Override
        @SneakyThrows
        public void show0() {
            //Creating shulker
            Object shulker = createShulker();

            //Get all fields
            Object shulkerDirection = NMSUtils.getStaticDWObjectFromEntity("b", shulker);
            Enum<?> direction = ReflectionTools.getEnumValueOf("UP", ReflectionTools.getEnum("EnumDirection"));
            Method getId = ReflectionTools.getMethodSafely(getNmsClass("Entity"), "getId");
            this.id = (int) getId.invoke(shulker);

            //Setting direction
            NMSUtils.setValueToDW(NMSUtils.getDataWatcher(shulker), shulkerDirection, direction);

            //Set position
            //s.setPosition(0.5 + location.getX(), location.getY(), location.getZ() + 0.5);
            Method setPosition = shulker.getClass().getDeclaredMethod("setPosition", double.class, double.class, double.class);
            setPosition.setAccessible(true);
            setPosition.invoke(shulker, 0.5 + location.getX(), location.getY(), location.getZ() + 0.5);

            //Set NO AI
            //s.setNoAI(true);
            Method setNoAI = ReflectionTools.getNmsClass("EntityInsentient").getDeclaredMethod("setNoAI", boolean.class);
            setNoAI.setAccessible(true);
            setNoAI.invoke(shulker, true);

            //Set invulnerability
            //s.setInvulnerable(true);
            Method setInvulnerable = ReflectionTools.getNmsClass("Entity").getDeclaredMethod("setInvulnerable", boolean.class);
            setInvulnerable.setAccessible(true);
            setInvulnerable.invoke(shulker, true);
            //Set peek
            //s.a(50);
            setPeek(shulker, NMSUtils.getDataWatcher(shulker));

            //Set bounding box
            Object aabb = NMSUtils.createBoundingBox(location.getX() - 0.5D, location.getY(), location.getZ() - 0.5D, location.getX() + 0.5D, location.getY() + 1.0D, location.getZ() + 0.5D);
            NMSUtils.setAABB(shulker, aabb);
            //s.a((new AxisAlignedBB(s.locX() - 0.5D, s.locY(), s.locZ() - 0.5D, s.locX() + 0.5D, s.locY() + 1.0D, s.locZ() + 0.5D)));

            //Tick
            Method tick = shulker.getClass().getDeclaredMethod("tick");
            tick.setAccessible(true);
            tick.invoke(shulker);
            //s.tick();

            //Set invisibility
            //s.setInvisible(true);
            NMSUtils.setInvisible(shulker, true);

            //s.setSilent(true);

            //Create spawn packet
            Object living = NMSUtils.createPacketInstance("PacketPlayOutSpawnEntityLiving", new Class[]{getNmsClass("EntityLiving")}, shulker);
            //PacketPlayOutSpawnEntityLiving living = new PacketPlayOutSpawnEntityLiving(s);
            NMSUtils.sendPacket(player, living);

            //Create metadata packet
            Object metadata = NMSUtils.createPacketInstance("PacketPlayOutEntityMetadata", new Class[] {int.class, getNmsClass("DataWatcher"), boolean.class}, id, NMSUtils.getDataWatcher(shulker), true);
            NMSUtils.sendPacket(player, metadata);

            this.shulker = shulker;
        }

        @Override
        public void hide0() {
            Object destroy = NMSUtils.createPacketInstance("PacketPlayOutEntityDestroy", new Class[] {int[].class}, new int[] {id}, true);
            NMSUtils.sendPacket(player, destroy);
        }

        @Override
        @SneakyThrows
        public void move0(Location location) {
            //Setting location
            //shulker.setLocation(location.getX(), location.getY(), location.getZ(), 0f, 0f);
            Method setLocation = ReflectionTools.getMethodSafely(getNmsClass("Entity"), "setLocation", double.class, double.class, double.class, float.class, float.class);
            setLocation.setAccessible(true);
            setLocation.invoke(shulker, location.getX(), location.getY(), location.getZ(), 0f, 0f);

            Constructor<?> blockPositionConstructor = getNmsClass("BlockPosition").getConstructor(int.class,int.class,int.class);
            Object blockPosition = blockPositionConstructor.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            Optional<?> blockPositionOptional = Optional.of(blockPosition);

            Object blockPosObj = NMSUtils.getStaticDWObjectFromEntity("c", shulker);

            NMSUtils.setValueToDW(NMSUtils.getDataWatcher(shulker), blockPosObj, blockPositionOptional);
            NMSUtils.setInvisible(shulker, true);

            Object metadata = NMSUtils.createPacketInstance("PacketPlayOutEntityMetadata", new Class[]{int.class, getNmsClass("DataWatcher"), boolean.class}, id, NMSUtils.getDataWatcher(shulker), false);

            NMSUtils.sendPacket(player, metadata);
        }

        @SneakyThrows
        private void setPeek(Object s, Object dataWatcher) {
            Field peekDW = s.getClass().getDeclaredField("d");
            peekDW.setAccessible(true);
            NMSUtils.setValueToDW(dataWatcher, peekDW.get(null), (byte) 50);
        }

        @SneakyThrows
        private Object createShulker() {
            Class<?> SHULKER_CLASS = ReflectionTools.getNmsClass("EntityShulker");
            Constructor<?> shulkerConstructor = SHULKER_CLASS.getDeclaredConstructor(ReflectionTools.getNmsClass("EntityTypes"), ReflectionTools.getNmsClass("World"));

            Field entityType = ReflectionTools.getNmsClass("EntityTypes").getDeclaredField("SHULKER");
            entityType.setAccessible(true);

            Object world = location.getWorld().getClass().getDeclaredMethod("getHandle").invoke(location.getWorld());

            return shulkerConstructor.newInstance(entityType.get(null), world);
        }

    }
}
