package ru.aardvark.sc.trax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class ExecutableLocator {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableLocator.class);

  public ExecutableLocator() {}

  private static List<String> getLines(Path path) {
    try {
      return Files.readAllLines(path);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static String getExecutableLocation(List<String> strings) {
    String executableString = strings.get(0);
    String executableLocation = executableString.split("= ")[1];
    LOGGER.info("executableLocation: {}", executableLocation);
    return executableLocation;
  }

  String findExecutableLocation() {
    Optional<String> foundLocation =
      findExecutablePath().map(ExecutableLocator::getLines).map(ExecutableLocator::getExecutableLocation);

    if (!foundLocation.isPresent()) {
      LOGGER.error("StarCraft II executable not found! Exiting.");
      System.exit(1);
    }
    return foundLocation.get();
  }

  private Optional<Path> findExecutablePath() {
    String userHome = System.getProperty("user.home");
    LOGGER.info("user.home: {}", userHome);
    String executeInfoPath = userHome + "\\Documents\\StarCraft II\\ExecuteInfo.txt";
    LOGGER.info("executeInfoPath: {}", executeInfoPath);
    Path path = Paths.get(executeInfoPath);
    if (Files.exists(path)) {
      return Optional.of(path);
    } else {
      return Optional.empty();
    }
  }
}
