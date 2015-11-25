import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class FileMsgTextCoder implements MsgCoder {
	/*
	 * Wire Format "FileMsg" <"g" | "p"> <FILENAME>
	 * Charset is fixed by the wire format.
	 */
	public static final String CHARSETNAME = "US-ASCII";
  	public static final String DELIMSTR = " ";
  	public static final int MAX_WIRE_LENGTH = 2000;

	public static final String PROTOCOL = "FileMsg";
	public static final String GET = "g";
	public static final String POST = "p";

	public byte[] toWire(FileMsg msg) throws IOException {
		String msgString = PROTOCOL + DELIMSTR
			+ (msg.isGet()? GET : POST) + DELIMSTR
			+ msg.getFilename() + (!msg.isGet()? (DELIMSTR + new String(msg.getFile())) : "");

		byte data[] = msgString.getBytes(CHARSETNAME);
		return data;
	}

	public FileMsg fromWire(byte[] message) throws IOException {
    
		ByteArrayInputStream msgStream = new ByteArrayInputStream(message);
		Scanner s = new Scanner(new InputStreamReader(msgStream, CHARSETNAME));

		boolean isGet;
		String filename;
    byte[] file = null;

		String token;


		try {
      // Determine that our protocol is being used
			token = s.next();
			if (!token.equals(PROTOCOL)) {
				throw new IOException("Bad protocol string: " + token);
			}

      // Decide whether GET or POST
			token = s.next();
			if (token.equals(GET)) {
				isGet = true;
			} else if (token.equals(POST)) {
				isGet = false;
			} else {
				throw new IOException("Bad GET/POST indicator: " + token);
			}

      // Get the filename
      token = s.next();
      filename = token;

      // If the request is not a GET request, then add the file.
      if (!isGet) {
        StringBuffer buffer = new StringBuffer();
        while(s.hasNextLine()) {
          token = s.nextLine();
    			buffer.append(token + "\n");
        }
        file = buffer.toString().getBytes(CHARSETNAME);
      }

		} catch (IOException ioe) {
			throw new IOException("Parse error...");
		}
		return new FileMsg(isGet, filename, file);
	}
}
