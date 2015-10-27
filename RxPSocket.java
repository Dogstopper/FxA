import java.net.*;
import java.io.*;

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

  private byte[] inBuffer;
  private byte[] outBuffer;
  private InetAddress acceptedAddress;
  private int acceptedPort;

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
      inBuffer = new byte[MAX_PACKET_SIZE];
      outBuffer = new byte[MAX_PACKET_SIZE];

      // Accept a new client
      while (true) { // Loop until we get a SYN packet.
        DatagramPacket packet = new DatagramPacket(inBuffer, inBuffer.length);
        dgSocket.receive(packet);

        RxPPacket rxpPacket = RxPPacket.initializeFromDatagramPacket(packet);
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

}
