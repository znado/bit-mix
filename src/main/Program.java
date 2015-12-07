package main;

public abstract class Program {
  protected abstract void createCircuits() throws Exception;

  protected abstract void execCircuit() throws Exception;

  protected abstract void execTransfer() throws Exception;

  protected void execute() throws Exception {
    execTransfer();

    execCircuit();

    interpretResult();
  }

  protected abstract void initializeOT() throws Exception;

  protected abstract void interpretResult() throws Exception;

  public void run() throws Exception {
    createCircuits();
    initializeOT();
    execute();
  }
}