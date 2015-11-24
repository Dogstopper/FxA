import java.net.*;
import java.io.*;
import java.util.*;

public class RxPSocket {

  public static final int MAX_PACKET_SIZE = 2000;

  // States that we could be in
  private enum State {
      CLOSED, SYN_SENT, SYN_REC, LISTEN, ESTABLISHED,
      FIN_WAIT_1, FIN_WAIT_2, CLOSING, CLOSE_WAIT,
      TIMED_WAIT, LAST_ACK
  }

  // Events that occur to move between states.
  private enum Event {
    SYN, LISTEN, CONNECT, SEND, CLOSE, ACK, SYNACK,
    FIN, TIMEOUT
  }

  // Private variables used in several states
  private DatagramSocket dgSocket;
  private int oldestUnackedPointer; // oldest unACKed pointer
  private int windowSize = 1; // Stop and Wait length

  // Variables that are set in the LISTEN/ACCEPT phases.
  // private byte[] inBuffer;
  // private byte[] outBuffer;
  private InetAddress acceptedAddress;
  ///private int acceptedPort;

  private ConnectionManager connectionManager;

  // TODO: Add a timer for timeouts.

  public RxPSocket()
      throws SocketException {
    dgSocket = new DatagramSocket();
    connectionManager = new ConnectionManager();
  }

  public RxPSocket(int port)
      throws SocketException {
    dgSocket = new DatagramSocket(port);
    connectionManager = new ConnectionManager();
	}

	public RxPSocket(int port, InetAddress address)
      throws SocketException {
    dgSocket = new DatagramSocket(port, address);
    connectionManager = new ConnectionManager();
	}

  /* Connect, Send, and Receive Methods */
  public void connect(InetAddress address, int port) {
    dgSocket.connect(address, port);

    // Initiate handshake
    short src = (short) dgSocket.getLocalPort();
    short dest = (short) dgSocket.getPort();

    Connection connection = connectionManager.getConnection(dest, src);

    // Handshake Packet
    RxPPacket handshakePacket = connectionManager.getNextHandshakePacket(connection);
    
    try {
      DatagramPacket dg = handshakePacket.asDatagramPacket();

      // Send Handshake Packet
      dgSocket.send(dg);
      connectionManager.updateConnection(handshakePacket);

    } catch (IOException e) {
      System.out.println("Handshake Packet did not send");
    }

    // Listen for next handshake packet
    try {
      this.handshake();
    } catch (IOException e) {
      System.out.println("Could not receive handshake packet");
    }
  }

  public void listen() {

    DatagramPacket dgPacket = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);

