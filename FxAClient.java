// java FxAClient 8080 localhost 5000
import java.io.*;
import java.net.InetAddress;
import java.util.Arrays;

public class FxAClient {

	public static void main(String args[]) throws IOException {

		int port, netEmuPort;

		String netEmuIPString;

		InetAddress localhost = InetAddress.getByName("127.0.0.1"),
					netEmuInetAddress;

		// Test for correct # of args
	    if (args.length != 3) { 
	      throw new IllegalArgumentException("Parameter(s): <port-evenNum>" + " <NetEmu-IP> <NetEmu-Port#>");
	    }

	    // Initialize Port and NetEmu Address/Port with command line arguments
	    port = Integer.parseInt(args[0]);
	    netEmuIPString = args[1];
	    netEmuPort = Integer.parseInt(args[2]);

	    // Create NetEmu InetAddress Obj
	    netEmuInetAddress = InetAddress.getByName(netEmuIPString);

	    // Creates Client's RxPSocket bound to Client's localhost and even port
	    RxPSocket socket = new RxPSocket(port, localhost);

		// connect - The FxA-client connects to the NetEmu which then connects to the FxA-server (running at the same IP host).
	    socket.connect(netEmuInetAddress, netEmuPort);

		/*
		 * get F
		 * The FxA-client downloads file F from the server (if F exists in the same directory with the FxA-server program).
	     */

	    boolean isGet = true;
	    String filename = "helloworld.txt";
		FileMsg fileMsg = new FileMsg(true, filename);

		// Change Text to Bin
    	MsgCoder coder = new FileMsgTextCoder();
    	byte[] encodedMsg = coder.toWire(fileMsg);
    	System.out.println("Sending Text-Encoded Request (" + encodedMsg.length + " bytes): ");
        System.out.println(fileMsg);

        // Send bytes to RxPSocket
        socket.send(encodedMsg);

        // Receive Response
        byte[] inBuffer = socket.receive();

        // TODO: Try to handle this from buffer/socket, should not have to create packet
        RxPPacket packet = new RxPPacket(inBuffer);
        encodedMsg = Arrays.copyOfRange(packet.getPayload(), 0, packet.getLength());

        System.out.println("Received Text-Encoded Response (" + encodedMsg.length + " bytes): ");
    	fileMsg = coder.fromWire(encodedMsg);
    	System.out.println(fileMsg);

		/*
		 * post F
		 * The FxA-client uploads file F to the server (if F exists in the same directory with the FxA-client program). This feature will be treated as extra credit for up to 20 project points.
		 */

		// window W (only for projects that support configurable flow window) W: the maximum receiver’s window-size at the FxA-Client (in segments).

		// disconnect - The FxA-client terminates gracefully from the FxA-server.
	}
}
