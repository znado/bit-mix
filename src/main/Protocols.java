package main;

import circuit.Circuit;

import java.io.IOException;
import java.util.List;


/**
 * @author nschank (Nicolas Schank)
 * @version 1.1
 */
public final class Protocols {
  /**
   * Utility class - not meant to be instantiated.
   */
  private Protocols() {}

  public static void buildCircuits(Iterable<Circuit> circuits, Connection connection) throws IOException {
    for (Circuit circuit : circuits) {
      circuit.build(connection);
    }
  }
}
