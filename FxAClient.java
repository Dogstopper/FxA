// java FxAClient 8080 localhost 5000
import java.io.*;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;

public class FxAClient {

  // Connection data
  private int port;
  private int netEmuPort;
  private InetAddress netEmuInetAddress;
  private RxPSocket socket;
  private MsgCoder coder;

  public FxAClient(int port, int netEmuPort, InetAddress netEmuInetAddress) throws IOException {
    this.port = port;
    this.netEmuPort = netEmuPort;
    this.netEmuInetAddress = netEmuInetAddress;
    this.coder = new FileMsgTextCoder();

    // Creates Client's RxPSocket bound to Client's localhost and even port
    InetAddress localhost = InetAddress.getByName("127.0.0.1");
    socket = new RxPSocket(port, localhost);
  }

  private void mainLoop() throws IOException {
    Scanner scanner = new Scanner(System.in);
    while(true) {
      System.out.print("> ");
      if (scanner.hasNextLine()) {
        String command = scanner.next();
        if (command.equals("connect")) {
          connect();
        }
        else if (command.equals("get")) {
          String filename = scanner.next();
          scanner.nextLine();
          getFile(filename);
        }
        else if (command.equals("post")) {
          String filename = scanner.next();
          scanner.nextLine();
          postFile(filename);
        }
        else if (command.equals("window")) {
          int windowSize = scanner.nextInt();
          scanner.nextLine();
          setWindowSize(windowSize);
        }
        else if (command.equals("disconnect")) {
          disconnect();
        }
        else {
          System.out.println("Not a valid command.");
        }
      }
    }
  }

  private void connect() {
	  socket.connect(netEmuInetAddress, netEmuPort);
  }

  private void listen() {
    socket.listen();
  }

  private void disconnect() {
    // TODO: Implement
  }

  private void setWindowSize(int windowSize) {
    socket.setWindowSize(windowSize);
  }

  private void getFile(String filename) throws IOException {
    // Send the request to the server
    FileMsg request = new FileMsg(true, filename, null);
    byte[] encodedMsg = coder.toWire(request);
    while (!socket.send(encodedMsg)) {
      System.err.println("Attempting to connect again.");
      connect();
    }

    // Wait for the server to respond to the request with a byte[]
    byte[] fileBytes = null;
    while ((fileBytes = socket.receive()) == null) {
      System.err.println("Attempting to connect again.");
      connect();
    }
    FileMsg response = coder.fromWire(fileBytes);

    if (response.isGet()) {
      System.err.println("The filename was wrong.");
      return;
    }

    try {
      File newFile = new File(response.getFilename());
      FileOutputStream fs = new FileOutputStream(newFile);
      fs.write(response.getFile());
    } catch(Exception e) {
      System.err.println("The file could not be saved.");
      System.err.println(e.getMessage());
    }
    System.out.println("File was downloaded successfully.\nSaved as: " + filename);
  }

  private void postFile(String filename) throws IOException {
    FileInputStream newFile = null;
    try {
      newFile = new FileInputStream(filename);
    } catch(FileNotFoundException fnfe) {
      System.err.println("The file to upload could not be found.");
      System.err.println(fnfe.getMessage());
      return;
    }

    // Read in the file
    byte[] file = new byte[newFile.available()];
    newFile.read(file);

    System.out.println("File Read. Num Bytes="+file.length);

    // Send it.
    FileMsg request = new FileMsg(false, filename, file);
    byte[] encodeMsg = coder.toWire(request);
    System.out.println("File Encoded. Num Bytes="+encodeMsg.length);
    System.out.println("File Encoded. "+new String(encodeMsg));
    socket.send(encodeMsg);

    socket.listen();
    System.out.println("\n\nWaiting for response.");
    // Wait for the response
    byte[] fileBytes = socket.receive();
    FileMsg response = coder.fromWire(fileBytes);

    if (new String(response.getFile()).equals(new String(file))) {
      System.out.println("File Upload success");
    }
    else {
      System.out.println("File upload failed");
    }
  }

