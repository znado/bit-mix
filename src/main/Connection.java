package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class Connection implements AutoCloseable {
  private final ObjectInputStream ois = null;              // socket input stream
  private final ObjectOutputStream oos = null;              // socket output stream
  private final Socket sock;

  public Connection(Socket sock) throws IOException {
    this.sock = sock;

    ProgCommon.oos = new ObjectOutputStream(sock.getOutputStream());
    ProgCommon.ois = new ObjectInputStream(sock.getInputStream());
  }

  @Override
  public void close() throws Exception {
    sock.close();
    oos.close();
    ois.close();
  }
}
