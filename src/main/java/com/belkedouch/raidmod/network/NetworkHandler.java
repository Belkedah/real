package com.belkedouch.raidmod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("raidmod", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    /**
     * Call this method during mod setup to register all packets.
     */
    public static void registerPackets() {
        // Example packet registration
        registerMessage(ExamplePacket.class, ExamplePacket::encode, ExamplePacket::decode, ExamplePacket::handle, NetworkDirection.PLAY_TO_SERVER);
    }

    private static <T> void registerMessage(Class<T> clazz,
                                            BiConsumer<T, FriendlyByteBuf> encoder,
                                            Function<FriendlyByteBuf, T> decoder,
                                            BiConsumer<T, Supplier<NetworkEvent.Context>> handler,
                                            NetworkDirection direction) {
        CHANNEL.registerMessage(packetId++, clazz, encoder, decoder, handler, Optional.of(direction));
    }

    // Example packet class
    public static class ExamplePacket {
        private final int data;

        public ExamplePacket(int data) {
            this.data = data;
        }

        public static void encode(ExamplePacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.data);
        }

        public static ExamplePacket decode(FriendlyByteBuf buf) {
            return new ExamplePacket(buf.readInt());
        }

        public static void handle(ExamplePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
            NetworkEvent.Context ctx = ctxSupplier.get();
            ctx.enqueueWork(() -> {
                // Handle packet on server/client
                System.out.println("Received packet with data: " + msg.data);
            });
            ctx.setPacketHandled(true);
        }
    }
}
