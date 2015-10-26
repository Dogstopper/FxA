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

  // Private variables used in several states
  private DatagramSocket dgSocket;
  private State currentState;
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

  public void listen()
      throws IOException {
    // We need to allocate space for the buffers
    if (currentState == State.CLOSED) {
      inBuffer = new byte[MAX_PACKET_SIZE];
      outBuffer = new byte[MAX_PACKET_SIZE];
      currentState = State.LISTEN;

      // Accept a new client
      while (true) {
        DatagramPacket packet = new DatagramPacket(inBuffer, inBuffer.length);
        dgSocket.receive(packet);
        byte[] encodedMsg = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        System.out.println("Handling request from " + packet.getSocketAddress() + " ("
            + encodedMsg.length + " bytes)");
        // Check for SYN set
      }
    }
  }

}
