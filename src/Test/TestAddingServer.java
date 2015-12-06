// Copyright (C) 2010 by Yan Huang <yhuang@virginia.edu>

package Test;

import main.AdditionCommon;
import main.AdditionServer;
import util.StopWatch;

import java.math.BigInteger;


class TestAddingServer {

  public static void main(String[] args) throws Exception {
    AdditionServer server = new AdditionServer(AdditionCommon.X);
    server.run();
  }
}