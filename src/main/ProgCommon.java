// Copyright (C) 2010 by Yan Huang <yh8h@virginia.edu>

package main;

import circuit.Circuit;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public abstract class ProgCommon {
  public static ObjectOutputStream oos = null;              // socket output stream
  public static ObjectInputStream ois = null;              // socket input stream
  public static Circuit[] ccs;
}