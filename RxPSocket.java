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
  private State currentState;
  private Event event;
  private int oldestUnackedPointer; // oldest unACKed pointer
  private int windowSize = 1; // Stop and Wait length

  // Variables that are set in the LISTEN/ACCEPT phases.
  // private byte[] inBuffer;
  // private byte[] outBuffer;
  private InetAddress acceptedAddress;
  // private int acceptedPort;

  // TODO: Add a timer for timeouts.

  public RxPSocket()
      throws SocketException {
    dgSocket = new DatagramSocket();
    currentState = State.CLOSED;
  }

  public RxPSocket(int port)
      throws SocketException {
    dgSocket = new DatagramSocket(port);
    currentState = State.CLOSED;
	}

	public RxPSocket(int port, InetAddress address)
      throws SocketException {
    dgSocket = new DatagramSocket(port, address);
    currentState = State.CLOSED;
	}

  // Moves the state from one to another if an event
  // is accepted in that state.
  private boolean stateMachineTransition() {
    try {
      switch (this.currentState) {
        case CLOSED:
          if (this.event == Event.LISTEN) {
            System.out.println("Moving from CLOSED to LISTEN");
            this.currentState = State.LISTEN;
          }
          else if (this.event == Event.CONNECT) {
            System.out.println("Moving from CLOSED to SYN_SENT");
            this.currentState = State.SYN_SENT;
          }
          break;

        case LISTEN:
          break;
          // Implement

        default:
          // Check for timeout.
          break;
      }
    } catch(Exception e) {
      // TODO: Error Handling
    }
    return true;
  }

  // Implement the state behavior. It's consolidated in the switch
  // so that when timeouts occur, it's trivial to repeat a state
  private void stateMachineAction() {
    try {
      switch (this.currentState) {
        case CLOSED:
          break;
        case LISTEN:
          handleListen();
          break;
      }
    } catch(Exception e) {
      // Handle this crap
    }
  }

  private void handleListen() throws IOException {
    // We need to allocate space for the buffers
    if (currentState == State.CLOSED) {
      byte[] inBuffer = new byte[MAX_PACKET_SIZE];
      byte[] outBuffer = new byte[MAX_PACKET_SIZE];

      // Accept a new client
      while (true) { // Loop until we get a SYN packet.
        DatagramPacket packet = new DatagramPacket(inBuffer, inBuffer.length);
        dgSocket.receive(packet);

        RxPPacket rxpPacket = new RxPPacket(packet);
        if (rxpPacket.isSYN()) {
          break;
        }
      }
    }
  }
  // Triggers a change into the LISTEN state.
  public void listen()
      throws IOException {
        event = Event.LISTEN;
        stateMachineTransition();
  }

  /* Connect, Send, and Receive Methods */
  public void connect(InetAddress address, int port) {
    dgSocket.connect(address, port);
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

    public void run() {
      // Send all unsent packets
      for (int i = oldestUnackedPointer; i < windowSize && i < sendBuffer.length; i++) {
        dgSocket.send(sendBuffer[i].asDatagramPacket());
      }
    }
  }

  public void send(byte[] sendBuffer) {
    int oldestUnackedPointer = 0;
    int packetBufferLength = (int)Math.ceil((double)(sendBuffer.length) / RxPPacket.DEFAULT_PACKET_SIZE);

    // Set a receive timeout so we can resend after a time, rather
    // than just having it block
    int timeoutMillis = 300;
    dgSocket.setSoTimeout(timeoutMillis);

    RxPPacket[] packetBuffer = new RxPPacket[packetBufferLength];
    // Create payload for each RxPPacket based on default packet size
    for (int i = 0; i < packetBuffer.length; i += 1) {
      byte[] payload = Arrays.copyOfRange(sendBuffer, i*RxPPacket.DEFAULT_PACKET_SIZE,
            RxPPacket.DEFAULT_PACKET_SIZE);
      RxPPacket newPacket = new RxPPacket(payload, payload.length);
      newPacket.setSeqNum(i); // TODO: Change this;
      newPacket.setDestPort((short)dgSocket.getPort());
      newPacket.setSrcPort((short)dgSocket.getLocalPort());
      packetBuffer[i] = new RxPPacket(payload, payload.length);
    }

    // Add PSH flag
    packetBuffer[packetBuffer.length - 1].setPSH(true);

    // The timer task is charged with resending all packets in the window
    // every time it is fired. It needs to be encapsulated in a class because
    // Async tasks need to work on constants, not variables.
    Timer timer = new Timer();

    // Resend the current data every 300ms there is not an ACKed packet.
    timer.scheduleAtFixedRate(
      new ResendTimerTask(oldestUnackedPointer, windowSize, packetBuffer),
    0, timeoutMillis);

    // TODO: 2) Set Window Size
    byte[] packetPayload = new byte[RxPPacket.DEFAULT_PACKET_SIZE];
    DatagramPacket packet = new DatagramPacket(packetPayload, packetPayload.length);

    while (oldestUnackedPointer != packetBuffer.length) {
      try {
        dgSocket.receive(packet);
        RxPPacket rxpPacket = new RxPPacket(packet);
        if (rxpPacket.isACK()) {
          int ackNumber = rxpPacket.getAckNum();

          // If the ACK is equal to the oldest unACKed packet, move the index by one
          // Otherwise, wait until it comes OR timeout occurs.
          if (ackNumber == packetBuffer[oldestUnackedPointer].getSeqNum()) {
            oldestUnackedPointer++;
          }
        }
      } catch(IOException e) {
        // TODO: Handle Error;
      }
    }
  }

  public byte[] receive(int length) throws IOException {
    // DatagramPacket dgPacket = packet.getDatagramPacket();
    // dgSocket.receive(dgPacket);
    // packet.setDatagramPacket(dgPacket);
  }
}
