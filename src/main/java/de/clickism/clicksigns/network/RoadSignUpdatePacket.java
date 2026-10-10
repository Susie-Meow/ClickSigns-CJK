package de.clickism.clicksigns.network;

import de.clickism.clicksigns.ClickSigns;
import de.clickism.clicksigns.entity.RoadSignBlockEntity;
import de.clickism.clicksigns.platform.Platform;
import de.clickism.clicksigns.platform.network.Packet;
import de.clickism.clicksigns.platform.network.PacketType;
import de.clickism.clicksigns.sign.RoadSign;
import de.clickism.clicksigns.sign.codec.RoadSignCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.Nullable;

/**
 * Packet for updating a road sign
 *
 * @param pos      position of the road sign block entity
 * @param roadSign new road sign data
 */
public record RoadSignUpdatePacket(
    BlockPos pos,
    RoadSign roadSign
) implements Packet {

    /**
     * Squared distance a player may stand away from a road sign to still edit it.
     * <p>
     * The client picks the {@link BlockPos}, so this distance check is the only thing
     * stopping a client from editing road signs anywhere in the world. 8 blocks covers
     * vanilla interaction range with some slack for servers that raise it.
     */
    private static final double MAX_EDIT_DISTANCE_SQUARED = 64.0;

    /**
     * Maximum number of elements a single road sign may contain.
     */
    private static final int MAX_ELEMENTS = 256;

    /**
     * Checks whether the given sign is within the limits this mod can handle.
     * <p>
     * Sign data arrives from a client, so a malformed or hostile packet must not be able to
     * push absurd dimensions or element counts into the renderer - neither on the server nor
     * on any client the update is broadcast to.
     *
     * @param roadSign the sign to validate, may be null
     * @return true if the sign may be applied
     */
    private static boolean isValid(@Nullable RoadSign roadSign) {
        if (roadSign == null) return false;
        if (roadSign.width() < RoadSign.MIN_SIGN_SIZE.width()
            || roadSign.width() > RoadSign.MAX_SIGN_SIZE.width()) return false;
        if (roadSign.height() < RoadSign.MIN_SIGN_SIZE.height()
            || roadSign.height() > RoadSign.MAX_SIGN_SIZE.height()) return false;
        // Element count is already bounded by PacketCodec#MAX_LIST_SIZE during decoding;
        // this rejects nonsense earlier and with a cheaper check.
        return roadSign.elements().size() <= MAX_ELEMENTS;
    }

    public static final PacketType<RoadSignUpdatePacket> TYPE = new PacketType<>(
        ClickSigns.identifier("road_sign_update"),
        // Writer
        (buf, packet) -> {
            try {
                buf.writeBlockPos(packet.pos());
                RoadSignCodec.codec().writePacket(buf, packet.roadSign());
            } catch (Exception e) {
                ClickSigns.LOGGER.error("Error writing RoadSignUpdatePacket", e);
                // Rethrow on purpose: sending a half written buffer desyncs the whole
                // connection, which is far worse than dropping this single packet.
                throw e;
            }
        },
        // Reader
        (buf) -> {
            try {
                BlockPos pos = buf.readBlockPos();
                RoadSign roadSign = RoadSignCodec.codec().readPacket(buf);
                return new RoadSignUpdatePacket(pos, roadSign);
            } catch (Exception e) {
                ClickSigns.LOGGER.error("Error reading RoadSignUpdatePacket", e);
                // Rethrow instead of returning null, so the vanilla payload decoder can
                // reject the malformed packet. A null packet would only blow up later, on
                // another thread, as a useless NullPointerException.
                throw e;
            }
        },
        // Server Handler
        (packet, player) -> {
            try {
                //~ if >=26.1 'serverLevel()' -> 'level()'
                var level = player.level();
                var pos = packet.pos();
                // The position comes from the client, so verify the player is actually
                // standing next to the sign before accepting the change.
                if (player.distanceToSqr(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5
                    ) > MAX_EDIT_DISTANCE_SQUARED) {
                    return;
                }
                if (!isValid(packet.roadSign())) {
                    ClickSigns.LOGGER.warn(
                        "Rejected out-of-bounds road sign update from {} at {}",
                        player.getName().getString(), pos
                    );
                    return;
                }
                var blockEntity = level.getBlockEntity(pos);
                if (!(blockEntity instanceof RoadSignBlockEntity roadSignBlockEntity)) return;
                // Update road sign
                roadSignBlockEntity.updateRoadSign(packet.roadSign());
                Platform.network().sendToAllInLevel(level, packet);
            } catch (Exception e) {
                ClickSigns.LOGGER.error("Error handling RoadSignUpdatePacket", e);
            }
        },
        // Client Handler
        (packet) -> {
            try {
                // A malicious server could push the same garbage at us, so validate here too
                if (!isValid(packet.roadSign())) return;
                var client = Minecraft.getInstance();
                var level = client.level;
                if (level == null) return;
                var blockEntity = level.getBlockEntity(packet.pos());
                if (!(blockEntity instanceof RoadSignBlockEntity roadSignBlockEntity)) return;
                // Update road sign
                roadSignBlockEntity.updateRoadSign(packet.roadSign());
            } catch (Exception e) {
                ClickSigns.LOGGER.error("Error handling RoadSignUpdatePacket on client", e);
            }
        }
    );

    @Override
    public PacketType<?> type() {
        return TYPE;
    }
}