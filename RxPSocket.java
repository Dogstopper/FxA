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
  private int windowSize = 5; // Stop and Wait length

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

  public void setWindowSize(int windowSize) {
    this.windowSize = windowSize;
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
    private int numTimesSilent = 0;

    public ResendTimerTask(int oldestUnackedPointer, int windowSize, RxPPacket[] sendBuffer) {
      this.oldestUnackedPointer = oldestUnackedPointer;
      this.windowSize = windowSize;
      this.sendBuffer = sendBuffer;
    }

    public boolean incrementOldestUnackedPointer() {
      numTimesSilent = 0;
      if (oldestUnackedPointer < sendBuffer.length) {
        oldestUnackedPointer++;
        return true;
      }
      return false;
    }

    public int getNumTimesNoResponse() {
      return numTimesSilent;
    }

    public void resetTimesNoResponse() {
      numTimesSilent = 0;
    }

    public void run() {

      Connection connection = connectionManager.getConnection(sendBuffer[0]);

      numTimesSilent++;
      if (connection.isAllowedToSendData()) {

        System.out.println("Resending " + Math.min(windowSize, sendBuffer.length-oldestUnackedPointer) + " packets");

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
  public boolean send(byte[] sendBuffer) {
    int oldestUnackedPointer = 0;
    boolean PSH_ACKsent = false;

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
    }

    Timer timer = new Timer();

    // Resend the current data every 300ms there is not an ACKed packet.
    ResendTimerTask resendTask = new ResendTimerTask(oldestUnackedPointer, windowSize, packetBuffer);
    timer.scheduleAtFixedRate(resendTask, 0, timeoutMillis);

    // TODO: 2) Set Window Size
    byte[] packetPayload = new byte[RxPPacket.DEFAULT_PACKET_SIZE];
    DatagramPacket dgPacket = new DatagramPacket(packetPayload, packetPayload.length);

    while (true) {
      try {
        // Wait for the ACK
        dgSocket.receive(dgPacket);

        resendTask.resetTimesNoResponse();
        RxPPacket rxpPacket = new RxPPacket(dgPacket);

        if (!rxpPacket.isSYN() && rxpPacket.isACK()) {

          int ackNumber = rxpPacket.getACKNum();
          System.out.println("ACK Received: " + ackNumber);

          // If the ACK is equal to the oldest unACKed packet, move the index by one
          // Otherwise, wait until it comes OR timeout occurs.
          if (oldestUnackedPointer < packetBuffer.length &&
              ackNumber == packetBuffer[oldestUnackedPointer].getSeqNum()) {
            oldestUnackedPointer++;
            boolean success = resendTask.incrementOldestUnackedPointer();

            // Reset timer
            timer.cancel();
            timer.purge();
            timer = new Timer();
            resendTask = new ResendTimerTask(oldestUnackedPointer, windowSize, packetBuffer);
            timer.scheduleAtFixedRate(resendTask, timeoutMillis, timeoutMillis);
          }

          if (rxpPacket.isPSH() && rxpPacket.isACK()) {
            // If the received packet is a PSH+ACK, then send a PSH+ACK and quit.
            RxPPacket ackRxPPacket = new RxPPacket();
            ackRxPPacket.setACK(true);
            ackRxPPacket.setACKNum(oldestUnackedPointer+1);
            ackRxPPacket.setSeqNum(oldestUnackedPointer+1);
            ackRxPPacket.setDestPort((short)dgPacket.getPort());
            ackRxPPacket.setSrcPort((short)dgSocket.getLocalPort());
            ackRxPPacket.setPSH(true);
            ackRxPPacket.setChecksum(ackRxPPacket.calculateChecksum());

            // Send ACK+PSH
            DatagramPacket dg = ackRxPPacket.asDatagramPacket();
            dg.setAddress(dgPacket.getAddress());
            dg.setPort(dgPacket.getPort());
            dgSocket.send(dg);
            System.out.println("Sending PSH+ACK");
            timer.cancel();
            return true;
          }
        }
      } catch(IOException e) {
        System.err.println("Received Nothing " + resendTask.getNumTimesNoResponse() + " Times");
        if (resendTask.getNumTimesNoResponse() == 40) {
          timer.cancel();
          return false;
        }
      }
    }
  }

  public byte[] receive() throws IOException {
    int expectedSeqNum = 1; // Initial Number

    boolean PSH_ACKsent = false;
    int receiveAttempts = 0;

    int timeoutMillis = 300;
    try {
      dgSocket.setSoTimeout(timeoutMillis);
    } catch(SocketException se) {
      // TODO: Add error handling
    }

    // Add to temp buffer
    List<RxPPacket> tempRxPPacketList = new ArrayList<>();
    DatagramPacket dgPacket = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
    // Receive until PSH flag is set or timeout occurs
    while (true) {
      // Receive Datagram from Datagram Socket
      try {
        dgSocket.receive(dgPacket);
      } catch (SocketTimeoutException ste) {
        receiveAttempts++;
        if (PSH_ACKsent || receiveAttempts > 40) {
          break;
        }
        else {
          continue;
        }
      }
      receiveAttempts = 0;

      // Create RxPPacket from received Datagram Buffer
      RxPPacket receivedRxPPacket = new RxPPacket(dgPacket.getData());

      // Check the checksum to make sure no corruption occurred
      long checksum = receivedRxPPacket.getChecksum();
      long currentChecksum = receivedRxPPacket.calculateChecksum();

      System.out.println("Expected: " + expectedSeqNum + "\tReceived: " + receivedRxPPacket.getSeqNum());

      if (currentChecksum != checksum) {
        System.out.println("Packet Corrupted");
      }
      else if (receivedRxPPacket.isPSH() && receivedRxPPacket.isACK() && PSH_ACKsent) {
        break;
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

          // Only add to the list if this is the right packet. ACK any other one.
          if (expectedSeqNum == receivedRxPPacket.getSeqNum() && !receivedRxPPacket.isACK()) {
            tempRxPPacketList.add(receivedRxPPacket);
            expectedSeqNum = receivedRxPPacket.getSeqNum() + 1;
          }
          else {
            if (!receivedRxPPacket.isACK())
              System.out.println("Packet Out of Order");
          }

          // Make an ACK
          RxPPacket ackRxPPacket = new RxPPacket();
          ackRxPPacket.setACK(true);
          ackRxPPacket.setACKNum(receivedRxPPacket.getSeqNum());
          ackRxPPacket.setDestPort((short)dgPacket.getPort());
          ackRxPPacket.setSrcPort((short)dgSocket.getLocalPort());
          if (receivedRxPPacket.isPSH()) {
            ackRxPPacket.setPSH(true);
            PSH_ACKsent = true;
            System.out.println("Sending PSH+ACK");
          }
          ackRxPPacket.setChecksum(ackRxPPacket.calculateChecksum());

          // Send ACK
          DatagramPacket dg = ackRxPPacket.asDatagramPacket();
          dg.setAddress(dgPacket.getAddress());
          dg.setPort(dgPacket.getPort());
          dgSocket.send(dg);

          System.out.println("Sending ACK: " + ackRxPPacket.getACKNum());
        }
      }
    }

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
}
