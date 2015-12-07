package main;

import circuit.Circuit;

import java.io.IOException;
import java.util.List;


public abstract class Program {
  private void buildCircuits(List<Circuit> circuits, Connection connection) throws IOException {
    for (Circuit circuit : circuits) {
      circuit.build(connection);
    }
  }

  public abstract Connection connect() throws IOException;

  public abstract List<Circuit> createCircuits();

  protected abstract void execCircuit();

  protected abstract void execTransfer();

  protected void execute() {
    execTransfer();

    execCircuit();

    interpretResult();
  }

  protected abstract void initializeOT();

  protected abstract void interpretResult();

  public final void run() throws IOException {
    try (Connection connection = connect()) {
      List<Circuit> circuits = createCircuits();
      buildCircuits(circuits, connection);
      initializeOT();
      execute();
    }
  }
}