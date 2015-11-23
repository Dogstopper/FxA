import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


public class RxPPacket {

  public static final int DEFAULT_PACKET_SIZE = 200;

  private ByteBuffer data;

  public RxPPacket(byte[] buf) {
    //System.out.println("Received Buffer: " + javax.xml.bind.DatatypeConverter.printHexBinary(buf));
    byte[] dataBytes = Arrays.copyOfRange(buf, 0, DEFAULT_PACKET_SIZE);
    System.out.println("Received Data: " + javax.xml.bind.DatatypeConverter.printHexBinary(dataBytes));

    this.data = ByteBuffer.wrap(dataBytes);
  }

  public RxPPacket(DatagramPacket packet) {
    //this.packet = packet;
    this.data = ByteBuffer.wrap(packet.getData());
    // this.setPayload(packet.getData());
  }

  public RxPPacket() {
    this.data = ByteBuffer.allocate(DEFAULT_PACKET_SIZE);
    //this.packet = new DatagramPacket(this.data.array(), DEFAULT_PACKET_SIZE);
  }

  // TODO: correctly put all attributes in buffer...
  public RxPPacket(short src, short dest, int seq, int ackNum, boolean fin, boolean syn, boolean ack, boolean psh) {
    this.data = ByteBuffer.allocate(DEFAULT_PACKET_SIZE + 20);
    this.setSrcPort(src);
    this.setDestPort(dest);
    this.setSeqNum(seq);
    this.setACKNum(ackNum);
    this.setFIN(fin);
    this.setSYN(syn);
    this.setACK(ack);
    this.setPSH(psh);
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
    this.data.put(FLAGS_BYTE_OFFSET, newByte);
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
    this.data.put(FLAGS_BYTE_OFFSET, newByte);
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
    this.data.put(FLAGS_BYTE_OFFSET, newByte);
  }

  public boolean isPSH() {
    System.out.println("FLAGS: " + this.data.get(FLAGS_BYTE_OFFSET));
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
    this.data.put(FLAGS_BYTE_OFFSET, newByte);
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

  // Retrives the payload data.
  public byte[] getPayload() {

    // Read ByteBuffer into ByteList
    data.rewind();
    List<Byte> byteList = new ArrayList<>();
    while (data.hasRemaining()) {
      byteList.add(data.get());
    }

    // Reset Cursor
    data.position(byteList.size());

    // convert Byte List to byte array
    byte[] payload = new byte[byteList.size()];
    for (int i = 0; i < payload.length; i++) {
      payload[i] = byteList.get(i);
    }

    return payload;
  }

  // Sets the payload data
  public void setPayload(byte[] buf) {

    for (int i = 0; i < buf.length; i++) {
      data.put(i + PAYLOAD_OFFSET, buf[i]);
    }
  }

  // Return everything that's not the header
  public byte[] getPacketData() {
    byte[] payload = getPayload();

    byte[] packetData = Arrays.copyOfRange(payload, PAYLOAD_OFFSET, payload.length);

    return packetData;
  }

  public DatagramPacket asDatagramPacket() {
    return new DatagramPacket(data.array(), data.array().length);
  }
}