	public static void main(String args[]) throws Exception {
    int inPort;
    int inNetEmuPort;
		String inNetEmuIPString;

		// Test for correct # of args
    if (args.length != 3) {
      throw new IllegalArgumentException("Parameter(s): <port-evenNum>  <NetEmu-IP> <NetEmu-Port#>");
    }

    // Initialize Port and NetEmu Address/Port with command line arguments
    inPort = Integer.parseInt(args[0]);
    if (inPort % 2 != 0) {
      throw new IllegalArgumentException("Port number of localhost must be even");
    }
    inNetEmuIPString = args[1];
    inNetEmuPort = Integer.parseInt(args[2]);

    // Create NetEmu InetAddress Obj
    InetAddress inNetEmuInetAddress = InetAddress.getByName(inNetEmuIPString);

    (new FxAClient(inPort, inNetEmuPort, inNetEmuInetAddress)).mainLoop();
  }
		// /*
		//  * get F
		//  * The FxA-client downloads file F from the server (if F exists in the same directory with the FxA-server program).
	  //    */
    //
	  //   boolean isGet = true;
	  //   //String filename = "helloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txthelloworld.txt";
		//     FileMsg fileMsg = new FileMsg(true, bookText);
    //
		// // Change Text to Bin
    // 	MsgCoder coder = new FileMsgTextCoder();
    // 	byte[] encodedMsg = coder.toWire(fileMsg);
    // 	System.out.println("Sending Text-Encoded Request (" + encodedMsg.length + " bytes): ");
    //     System.out.println("Encoded Payload: " + javax.xml.bind.DatatypeConverter.printHexBinary(encodedMsg));
    //
    //     // Send bytes to RxPSocket
    //     socket.send(encodedMsg);
    //
    //     System.out.println("CLIENT SENT ALL DATA\n\n\n");
        // Receive Response
      //   byte[] inBuffer = socket.receive();
      //
      //   // TODO: Try to handle this from buffer/socket, should not have to create packet
      //   RxPPacket packet = new RxPPacket(inBuffer);
      //   encodedMsg = Arrays.copyOfRange(packet.getPayload(), 0, packet.getLength());
      //
      //   System.out.println("Received Text-Encoded Response (" + encodedMsg.length + " bytes): ");
    	// fileMsg = coder.fromWire(encodedMsg);
    	// System.out.println(fileMsg);

		/*
		 * post F
		 * The FxA-client uploads file F to the server (if F exists in the same directory with the FxA-client program). This feature will be treated as extra credit for up to 20 project points.
		 */

		// window W (only for projects that support configurable flow window) W: the maximum receiver’s window-size at the FxA-Client (in segments).

		// disconnect - The FxA-client terminates gracefully from the FxA-server.
	// }

