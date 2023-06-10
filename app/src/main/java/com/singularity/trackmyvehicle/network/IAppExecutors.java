package com.singularity.trackmyvehicle.network;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Imran Chowdhury on 8/12/2018.
 */
public class IAppExecutors implements AppExecutors {

  @Override
  public void ioThread(@NotNull Function0<Unit> f) {
    AppThreadExecutorsKt.ioThread(f);
  }

  @Override
  public void networkThread(@NotNull Function0<Unit> f) {
    AppThreadExecutorsKt.networkThread(f);
  }

  @Override
  public void mainThread(@NotNull Function0<Unit> f) {
    AppThreadExecutorsKt.mainThread(f);
  }
}
