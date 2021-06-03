package ru.armagidon.poseplugin.api.poses.swim;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.misc.BlockCache;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;
import ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerEntityDestroy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static ru.armagidon.poseplugin.api.utils.nms.ReflectionTools.getNmsClass;


abstract class PressingBlock
{

    protected final Player player;
    protected Location location;
    private boolean shown = false;

    public PressingBlock(Location location, Player player) {
        this.location = location;
        this.player = player;
    }

    public void show() {
        if (!shown) {
            show0();
            shown = true;
        }
    }

    public void hide() {
        if (shown) hide0();
    }

    public void move(Location location) {
        if (shown) move0(location);
        this.location = location;
    }

    protected abstract void show0();
    protected abstract void hide0();
    protected abstract void move0(Location location);

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
            /*EntityShulker s = new EntityShulker(EntityTypes.SHULKER, ((CraftWorld)player.getWorld()).getHandle()) {

                {
                    o = EntityShulker.c;
                }

                @Override
                public boolean isCollidable() {
                    return true;
                }

                @Override
                public void a(int i) {
                    this.datawatcher.set(d, (byte)i);
                }
            };*/
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
            Object metadata = NMSUtils.createPacketInstance("PacketPlayOutEntityMetadata", new Class[]{int.class, getNmsClass("DataWatcher"), boolean.class}, id, NMSUtils.getDataWatcher(shulker), true);
            NMSUtils.sendPacket(player, metadata);

            this.shulker = shulker;
        }

        @Override
        public void hide0() {
            //shulker.remove();
            WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
            destroy.setEntityIds(id);
            destroy.sendPacket(player);

            /*PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(id);
            NMSUtils.sendPacket(player, destroy);*/
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
            /*
            shulker.getDataWatcher().set(o, Optional.of(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ())));
            PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(shulker.getId(), shulker.getDataWatcher(), false);
            Object metadata = NMSUtils.createPacketInstance("PacketPlayOutEntityMetadata", new Class[]{int.class, getNmsClass("DataWatcher"), boolean.class}, id, NMSUtils.getDataWatcher(shulker), true);
            NMSUtils.sendPacket(player, metadata);*/
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

    public static class BarrierPressingBlock extends PressingBlock {

        private static final BlockData BARRIER_DATA = Bukkit.createBlockData(Material.BARRIER);
        private final BlockCache cache;

        public BarrierPressingBlock(Location location, Player player) {
            super(location, player);
            cache = new BlockCache(location.getBlock().getBlockData(), location);
        }

        @Override
        public void show0() {
            player.sendBlockChange(location, BARRIER_DATA);
        }

        @Override
        public void hide0() {
            cache.restore(player);
        }

        @Override
        public void move0(Location to) {
            cache.restore(player);
            if (!to.getBlock().getType().isSolid()) {
                cache.setData(to.getBlock().getBlockData());
                cache.setLocation(to);
                player.sendBlockChange(to, BARRIER_DATA);
            }
        }
    }
}
