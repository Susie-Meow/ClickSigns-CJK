package de.clickism.clicksigns.serialization.codec;

import de.clickism.clicksigns.serialization.PacketReader;
import de.clickism.clicksigns.serialization.PacketWriter;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface PacketCodec<T> {
    /**
     * The maximum number of elements a single decoded list may contain.
     * <p>
     * List lengths are read straight off the wire, so without an upper bound a client could
     * declare an arbitrary size and make the receiver allocate a matching array right away.
     */
    int MAX_LIST_SIZE = 4096;

    static <T> PacketCodec<T> of(PacketWriter<T> writer, PacketReader<T> reader) {
        return new PacketCodec<>() {
            @Override
            public PacketWriter<T> packetWriter() {
                return writer;
            }

            @Override
            public PacketReader<T> packetReader() {
                return reader;
            }
        };
    }

    PacketWriter<T> packetWriter();

    PacketReader<T> packetReader();

    static <T> void writeCollection(FriendlyByteBuf buf, Collection<T> collection, PacketWriter<T> writer) {
        buf.writeVarInt(collection.size());
        for (T element : collection) {
            writer.write(buf, element);
        }
    }

    static <T> List<T> readList(FriendlyByteBuf buf, PacketReader<T> reader) {
        int size = buf.readVarInt();
        // Reject negative sizes (readVarInt can produce them) and absurdly large ones,
        // otherwise new ArrayList<>(size) would allocate whatever the client asked for.
        if (size < 0 || size > MAX_LIST_SIZE) {
            throw new IllegalArgumentException("List size out of bounds: " + size);
        }
        // Don't pre-allocate the full size: the list is grown as elements are actually read,
        // so a truncated buffer can't make us hold on to a large empty array either.
        List<T> list = new ArrayList<>(Math.min(size, 64));
        for (int i = 0; i < size; i++) {
            list.add(reader.read(buf));
        }
        return list;
    }
}
