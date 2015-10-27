import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;

public class FxAServer {

	public static void main(String[] args) throws IOException {

		if (args.length != 1) { // Test for correct # of args
			throw new IllegalArgumentException("Parameter(s): <Port>");
		}

    int port = Integer.parseInt(args[0]);

    RxPSocket socket = new RxPSocket(port);

    byte[] inBuffer = new byte[FileMsgTextCoder.MAX_WIRE_LENGTH];
    MsgCoder coder = new FileMsgTextCoder();
    FileService service = new FileService();

    while (true) {
      RxPPacket packet = new RxPPacket(inBuffer, inBuffer.length);
      socket.receive(packet);
      byte[] encodedMsg = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
      System.out.println("Handling request from " + packet.getDatagramPacket().getSocketAddress() + " ("
          + encodedMsg.length + " bytes)");

      try {
        FileMsg msg = coder.fromWire(encodedMsg);
        msg = service.handleRequest(msg);
        packet.setData(coder.toWire(msg));
        System.out.println("Sending response (" + packet.getLength() + " bytes):");
        System.out.println(msg);
        socket.send(packet);
      } catch (IOException ioe) {
        System.err.println("Parse error in message: " + ioe.getMessage());
      }
    }
	}
}