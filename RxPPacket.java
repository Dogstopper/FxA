import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

public class RxPPacket {

  public static final int DEFAULT_PACKET_SIZE = 200;

  private DatagramPacket packet;
  private ByteBuffer data;

  public RxPPacket(byte[] buf, int length) {
    this.packet = new DatagramPacket(buf, length);
    this.data = ByteBuffer.wrap(packet.getData());
  }

  public RxPPacket(DatagramPacket packet) {
    this.packet = packet;
    this.data = ByteBuffer.wrap(packet.getData());
  }

  public RxPPacket() {
    this.data = allocate(DEFAULT_PACKET_SIZE);
    this.packet = new DatagramPacket(this.data.array(), DEFAULT_PACKET_SIZE);
  }

  //---- Masks and information pertaining to the header
  public static final int SOURCE_PORT_OFFSET = 0;
  public static final int DEST_PORT_OFFSET = 2;
  public static final int SEQ_NUMBER_OFFSET = 4;
  public static final int ACK_NUMBER_OFFSET = 8;
  public static final int FLAGS_BYTE_OFFSET = 13;
  public static final int WINDOW_SIZE_OFFSET = 15;
  public static final int CHECKSUM_OFFSET = 16;
  public static final int PAYLOAD_OFFSET = 20;

  public static final byte FIN_MASK = 0b00000001;
  public static final byte SYN_MASK = 0b00000010;
  public static final byte ACK_MASK = 0b00000100;
  public static final byte PSH_MASK = 0b00001000;

  //---- Functions that allow for getting and setting of bit flags
  public boolean isFIN() {
    byte result = (byte) ( this.data.get(FLAGS_BYTE_OFFSET) & FIN_MASK);
    return result == FIN_MASK;
  }

  public void setFIN(boolean set) {
    byte newByte = this.data.get(FLAGS_BYTE_OFFSET);
    if (set) {
      newByte = (byte)(newByte | FIN_MASK);
    } else {
      newByte = (byte)(newByte & ~FIN_MASK);
    }
  }

  public boolean isSYN() {
    byte result = (byte) ( this.data.get(FLAGS_BYTE_OFFSET) & SYN_MASK);
    return result == SYN_MASK;
  }

  public void setSYN(boolean set) {
    byte newByte = this.data.get(FLAGS_BYTE_OFFSET);
    if (set) {
      newByte = (byte)(newByte | SYN_MASK);
    } else {
      newByte = (byte)(newByte & ~SYN_MASK);
    }
  }

  public boolean isACK() {
    byte result = (byte) ( this.data.get(FLAGS_BYTE_OFFSET) & ACK_MASK);
    return result == ACK_MASK;
  }

  public void setACK(boolean set) {
    byte newByte = this.data.get(FLAGS_BYTE_OFFSET);
    if (set) {
      newByte = (byte)(newByte | ACK_MASK);
    } else {
      newByte = (byte)(newByte & ~ACK_MASK);
    }
  }

  public boolean isPSH() {
    byte result = (byte) ( this.data.get(FLAGS_BYTE_OFFSET) & PSH_MASK);
    return result == SYN_PSH;
  }

  public void setPSH(boolean set) {
    byte newByte = this.data.get(FLAGS_BYTE_OFFSET);
    if (set) {
      newByte = (byte)(newByte | PSH_MASK);
    } else {
      newByte = (byte)(newByte & ~PSH_MASK);
    }
  }

  //---- Getters and setters for RxPPacket items.
  public short getSrcPort() {
    return data.getShort(SOURCE_PORT_OFFSET);
  }

  public void setSrcPort(short port) {
    return data.setShort(SOURCE_PORT_OFFSET, port);
  }

  public short getDestPort() {
    return data.getShort(DEST_PORT_OFFSET);
  }

  public void setSrcPort(short port) {
    return data.setShort(DEST_PORT_OFFSET, port);
  }

  public int getSeqNum() {
    return data.getInt(SEQ_NUMBER_OFFSET);
  }

  public void setSeqNum(int seqNum) {
    return data.setShort(SEQ_NUMBER_OFFSET, seqNum);
  }

  public short getAckNum() {
    return data.getShort(ACK_NUMBER_OFFSET);
  }

  public void setAckNumber(int ack) {
    return data.setShort(ACK_NUMBER_OFFSET, port);
  }

  public short getWindowSize() {
    return data.getShort(WINDOW_SIZE_OFFSET);
  }

  public void setWindowSize(short port) {
    return data.setShort(WINDOW_SIZE_OFFSET, port);
  }

  public short getChecksum() {
    return data.getShort(CHECKSUM_OFFSET);
  }

  public void calculateChecksum() {
    // TODO: Implement.
  }

  // Returns the length that the payload can be.
  public int getLength() {
    return packet.getLength() - PAYLOAD_OFFSET;
  }

  // Retrives the payload data.
  public byte[] getData() {
    byte[] payload = new byte[getLength()];
    data.get(payload, PAYLOAD_OFFSET, payload.length);
    return payload;
  }

  // Sets the payload data
  public void setData(byte[] buf) {
    data.put(buf, PAYLOAD_OFFSET, getLength());
  }

  public DatagramPacket asDatagramPacket() {
    this.packet = new DatagramPacket(data, data.length,
        this.packet.getAddress(), getDestPort()); // Sync everything.
    return this.packet;
  }
}
