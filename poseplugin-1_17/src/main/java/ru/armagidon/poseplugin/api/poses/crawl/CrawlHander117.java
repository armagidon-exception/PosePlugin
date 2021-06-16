package ru.armagidon.poseplugin.api.poses.crawl;


import lombok.SneakyThrows;
import net.minecraft.BlockUtil;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;
import ru.armagidon.poseplugin.api.utils.nms.ToolPackage;
import ru.armagidon.poseplugin.api.utils.nms.v1_17.npc.FakePlayer117;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static ru.armagidon.poseplugin.api.utils.misc.BlockPositionUtils.round;
import static ru.armagidon.poseplugin.api.utils.nms.ReflectionTools.getNmsClass;

@ToolPackage(mcVersion = "1.17")
public class CrawlHander117 extends CrawlHandler
{


    private final AABB swimmingBoundingBox;

    public CrawlHander117(Player player) {
        super(player);
        ServerPlayer vanillaPlayer = ((CraftPlayer)player).getHandle();
        EntityDimensions entitysize = vanillaPlayer.getDimensions(Pose.SWIMMING);
        float f = entitysize.width / 2.0F;
        Vec3 vec3d = new Vec3(vanillaPlayer.getX() - (double)f, vanillaPlayer.getY(), vanillaPlayer.getZ() - (double)f);
        Vec3 vec3d1 = new Vec3(vanillaPlayer.getX() + (double)f, vanillaPlayer.getY() + (double)entitysize.height, vanillaPlayer.getZ() + (double)f);
        this.swimmingBoundingBox = new AABB(vec3d, vec3d1);
    }

    @Override
    protected void updateBoundingBox() {
        ((CraftPlayer)getPlayer()).getHandle().setBoundingBox(swimmingBoundingBox);
    }

    @Override
    protected void updatePose(Player receiver) {
        ServerPlayer vanillaPlayer = ((CraftPlayer)getPlayer()).getHandle();
        vanillaPlayer.setPose(Pose.SWIMMING);
        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(vanillaPlayer.getId(), vanillaPlayer.getEntityData(), false);
        FakePlayer117.sendPacket(receiver, packet);
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

        private Shulker shulker;

        public ShulkerPressingBlock(Location location, Player player) {
            super(location, player);
        }

        @Override
        @SneakyThrows
        public void show0() {
            //Creating shulker
            Shulker shulker = createShulker();

            //Get all fields
            shulker.setAttachFace(Direction.UP);
            shulker.setPosRaw(0.5 + round(location.getBlockX()), round(location.getBlockY()), round(location.getZ()) + 0.5);

            //Set NO AI
            shulker.setNoAi(true);

            //Set invulnerability
            shulker.setInvulnerable(true);
            //Set peek
            shulker.setRawPeekAmount(50);

            //Tick
            shulker.tick();

            //Set invisibility
            shulker.setInvisible(true);

            //s.setSilent(true);

            //Create spawn packet
            ClientboundAddMobPacket spawner = new ClientboundAddMobPacket(shulker);
            FakePlayer117.sendPacket(player, spawner);

            //Create metadata packet
            ClientboundSetEntityDataPacket metadata = new ClientboundSetEntityDataPacket(shulker.getId(), shulker.getEntityData(), true);
            FakePlayer117.sendPacket(player, metadata);

            this.shulker = shulker;
        }

        @Override
        public void hide0() {
            ClientboundRemoveEntityPacket destroy = new ClientboundRemoveEntityPacket(shulker.getId());
            FakePlayer117.sendPacket(player, destroy);
        }

        @Override
        @SneakyThrows
        public void move0(Location location) {
            //Setting location

            shulker.setPos(location.getX(), location.getBlockY(), location.getZ());

            ClientboundTeleportEntityPacket teleportEntityPacket = new ClientboundTeleportEntityPacket(shulker);
            FakePlayer117.sendPacket(player, teleportEntityPacket);

            shulker.setRawPeekAmount(50);
            shulker.setInvisible(true);

            ClientboundSetEntityDataPacket metadata = new ClientboundSetEntityDataPacket(shulker.getId(), shulker.getEntityData(), true);
            FakePlayer117.sendPacket(player, metadata);
        }

        @SneakyThrows
        private Shulker createShulker() {
            return new Shulker(EntityType.SHULKER, ((CraftPlayer)player).getHandle().getLevel()) {
                @Override
                public void setRawPeekAmount(int i) {
                    this.entityData.set(DATA_PEEK_ID, (byte)i);
                    setBoundingBox(makeBoundingBox());
                }
            };
        }

    }

}
