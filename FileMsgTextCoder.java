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
			+ msg.getFilename();

		byte data[] = msgString.getBytes(CHARSETNAME);
		return data;
	}

	public FileMsg fromWire(byte[] message) throws IOException {
		System.out.println("fromWire: " + javax.xml.bind.DatatypeConverter.printHexBinary(message));
		
		ByteArrayInputStream msgStream = new ByteArrayInputStream(message);
		Scanner s = new Scanner(new InputStreamReader(msgStream, CHARSETNAME));

		boolean isGet;
		String filename;

		String token;

		try {
			token = s.next();

			if (!token.equals(PROTOCOL)) {
				throw new IOException("Bad protocol string: " + token);
			}
			token = s.next();
			if (token.equals(GET)) {
				isGet = true;
			} else if (token.equals(POST)) {
				isGet = false;
			} else {
				throw new IOException("Bad GET/POST indicator: " + token);
			}

			token = s.next();
			filename = token;
		} catch (IOException ioe) {
			throw new IOException("Parse error...");
		}
		return new FileMsg(isGet, filename);
	}
}
