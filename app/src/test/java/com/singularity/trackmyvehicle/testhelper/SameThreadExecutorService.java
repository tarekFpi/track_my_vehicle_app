package com.singularity.trackmyvehicle.testhelper;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Imran Chowdhury on 8/12/2018.
 */
public class SameThreadExecutorService extends AbstractExecutorService {

  //volatile because can be viewed by other threads
  private volatile boolean terminated;

  @Override
  public void shutdown() {
    terminated = true;
  }

  @Override
  public boolean isShutdown() {
    return terminated;
  }

  @Override
  public boolean isTerminated() {
    return terminated;
  }

  @Override
  public boolean awaitTermination(long theTimeout, TimeUnit theUnit) throws InterruptedException {
    shutdown();
    return terminated;
  }

  @Override
  public List<Runnable> shutdownNow() {
    return Collections.emptyList();
  }

  @Override
  public void execute(Runnable theCommand) {
    theCommand.run();
  }
}