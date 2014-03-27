package dimes.measurements.nio.packet;

import java.nio.ByteBuffer;

/**
 * @author Udi Weinsberg
 *
 */
public abstract class PacketFinalizer {


	public abstract void finalizeBuffer(Packet packet, ByteBuffer packetBuffer);

}