    try 
    {
      this.handshake();
    } catch (IOException ioe) {
      System.out.println("Handshake Packet IOException");
    }
  }

  public void handshake() throws IOException {

    DatagramPacket dgPacket = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);

    boolean allowedToSendData = false;
    while (!allowedToSendData) {
      dgSocket.receive(dgPacket);

      RxPPacket receivedRxPPacket = new RxPPacket(dgPacket.getData());

      // Check the checksum to make sure no corruption occurred
      long checksum = receivedRxPPacket.getChecksum();
      long currentChecksum = receivedRxPPacket.calculateChecksum();
      if (currentChecksum == checksum) {

        // If it's a handshake packet, send the next handshake packet
        if (connectionManager.updateConnection(receivedRxPPacket)) {

          System.out.println("Received Handshake Request: " + connectionManager.getConnection(receivedRxPPacket).connectionStateToString());

          Connection connection = connectionManager.getConnection(receivedRxPPacket);
          RxPPacket handshakePacket = connectionManager.getNextHandshakePacket(connection);

          if (handshakePacket != null) { 
            
            connectionManager.updateConnection(handshakePacket);
  
            // Send handshake packet as datagram
            // if (!connectionManager.getConnection(receivedRxPPacket).isAllowedToSendData()) {
              DatagramPacket dg = handshakePacket.asDatagramPacket();
              dg.setAddress(dgPacket.getAddress());
              dg.setPort(dgPacket.getPort());
              dgSocket.send(dg);
            // }
            
            System.out.println("Sending Handshake Response: " + connectionManager.getConnection(handshakePacket).connectionStateToString());
          }

          // Update while loop condition
          allowedToSendData = connection.isAllowedToSendData();
        }
      } else {
        System.out.println("Handshake Packet Corrupted");
      }
    }
  }

  private class ResendTimerTask extends TimerTask {
    private int oldestUnackedPointer;
    private int windowSize;
    private RxPPacket[] sendBuffer;

    public ResendTimerTask(int oldestUnackedPointer, int windowSize, RxPPacket[] sendBuffer) {
      this.oldestUnackedPointer = oldestUnackedPointer;
      this.windowSize = windowSize;
      this.sendBuffer = sendBuffer;
    }

    public boolean incrementOldestUnackedPointer() {
      if (oldestUnackedPointer < sendBuffer.length) {
        oldestUnackedPointer++;
        return true;
      }
      return false;
    }

    public void run() {

      Connection connection = connectionManager.getConnection(sendBuffer[0]);

      if (connection.isAllowedToSendData()) {

        System.out.println("Resending all packets");
        // Send all unsent packets
        for (int i = 0; i < windowSize && i < sendBuffer.length-oldestUnackedPointer; i++) {
          try {
            DatagramPacket dg = sendBuffer[i+oldestUnackedPointer].asDatagramPacket();
            dg.setAddress(dgSocket.getInetAddress());
            dg.setPort(dgSocket.getPort());
            dgSocket.send(dg);
          } catch(IOException e) {
            // ACtually do not care. Timer will requeue these.
          }
        }
      }
    }
  }

  // Application sends a buffer, send creates RxPPacket from buffer then...
  // TODO: send window of packets
  public void send(byte[] sendBuffer) {
    int oldestUnackedPointer = 0;
    int packetBufferLength = (int) Math.ceil((double) (sendBuffer.length) / RxPPacket.DEFAULT_PACKET_SIZE);

    // Set a receive timeout so we can resend after a time, rather
    // than just having it block
    int timeoutMillis = 300;
    try {
      dgSocket.setSoTimeout(timeoutMillis);
    } catch(SocketException se) {
      // TODO: Add error handling
    }

    RxPPacket[] packetBuffer = new RxPPacket[packetBufferLength];
    // Create payload for each RxPPacket based on default packet size
    for (int i = 0; i < packetBuffer.length; i += 1) {
      byte[] payload = Arrays.copyOfRange(sendBuffer, i*RxPPacket.DEFAULT_PACKET_SIZE,
            (i+1)*RxPPacket.DEFAULT_PACKET_SIZE);

      short src = (short) dgSocket.getLocalPort();
      short dest = (short) dgSocket.getPort();
      int seqNum = i+1;
      int ackNum = 0;
      boolean fin = false;
      boolean syn = false;
      boolean ack = false;
      boolean psh = (i == packetBuffer.length - 1) ? true : false;
      RxPPacket newPacket = new RxPPacket(src,
                                          dest,
                                          seqNum,
                                          ackNum,
                                          fin,
                                          syn,
                                          ack,
                                          psh);
      newPacket.setPayload(payload);
      newPacket.setChecksum(newPacket.calculateChecksum());

      packetBuffer[i] = newPacket;

      System.out.println("Packet " + i + ": " + javax.xml.bind.DatatypeConverter.printHexBinary(newPacket.getPayload()));
    }

    // Add PSH flag
    //packetBuffer[packetBuffer.length - 1].setPSH(true);

    // The timer task is charged with resending all packets in the window
    // every time it is fired. It needs to be encapsulated in a class because
    // Async tasks need to work on constants, not variables.
    Timer timer = new Timer();

    // Resend the current data every 300ms there is not an ACKed packet.
    ResendTimerTask resendTask = new ResendTimerTask(oldestUnackedPointer, windowSize, packetBuffer);
    timer.scheduleAtFixedRate(resendTask, 0, timeoutMillis);

    // TODO: 2) Set Window Size
    byte[] packetPayload = new byte[RxPPacket.DEFAULT_PACKET_SIZE];
    DatagramPacket dgPacket = new DatagramPacket(packetPayload, packetPayload.length);

    while (oldestUnackedPointer != packetBuffer.length) {
      try {
        // Wait for the ACK
        dgSocket.receive(dgPacket);
        RxPPacket rxpPacket = new RxPPacket(dgPacket);

        if (!rxpPacket.isSYN() && rxpPacket.isACK()) {

          int ackNumber = rxpPacket.getACKNum();
          System.out.println("ACK Received: " + ackNumber);

          // If the ACK is equal to the oldest unACKed packet, move the index by one
          // Otherwise, wait until it comes OR timeout occurs.
          if (ackNumber == packetBuffer[oldestUnackedPointer].getSeqNum()) {
            oldestUnackedPointer++;
            boolean success = resendTask.incrementOldestUnackedPointer();
            //timer.scheduleAtFixedRate(resendTask, 0, timeoutMillis);
          }
        }
      } catch(IOException e) {
        // TODO: Handle Error;
      }
    }
    timer.cancel();
  }

  // TODO: receive packet
  // TODO: receive length?
  public byte[] receive() throws IOException {

    // Add to temp buffer
    List<RxPPacket> tempRxPPacketList = new ArrayList<>();
    DatagramPacket dgPacket = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
    // Receive until PSH flag is set or timeout occurs
    while (true) {
      // Receive Datagram from Datagram Socket
      dgSocket.receive(dgPacket);

      //System.out.println("dgPacket.getData(): " + javax.xml.bind.DatatypeConverter.printHexBinary(dgPacket.getData()));

      // Create RxPPacket from received Datagram Buffer
      RxPPacket receivedRxPPacket = new RxPPacket(dgPacket.getData());

      //System.out.println("receivedRxPPacket.getPayload(): " + javax.xml.bind.DatatypeConverter.printHexBinary(receivedRxPPacket.getPayload()));

      // Check the checksum to make sure no corruption occurred
      long checksum = receivedRxPPacket.getChecksum();
      long currentChecksum = receivedRxPPacket.calculateChecksum();
      if (currentChecksum != checksum) {
        System.out.println("Packet Corrupted");
      }
      else {

        // If it's a handshake packet, send the next handshake packet
        if (connectionManager.updateConnection(receivedRxPPacket)) {

          Connection connection = connectionManager.getConnection(receivedRxPPacket);
          RxPPacket handshakePacket = connectionManager.getNextHandshakePacket(connection);

          // Send handshake packet as datagram
          DatagramPacket dg = handshakePacket.asDatagramPacket();
          dg.setAddress(dgPacket.getAddress());
          dg.setPort(dgPacket.getPort());
          dgSocket.send(dg);

          connectionManager.updateConnection(handshakePacket);

          System.out.println("Sending Handshake Response: " + connectionManager.getConnection(handshakePacket).connectionStateToString());

        } else { // Otherwise send ACK

          // Add to PacketList that gets passed up to Application
          tempRxPPacketList.add(receivedRxPPacket);

          // Make an ACK
          RxPPacket ackRxPPacket = new RxPPacket();
          ackRxPPacket.setACK(true);
          ackRxPPacket.setACKNum(receivedRxPPacket.getSeqNum());
          ackRxPPacket.setDestPort((short)dgPacket.getPort());
          ackRxPPacket.setSrcPort((short)dgSocket.getLocalPort());

          // Send ACK
          DatagramPacket dg = ackRxPPacket.asDatagramPacket();
          dg.setAddress(dgPacket.getAddress());
          dg.setPort(dgPacket.getPort());
          dgSocket.send(dg);

          System.out.println("Sending ACK: " + ackRxPPacket.getACKNum());

          if (receivedRxPPacket.isPSH()) {
            // Make sure the last ACK is received
            break;
          }
        }
      }
    }

    // // TODO: expectedSeqNum should not reset everytime receive is called.
    // // What is the expected sequence number of the first packet? 1?
    // int expectedSeqNum = 1;
    //
    // // TODO: Make sure received packets are not corrupt, duplicate, out of order
    // for (RxPPacket tempPacket : tempRxPPacketList) {
    //
    //   // send handles duplicate packets, check if corrupted or out of order
    //   if (isCorrupt(tempPacket) || isOutOfOrder(tempPacket, expectedSeqNum)) {
    //     // TODO: Timeout
    //   }
    //   expectedSeqNum++;
    // }

    // If tempPackets pass all the tests (not dup, corrupt, out of order)
    // give the application a byte buffer
    List<Byte> receivedByteList = new ArrayList<>();
    for (RxPPacket tempPacket : tempRxPPacketList) {
        
      // getPayload returns an array of bytes, add each byte to the byteList
      for (int i = 0; i < tempPacket.getPacketData().length; i++) {
        receivedByteList.add(tempPacket.getPacketData()[i]);
      }
    }

    // Byte to byte ...
    byte[] receivedBytes = new byte[receivedByteList.size()];
    for (int i = 0; i < receivedBytes.length; i++) {
      receivedBytes[i] = receivedByteList.get(i);
    }

    return receivedBytes;
  }

  // TODO: Check if corrupted via checksum
  public static boolean isCorrupt(RxPPacket rxpPacket) {
    return false;
  }

  // TODO: Check if packet is out of order
  public static boolean isOutOfOrder(RxPPacket rxpPacket, int expectedSeqNum) {

    // Expected sequence number is last packet number + 1
    return false;
  }
}
