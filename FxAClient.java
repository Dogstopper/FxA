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
    boolean connected = false;
    while(true) {
      System.out.print("> ");
      if (scanner.hasNextLine()) {
        String command = scanner.next();
        if (command.equals("connect")) {
          connect();
          connected = true;
        }
        else if (command.equals("get") && connected == true) {
          String filename = scanner.next();
          scanner.nextLine();
          getFile(filename);
        }
        else if (command.equals("post") && connected == true) {
          String filename = scanner.next();
          scanner.nextLine();
          postFile(filename);
        }
        else if (command.equals("window") && connected == true) {
          int windowSize = scanner.nextInt();
          scanner.nextLine();
          setWindowSize(windowSize);
        }
        else if (command.equals("disconnect")) {
          disconnect();
          connected = false;
        }
        else {
          if (!connected) {
            System.out.println("You need to connect to the server");
          } else {
            System.out.println("Not a valid command.");
          }
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
    socket.close();
  }

  private void setWindowSize(int windowSize) {
    socket.setWindowSize(windowSize);
  }

  private void getFile(String filename) throws IOException {
    // Send the request to the server
    FileMsg request = new FileMsg(true, filename, null);
    byte[] encodedMsg = coder.toWire(request);
    while (!socket.send(encodedMsg));

    System.out.println("Query Sent. Waiting for response.");
    // Wait for the server to respond to the request with a byte[]
    byte[] fileBytes = null;
    while ((fileBytes = socket.receive()).length == 0);

    FileMsg response = coder.fromWire(fileBytes);

    if (response.isGet()) {
      System.err.println("The filename was wrong.");
      return;
    }

    // Output the received bytes to a file.
    FileOutputStream fs = null;
    try {
      File newFile = new File(filename);
      fs = new FileOutputStream(newFile);
      fs.write(response.getFile());
    } catch(Exception e) {
      System.err.println("The file could not be saved.");
      System.err.println(e.getMessage());
    }
    finally {
      if (fs != null) {
        fs.close();
      }
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
    newFile.close();

    System.out.println("File Read. Num Bytes="+file.length);

    // Send it.
    FileMsg request = new FileMsg(false, filename, file);
    byte[] encodeMsg = coder.toWire(request);
    System.out.println("File Encoded. Num Bytes="+encodeMsg.length);
    System.out.println("File Encoded. "+new String(encodeMsg));
    while (!socket.send(encodeMsg));

    System.out.println("\n\nFile Uploaded. Verifying success");
    // Wait for the response
    byte[] fileBytes = null;
    while ((fileBytes = socket.receive()).length == 0); // Loop until data comes back
    FileMsg response = coder.fromWire(fileBytes);

    if (new String(response.getFile()).trim().equals(new String(file).trim())) {
      System.out.println("File Upload success");
    }
    else {
      System.out.println("File upload failed");
      try {
        File errorFile = new File("upload_failure.txt");
        FileOutputStream fs = new FileOutputStream(errorFile);
        fs.write(response.getFile());
      } catch(Exception e) {
        System.err.println("The file could not be saved.");
        System.err.println(e.getMessage());
      }
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
}
