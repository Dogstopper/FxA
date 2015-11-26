// java FxAServer 8081
import java.io.*;
import java.net.InetAddress;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.*;

public class FxAServer {

  private int port;
  private int netEmuPort;
  private InetAddress netEmuInetAddress;
  private RxPSocket socket;
  private MsgCoder coder;

  public FxAServer(int port, int netEmuPort, InetAddress netEmuInetAddress) throws IOException {
    this.port = port;
    this.netEmuPort = netEmuPort;
    this.netEmuInetAddress = netEmuInetAddress;
    this.coder = new FileMsgTextCoder();

    // Creates Client's RxPSocket bound to Client's localhost and even port
    InetAddress localhost = InetAddress.getByName("127.0.0.1");
    socket = new RxPSocket(port, localhost);
    listen();
  }

  public void mainLoop() throws InterruptedException {
    //Thread commandLoopThread = new Thread(new CommandLoop(this));
    Thread serverLoop = new Thread(new ServerLoop(this));
    //commandLoopThread.start();
    serverLoop.start();
    while (true) {
      //commandLoopThread.join(1000);
      serverLoop.join(1000);
    }
  }

  public synchronized void listen() {
    socket.listen();
  }

  public synchronized byte[] receive() {
    while(true) {
      try {
        return socket.receive();
      } catch(Exception e) {
        continue;
      }
    }
  }

  public synchronized void connect() {
    socket.connect(netEmuInetAddress, netEmuPort);
  }

  public synchronized boolean send(byte[] toSend) {
    return socket.send(toSend);
  }

  public synchronized void setWindowSize(int windowSize) {
    System.out.println("SETTING WINDOW");
    socket.setWindowSize(windowSize);
  }

  public synchronized void terminate() {
    System.out.println("TERMINATE");
    // TODO: Disconnect server.
  }

	public static void main(String[] args) throws IOException, InterruptedException {

		if (args.length != 3) { // Test for correct # of args
			throw new IllegalArgumentException("Parameter(s): <port-oddNum>  <NetEmu-IP> <NetEmu-Port#>");
		}

    int inPort = Integer.parseInt(args[0]);
    if (inPort % 2 != 1) {
      throw new IllegalArgumentException("Port number of localhost must be odd");
    }
    String inNetEmuIPString = args[1];
    int inNetEmuPort = Integer.parseInt(args[2]);

    // Create NetEmu InetAddress Obj
    InetAddress inNetEmuInetAddress = InetAddress.getByName(inNetEmuIPString);

    (new FxAServer(inPort, inNetEmuPort, inNetEmuInetAddress)).mainLoop();
  }
}

class CommandLoop
      implements Runnable {

  private FxAServer server;
  private Scanner scanner;

  public CommandLoop(FxAServer server) {
    this.server = server;
    this.scanner = new Scanner(System.in);
  }

  public void run() {
    System.out.print("> ");
    if (scanner.hasNextLine()) {
      String command = scanner.next();
      if (command.equals("window")) {
        int windowSize = scanner.nextInt();
        scanner.nextLine();
        server.setWindowSize(windowSize);
      }
      else if (command.equals("terminate")) {
        server.terminate();
      }
      else {
        System.out.println("Not a valid command.");
      }
    }
  }
}

class ServerLoop
      implements Runnable {

  private FxAServer server;

  public ServerLoop(FxAServer server) {
    this.server = server;
  }

  public void run(){

    MsgCoder coder = new FileMsgTextCoder();
    FileService service = new FileService();

    try {
      byte[] inBuffer = null;
      while ((inBuffer = server.receive()).length == 0);

      FileMsg msg = coder.fromWire(inBuffer);
      msg = service.handleRequest(msg);
      String filename = msg.getFilename();

      // GET requests a file, and we send a packet back with the result.
      if (msg.isGet()) {
        FileInputStream newFile = null;
        File openFile = null;
        try {
          openFile = new File(filename);
          newFile = new FileInputStream(openFile);

          // Read in the file
          byte[] file = new byte[newFile.available()];
          newFile.read(file);

          System.out.println("File Read. Num Bytes="+file.length);

          // Send it.
          FileMsg result = new FileMsg(false, filename, file);
          byte[] encodeMsg = coder.toWire(result);
          System.out.println("File Encoded. Num Bytes="+encodeMsg.length);
          while (server.send(encodeMsg) == false);

        } catch(FileNotFoundException fnfe) {
          System.err.println("The file to download could not be found: " + openFile.getAbsolutePath());
          System.err.println(fnfe.getMessage());

          // Send an error back
          FileMsg error = new FileMsg(true, filename, null);
          byte[] encodeMsg = coder.toWire(error);
          System.out.println("File Encoded. Num Bytes="+encodeMsg.length);

          while(!server.send(encodeMsg));

        }
      }
      else {
        try {
          File newFile = new File(filename);
          FileOutputStream fs = new FileOutputStream(newFile);
          fs.write(msg.getFile());

          byte[] encodedMsg = coder.toWire(msg);
          System.out.println("\n\nSending response back");
          System.out.println(new String(encodedMsg));
          server.send(encodedMsg);
        } catch(Exception e) {
          System.err.println("The file could not be saved.");
          System.err.println(e.getMessage());
        }
        System.out.println("File was downloaded successfully.\nSaved as: " + filename);
      }
    }
    catch (IOException ioe) {
      System.err.println(ioe.getMessage());
    }
  }
}
  //   // byte[] inBuffer = new byte[FileMsgTextCoder.MAX_WIRE_LENGTH];
  // MsgCoder coder = new FileMsgTextCoder();
  // FileService service = new FileService();
  //
  //   socket.listen();
  //
  //   while (true) {
  //
  //     // Receive Buffer
  //     byte[] inBuffer = socket.receive();
  //     //System.out.println("Application Layer: " + javax.xml.bind.DatatypeConverter.printHexBinary(inBuffer));
  //     // System.out.println(new String(inBuffer));
  //     try {
  //       // System.out.println("FROM WIRE: " + new String(inBuffer));
  //       FileMsg msg = coder.fromWire(inBuffer);
  //       msg = service.handleRequest(msg);
  //
  //       // Send response (byte[]) from handledRequest
  //       byte[] bytesToSend = coder.toWire(msg);
  //
  //       System.out.println("\n\n\nSending response (" + bytesToSend.length + " bytes):");
  //       // System.out.println(new String(bytesToSend));
  //       socket.send(bytesToSend);
  //     } catch (IOException ioe) {
  //       System.err.println("Parse error in message: " + ioe.getMessage());
  //     }
  //   }
	// }
