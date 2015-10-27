import java.io.IOException;

public interface MsgCoder {
  byte[] toWire(FileMsg msg) throws IOException;
  FileMsg fromWire(byte[] input) throws IOException;
}
