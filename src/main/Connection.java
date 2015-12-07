package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public class Connection implements AutoCloseable {
  private final ObjectInputStream ois;              // socket input stream
  private final ObjectOutputStream oos;              // socket output stream
  private final Socket sock;
  private final boolean forGarbling;
  private AtomicInteger K = new AtomicInteger(0);

  public static Connection serverInstance(Socket sock) throws IOException {
    return new Connection(sock, true);
  }

  public static Connection clientInstance(Socket sock) throws IOException {
    return new Connection(sock, false);
  }

  private Connection(Socket sock, boolean forGarbling) throws IOException {
    this.sock = sock;
    oos = new ObjectOutputStream(sock.getOutputStream());
    ois = new ObjectInputStream(sock.getInputStream());
    this.forGarbling = forGarbling;
  }

  public boolean isForGarbling() {
    return forGarbling;
  }

  public ObjectOutputStream getOos() {
    return oos;
  }

  public ObjectInputStream getOis() {
    return ois;
  }

  public void flush() throws IOException {
    oos.flush();
  }

  @Override
  public void close() throws IOException {
    sock.close();
    oos.close();
    ois.close();
  }

  public int wireSerial() {
    return K.getAndIncrement();
  }
}
