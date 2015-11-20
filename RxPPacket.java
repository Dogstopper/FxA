import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

// Libraries for debugging
import java.util.Arrays;
import java.util.logging.*;

public class RxPPacket {

  public static final Logger log = Logger.getLogger(RxPPacket.class.getName());

  public static final int DEFAULT_PACKET_SIZE = 200;

  private ByteBuffer data;

  public RxPPacket(byte[] buf) {
    //this.packet = new DatagramPacket(buf, length+PAYLOAD_OFFSET);
    this.data = ByteBuffer.wrap(buf);
  }

  public RxPPacket(DatagramPacket packet) {
    //this.packet = packet;
    this.data = ByteBuffer.wrap(packet.getData());
  }

  public RxPPacket() {
    this.data = ByteBuffer.allocate(DEFAULT_PACKET_SIZE);
    //this.packet = new DatagramPacket(this.data.array(), DEFAULT_PACKET_SIZE);
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
    return result == PSH_MASK;
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
    data.putShort(SOURCE_PORT_OFFSET, port);
  }

  public short getDestPort() {
    return data.getShort(DEST_PORT_OFFSET);
  }

  public void setDestPort(short port) {
    data.putShort(DEST_PORT_OFFSET, port);
  }

  public int getSeqNum() {
    return data.getInt(SEQ_NUMBER_OFFSET);
  }

  public void setSeqNum(int seqNum) {
    data.putInt(SEQ_NUMBER_OFFSET, seqNum);
  }

  public int getACKNum() {
    return data.getInt(ACK_NUMBER_OFFSET);
  }

  public void setACKNum(int ack) {
    data.putInt(ACK_NUMBER_OFFSET, ack);
  }

  public short getWindowSize() {
    return data.getShort(WINDOW_SIZE_OFFSET);
  }

  public void setWindowSize(short port) {
    data.putShort(WINDOW_SIZE_OFFSET, port);
  }

  public short getChecksum() {
    return data.getShort(CHECKSUM_OFFSET);
  }

  public void calculateChecksum() {
    // TODO: Implement.
  }

  // Returns the length that the payload can be.
  public int getLength() {
    return DEFAULT_PACKET_SIZE;
  }

  // TODO: Fix Index Out of Bounds
  // Retrives the payload data.
  public byte[] getPayload() {
    // Fix this bug, shouldn't be * 2
    byte[] payload = new byte[(int) (getLength() * 1.1)];
    data.get(payload, PAYLOAD_OFFSET, getLength());
    
    // Log Payload
    log.logrb(Level.INFO, "RxPPacket", "getPayload()", "byte[] payload", javax.xml.bind.DatatypeConverter.printHexBinary(payload));
    
    return payload;
  }

  // Sets the payload data
  public void setPayload(byte[] buf) {
    data.put(buf, PAYLOAD_OFFSET, getLength());
  }

  public DatagramPacket asDatagramPacket() {
    return new DatagramPacket(data.array(), data.array().length);
  }
}
