// java FxAServer 8081
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

    // byte[] inBuffer = new byte[FileMsgTextCoder.MAX_WIRE_LENGTH];
    MsgCoder coder = new FileMsgTextCoder();
    FileService service = new FileService();

    socket.listen();

    while (true) {

      // Receive Buffer
      byte[] inBuffer = socket.receive();
      //System.out.println("Application Layer: " + javax.xml.bind.DatatypeConverter.printHexBinary(inBuffer));
      System.out.println(new String(inBuffer));

      // TODO: Try to handle this from buffer/socket, should not have to create packet
      // TODO: Print this info from socket, not packet
      // System.out.println("Handling request from " + packet.asDatagramPacket().getSocketAddress() + " (" + encodedMsg.length + " bytes)");

      try {
        // open encoded message as a FileMsg
        FileMsg msg = coder.fromWire(inBuffer);
        msg = service.handleRequest(msg);

        // Send response (byte[]) from handledRequest
        byte[] bytesToSend = coder.toWire(msg);

        //System.out.println("Sending response (" + bytesToSend.length + " bytes):");
        //System.out.println(msg.getFilename());
        //socket.send(bytesToSend);
      } catch (IOException ioe) {
        System.err.println("Parse error in message: " + ioe.getMessage());
      }
    }
	}
}
