package com.singularity.trackmyvehicle.utils;

/**
 * Created by Imran Chowdhury on 8/12/2018.
 */
public class Log {
  public static int d(String tag, String msg) {
    System.out.println("DEBUG: " + tag + ": " + msg);
    return 0;
  }
}
