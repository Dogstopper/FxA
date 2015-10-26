import java.io.IOException;

public interface MsgCoder {
  byte[] toWire(VoteMsg msg) throws IOException;
  VoteMsg fromWire(byte[] input) throws IOException;
}