  static String bookText = "GREAT EXPECTATIONS\n" +
" \n" +
" [1867 Edition]\n" +
" \n" +
" by Charles Dickens\n" +
" \n" +
" \n" +
" [Project Gutenberg Editor's Note: There is also another version of\n" +
" this work etext98/grexp10.txt scanned from a different edition]\n" +
" \n" +
" \n" +
" \n" +
" \n" +
" Chapter I\n" +
" \n" +
" My father's family name being Pirrip, and my Christian name Philip, my\n" +
" infant tongue could make of both names nothing longer or more explicit\n" +
" than Pip. So, I called myself Pip, and came to be called Pip.\n" +
" \n" +
" I give Pirrip as my father's family name, on the authority of his\n" +
" tombstone and my sister,--Mrs. Joe Gargery, who married the blacksmith.\n" +
" As I never saw my father or my mother, and never saw any likeness\n" +
" of either of them (for their days were long before the days of\n" +
" photographs), my first fancies regarding what they were like were\n" +
" unreasonably derived from their tombstones. The shape of the letters on\n" +
" my father's, gave me an odd idea that he was a square, stout, dark man,\n" +
" with curly black hair. From the character and turn of the inscription,\n" +
" \"Also Georgiana Wife of the Above,\" I drew a childish conclusion that\n" +
" my mother was freckled and sickly. To five little stone lozenges, each\n" +
" about a foot and a half long, which were arranged in a neat row beside\n" +
" their grave, and were sacred to the memory of five little brothers of\n" +
" mine,--who gave up trying to get a living, exceedingly early in\n" +
" that universal struggle,--I am indebted for a belief I religiously\n" +
" entertained that they had all been born on their backs with their hands\n" +
" in their trousers-pockets, and had never taken them out in this state of\n" +
" existence.\n" +
" \n" +
" Ours was the marsh country, down by the river, within, as the river\n" +
" wound, twenty miles of the sea. My first most vivid and broad impression\n" +
" of the identity of things seems to me to have been gained on a memorable\n" +
" raw afternoon towards evening. At such a time I found out for certain\n" +
" that this bleak place overgrown with nettles was the churchyard; and\n" +
" that Philip Pirrip, late of this parish, and also Georgiana wife of the\n" +
" above, were dead and buried; and that Alexander, Bartholomew, Abraham,\n" +
" Tobias, and Roger, infant children of the aforesaid, were also dead\n" +
" and buried; and that the dark flat wilderness beyond the churchyard,\n" +
" intersected with dikes and mounds and gates, with scattered cattle\n" +
" feeding on it, was the marshes; and that the low leaden line beyond\n" +
" was the river; and that the distant savage lair from which the wind was\n" +
" rushing was the sea; and that the small bundle of shivers growing afraid\n" +
" of it all and beginning to cry, was Pip.\n" +
" \n" +
" \"Hold your noise!\" cried a terrible voice, as a man started up from\n" +
" among the graves at the side of the church porch. \"Keep still, you\n" +
" little devil, or I'll cut your throat!\"\n" +
" \n" +
" A fearful man, all in coarse gray, with a great iron on his leg. A man\n" +
" with no hat, and with broken shoes, and with an old rag tied round his\n" +
" head. A man who had been soaked in water, and smothered in mud, and\n" +
" lamed by stones, and cut by flints, and stung by nettles, and torn by\n" +
" briars; who limped, and shivered, and glared, and growled; and whose\n" +
" teeth chattered in his head as he seized me by the chin.\n" +
" \n" +
" \"Oh! Don't cut my throat, sir,\" I pleaded in terror. \"Pray don't do it,\n" +
" sir.\"\n" +
" \n" +
" \"Tell us your name!\" said the man. \"Quick!\"\n" +
" \n" +
" \"Pip, sir.\"\n" +
" \n" +
" \"Once more,\" said the man, staring at me. \"Give it mouth!\"\n" +
" \n" +
" \"Pip. Pip, sir.\"\n" +
" \n" +
" \"Show us where you live,\" said the man. \"Pint out the place!\"\n" +
" \n" +
" I pointed to where our village lay, on the flat in-shore among the\n" +
" alder-trees and pollards, a mile or more from the church.\n" +
" \n" +
" The man, after looking at me for a moment, turned me upside down, and\n" +
" emptied my pockets. There was nothing in them but a piece of bread. When\n" +
" the church came to itself,--for he was so sudden and strong that he\n" +
" made it go head over heels before me, and I saw the steeple under my\n" +
" feet,--when the church came to itself, I say, I was seated on a high\n" +
" tombstone, trembling while he ate the bread ravenously.\n" +
" \n" +
" \"You young dog,\" said the man, licking his lips, \"what fat cheeks you\n" +
" ha' got.\"\n" +
" \n" +
" I believe they were fat, though I was at that time undersized for my\n" +
" years, and not strong.\n" +
" \n" +
" \"Darn me if I couldn't eat em,\" said the man, with a threatening shake\n" +
" of his head, \"and if I han't half a mind to't!\"\n" +
" \n" +
" I earnestly expressed my hope that he wouldn't, and held tighter to\n" +
" the tombstone on which he had put me; partly, to keep myself upon it;\n" +
" partly, to keep myself from crying.\n" +
" \n" +
" \"Now lookee here!\" said the man. \"Where's your mother?\"\n" +
" \n" +
" \"There, sir!\" said I.\n" +
" \n" +
" He started, made a short run, and stopped and looked over his shoulder.\n" +
" \n" +
" \"There, sir!\" I timidly explained. \"Also Georgiana. That's my mother.\"\n" +
" \n" +
" \"Oh!\" said he, coming back. \"And is that your father alonger your\n" +
" mother?\"\n" +
" \n" +
" \"Yes, sir,\" said I; \"him too; late of this parish.\"\n" +
" \n" +
" \"Ha!\" he muttered then, considering. \"Who d'ye live with,--supposin'\n" +
" you're kindly let to live, which I han't made up my mind about?\"\n" +
" \n" +
" \"My sister, sir,--Mrs. Joe Gargery,--wife of Joe Gargery, the\n" +
" blacksmith, sir.\"\n" +
" \n" +
" \"Blacksmith, eh?\" said he. And looked down at his leg.\n" +
" \n" +
" After darkly looking at his leg and me several times, he came closer\n" +
" to my tombstone, took me by both arms, and tilted me back as far as he\n" +
" could hold me; so that his eyes looked most powerfully down into mine,\n" +
" and mine looked most helplessly up into his.\n" +
" \n" +
" \"Now lookee here,\" he said, \"the question being whether you're to be let\n" +
" to live. You know what a file is?\"\n" +
" \n" +
" \"Yes, sir.\"\n" +
" \n" +
" \"And you know what wittles is?\"\n" +
" \n" +
" \"Yes, sir.\"\n" +
" \n" +
" After each question he tilted me over a little more, so as to give me a\n" +
" greater sense of helplessness and danger.\n" +
" \n" +
" \"You get me a file.\" He tilted me again. \"And you get me wittles.\" He\n" +
" tilted me again. \"You bring 'em both to me.\" He tilted me again. \"Or\n" +
" I'll have your heart and liver out.\" He tilted me again.\n" +
" \n" +
" I was dreadfully frightened, and so giddy that I clung to him with both\n" +
" hands, and said, \"If you would kindly please to let me keep upright,\n" +
" sir, perhaps I shouldn't be sick, and perhaps I could attend more.\"\n" +
" \n" +
" He gave me a most tremendous dip and roll, so that the church jumped\n" +
" over its own weathercock. Then, he held me by the arms, in an upright\n" +
" position on the top of the stone, and went on in these fearful terms:--\n" +
" \n" +
" \"You bring me, to-morrow morning early, that file and them wittles. You\n" +
" bring the lot to me, at that old Battery over yonder. You do it, and you\n" +
" never dare to say a word or dare to make a sign concerning your having\n" +
" seen such a person as me, or any person sumever, and you shall be let to\n" +
" live. You fail, or you go from my words in any partickler, no matter how\n" +
" small it is, and your heart and your liver shall be tore out, roasted,\n" +
" and ate. Now, I ain't alone, as you may think I am. There's a young man\n" +
" hid with me, in comparison with which young man I am a Angel. That young\n" +
" man hears the words I speak. That young man has a secret way pecooliar\n" +
" to himself, of getting at a boy, and at his heart, and at his liver. It\n" +
" is in wain for a boy to attempt to hide himself from that young man. A\n" +
" boy may lock his door, may be warm in bed, may tuck himself up, may draw\n" +
" the clothes over his head, may think himself comfortable and safe, but\n" +
" that young man will softly creep and creep his way to him and tear him\n" +
" open. I am a keeping that young man from harming of you at the present\n" +
" moment, with great difficulty. I find it wery hard to hold that young\n" +
" man off of your inside. Now, what do you say?\"\n" +
" \n" +
" I said that I would get him the file, and I would get him what broken\n" +
" bits of food I could, and I would come to him at the Battery, early in\n" +
" the morning.\n" +
" \n" +
" \"Say Lord strike you dead if you don't!\" said the man.\n" +
" \n" +
" I said so, and he took me down.\n" +
" \n" +
" \"Now,\" he pursued, \"you remember what you've undertook, and you remember\n" +
" that young man, and you get home!\"\n" +
" \n" +
" \"Goo-good night, sir,\" I faltered.\n" +
" \n" +
" \"Much of that!\" said he, glancing about him over the cold wet flat. \"I\n" +
" wish I was a frog. Or a eel!\"\n" +
" \n" +
" At the same time, he hugged his shuddering body in both his\n" +
" arms,--clasping himself, as if to hold himself together,--and limped\n" +
" towards the low church wall. As I saw him go, picking his way among the\n" +
" nettles, and among the brambles that bound the green mounds, he looked\n" +
" in my young eyes as if he were eluding the hands of the dead people,\n" +
" stretching up cautiously out of their graves, to get a twist upon his\n" +
" ankle and pull him in.\n" +
" \n" +
" When he came to the low church wall, he got over it, like a man whose\n" +
" legs were numbed and stiff, and then turned round to look for me. When I\n" +
" saw him turning, I set my face towards home, and made the best use of\n" +
" my legs. But presently I looked over my shoulder, and saw him going on\n" +
" again towards the river, still hugging himself in both arms, and picking\n" +
" his way with his sore feet among the great stones dropped into the\n" +
" marshes here and there, for stepping-places when the rains were heavy or\n" +
" the tide was in.\n" +
" \n" +
" The marshes were just a long black horizontal line then, as I stopped\n" +
" to look after him; and the river was just another horizontal line, not\n" +
" nearly so broad nor yet so black; and the sky was just a row of long\n" +
" angry red lines and dense black lines intermixed. On the edge of the\n" +
" river I could faintly make out the only two black things in all the\n" +
" prospect that seemed to be standing upright; one of these was the beacon\n" +
" by which the sailors steered,--like an unhooped cask upon a pole,--an\n" +
" ugly thing when you were near it; the other, a gibbet, with some chains\n" +
" hanging to it which had once held a pirate. The man was limping on\n" +
" towards this latter, as if he were the pirate come to life, and come\n" +
" down, and going back to hook himself up again. It gave me a terrible\n" +
" turn when I thought so; and as I saw the cattle lifting their heads to\n" +
" gaze after him, I wondered whether they thought so too. I looked all\n" +
" round for the horrible young man, and could see no signs of him. But now\n" +
" I was frightened again, and ran home without stopping.";
}
