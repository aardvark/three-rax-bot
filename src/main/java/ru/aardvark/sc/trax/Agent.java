package ru.aardvark.sc.trax;

import org.springframework.messaging.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Agent {

  private final InstanceLauncher launcher;
  private BlockingQueue<Message> input = new LinkedBlockingQueue<>();
  private BlockingQueue<Message> output = new LinkedBlockingQueue<>();
  private Process process;

  public Agent(InstanceLauncher launcher) {
    this.launcher = launcher;
  }

  public void start() {
    process = launcher.startClient().join();
  }

  public int stop() {
    Process process = this.process.destroyForcibly();
    return process.exitValue();
  }

  public String getUri() {
    return launcher.getWsUri();
  }
}
