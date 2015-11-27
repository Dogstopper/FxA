// java FxAServer 8081 localhost 5000
import java.io.*;
import java.net.InetAddress;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class FxAServer {

  private ExecutorService service;
  private RxPSocket socket;

  public FxAServer(int port, int netEmuPort, InetAddress netEmuInetAddress) throws IOException {
    // Creates Client's RxPSocket bound to Client's localhost and even port
    InetAddress localhost = InetAddress.getByName("127.0.0.1");
    socket = new RxPSocket(port, localhost);
  }

  public void mainLoop() throws InterruptedException {
    service = Executors.newFixedThreadPool(3);

    ArrayList<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
    tasks.add(new ServerLoop(socket));
    tasks.add(new CommandLoop(this));

    service.invokeAll(tasks);

    service.shutdown();
    service.awaitTermination(1, TimeUnit.DAYS);
  }

  public void setWindowSize(int windowSize) {
    System.out.println("SETTING WINDOW");
    socket.setWindowSize(windowSize);
  }

  public void terminate() {
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

    System.out.println("Entering mainLoop()");
    (new FxAServer(inPort, inNetEmuPort, inNetEmuInetAddress)).mainLoop();
  }
}

class CommandLoop implements Callable<Object> {

  private FxAServer server;
  private Scanner scanner;

  public CommandLoop(FxAServer server) {
    this.server = server;
    this.scanner = new Scanner(System.in);
  }

  @Override
  public Object call() throws Exception {
      commandLoop();
      return null;
  }

  public void commandLoop() {
    while(true) {
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
}

class ServerLoop implements Callable<Object> {

  private RxPSocket socket;
  private boolean connected;

  public ServerLoop(RxPSocket socket) {
    this.socket = socket;
    this.connected = false;
  }

  @Override
  public Object call() throws Exception {
    serverLoop();
    return null;
  }

  private byte[] receive() {
    while(true) {
      try {
        return socket.receive();
      } catch(Exception e) {
        continue;
      }
    }
  }

  private boolean send(byte[] toSend) {
    return socket.send(toSend);
  }

  private void listen() {
    socket.listen();
  }

  private void handleGet(FileMsg msg, MsgCoder coder) throws IOException {
    FileInputStream newFile = null;
    File openFile = null;
    String filename = msg.getFilename();
    try {
      File f = new File(filename.trim());
      if (!f.exists()) {
        System.err.println("File Not Found");
        // Send an error back
        FileMsg error = new FileMsg(true, filename, null);
        byte[] encodeMsg = new String("FileMsg g " + filename).getBytes();
        System.out.println("File Encoded. Num Bytes="+encodeMsg.length);
        return;
      }

      Path path = Paths.get(f.getAbsolutePath());
      System.out.println("Handling GET: " + path);
      byte[] data = Files.readAllBytes(path);

      System.out.println("File Read. Num Bytes="+data.length);

      // Send it.
      FileMsg result = new FileMsg(false, filename, data);
      byte[] encodeMsg = coder.toWire(result);
      System.out.println("File Encoded. Num Bytes="+encodeMsg.length);
      while (send(encodeMsg) == false);

    } catch(Exception fnfe) {
      System.err.println("The file to download could not be found: " + openFile.getAbsolutePath());
      System.err.println(fnfe.getMessage());

      // Send an error back
      FileMsg error = new FileMsg(true, filename, null);
      byte[] encodeMsg = new String("FileMsg g " + filename).getBytes();
      System.out.println("File Encoded. Num Bytes="+encodeMsg.length);

      while(!send(encodeMsg));
    }
    finally {
      if (newFile != null) {
        newFile.close();
      }
    }
  }

  public void handlePost(FileMsg msg, MsgCoder coder) throws IOException {
    System.out.println("Handling POST");
    String filename = msg.getFilename();
    FileOutputStream fs = null;
    try {
      fs = new FileOutputStream(filename);
      fs.write(msg.getFile());

      byte[] encodedMsg = coder.toWire(msg);
      System.out.println("\n\nSending response back");
      System.out.println(new String(encodedMsg));
      while (send(encodedMsg) == false);
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

  public void serverLoop(){

    if (!connected) {
      listen();
      System.out.println("Listening");
      connected = true;
    }
    System.out.print("> ");

    MsgCoder coder = new FileMsgTextCoder();
    FileService service = new FileService();

    
    while (true) {
      try {
        byte[] inBuffer = null;
        while ((inBuffer = receive()).length == 0);

        FileMsg msg = coder.fromWire(inBuffer);
        msg = service.handleRequest(msg);

        // GET requests a file, and we send a packet back with the result.
        if (msg.isGet()) {
          handleGet(msg, coder);
        }
        else {
          handlePost(msg, coder);
        }
      }
      catch (IOException ioe) {
        System.err.println(ioe.getMessage());
      }
      System.out.print("> ");
    }
  }
}
