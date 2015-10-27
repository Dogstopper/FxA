import java.io.*;
import java.net.InetAddress;
import java.util.Arrays;

public class FxAClient {

	public static void main(String args[]) throws IOException {

		String addr = "127.0.0.1";
		InetAddress inetAddress = InetAddress.getByName(addr);
		int port = 5000;

	    if (args.length != 3) { // Test for correct # of args
	      throw new IllegalArgumentException("Parameter(s): <port-evenNum>" +
	                                          " <NetEmu-IP> <NetEmu-Port#>");
	    }

	    RxPSocket socket = new RxPSocket();

		// connect - The FxA-client connects to the FxA-server (running at the same IP host).
	    socket.connect(inetAddress, 5000);

		/*
		 * get F
		 * The FxA-client downloads file F from the server (if F exists in the same directory with the FxA-server program).
	     */

	    boolean isGet = true;
	    String filename = "helloworld.txt";
		FileMsg fileMsg = new FileMsg(true, filename);

		// Change Text to Bin
    	MsgCoder coder = new FileMsgTextCoder();
    	byte[] encodedFile = coder.toWire(fileMsg);
    	System.out.println("Sending Text-Encoded Request (" + encodedFile.length
        + " bytes): ");
        System.out.println(fileMsg);

        RxPPacket message = new RxPPacket(encodedFile, encodedFile.length);
        socket.send(message);

        // Receive Response
        message = new RxPPacket(new byte[FileMsgTextCoder.MAX_WIRE_LENGTH],
        	FileMsgTextCoder.MAX_WIRE_LENGTH);
        socket.receive(message);
        encodedFile = Arrays.copyOfRange(message.getData(), 0, message.getLength());

        System.out.println("Received Text-Encoded Response (" + encodedFile.length
        	+ " bytes): ");
    	fileMsg = coder.fromWire(encodedFile);
    	System.out.println(fileMsg);

		/*
		 * post F
		 * The FxA-client uploads file F to the server (if F exists in the same directory with the FxA-client program). This feature will be treated as extra credit for up to 20 project points.
		 */

		// window W (only for projects that support configurable flow window) W: the maximum receiver’s window-size at the FxA-Client (in segments).

		// disconnect - The FxA-client terminates gracefully from the FxA-server.
	}
}
