package ru.aardvark.sc.trax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;

class InstanceLauncher {
  public static final Logger LOGGER = LoggerFactory.getLogger(InstanceLauncher.class);

  private final String sc2ExecutableLocation;
  private final String port;
  private final String sc2WorkDir;
  private final String ipAddress;


  public InstanceLauncher(String port) {
    this.sc2ExecutableLocation = new ExecutableLocator().findExecutableLocation();

    String sc2HomeDirPath = new File(sc2ExecutableLocation).toPath().getParent().getParent().getParent().toString();
    sc2WorkDir = Paths.get(sc2HomeDirPath, "Support64").toString();
    this.port = port;
    ipAddress = "127.0.0.1";
  }

  public Process launch() {
    List<String> sc2Args = new ArrayList<>();
    sc2Args.add(this.sc2ExecutableLocation);
    sc2Args.add("-listen");
    sc2Args.add(ipAddress);
    sc2Args.add("-port");
    sc2Args.add(port);
    sc2Args.add("-displayMode");
    sc2Args.add("0");
    ProcessBuilder pb = new ProcessBuilder(sc2Args);
    pb.directory(new File(sc2WorkDir));
    File log = new File("log.txt");
    pb.redirectErrorStream(true);
    pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
    try {
      Process process = pb.start();
      assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
      assert pb.redirectOutput().file() == log;
      assert process.getInputStream().read() == -1;
      return process;
    } catch (IOException e) {
      e.printStackTrace();
    }
    LOGGER.error("Unable to start sc2 process");
    System.exit(-1);
    return null;
  }

  private String instanceLaunched() {
    return "StarCraft II instance launched at " + ipAddress + ":" + port + "/sc2api";
  }

  private boolean isAlive() {
    try {
      Socket socket = new Socket(ipAddress, Integer.valueOf(port));
      boolean connected = socket.isConnected();
      if (connected) {
        URL url = new URL("http://" + ipAddress + ":" + port + "/sc2api");
        URLConnection connection = url.openConnection();
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        if (!headerFields.isEmpty()) {
          LOGGER.info(instanceLaunched());
          return true;
        }
      }
      return connected;
    } catch (IOException e) {
      LOGGER.info("Waiting for server to become live");
      return false;
    }
  }

  public CompletableFuture<Process> startClient() {
    return CompletableFuture.supplyAsync(() -> {
      Process sc2Process = this.launch();
      while (!this.isAlive()) {
        LockSupport.parkNanos(1_000_000);
      }
      return sc2Process;
    });
  }

  String getWsUri() {
    return "ws://" + ipAddress + ":" + port + "/sc2api";
  }
}
