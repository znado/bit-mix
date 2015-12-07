package Test;

import main.AdditionClient;
import main.AdditionCommon;
import main.ProtocolClient;
import main.Program;
import util.StopWatch;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class TestAddingClient {
  public static void main(String[] args) throws Exception {
    StopWatch.pointTimeStamp("Starting program");
    ProtocolClient.serverIPname = "localhost";
    Program.iterCount = 1;

    AdditionClient client = new AdditionClient(AdditionCommon.Y);
    client.run();
  }
}
