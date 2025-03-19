package fr.ens.biologie.genomique.kenetre.log;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * This class define a logger using the java.util.logging API that write log in a file.
 *
 * @author Laurent Jourdren
 */
public class FileLogger extends AbstractJavaUtilLoggingLogger {

  @Override
  protected Handler createHandler(Map<String, String> conf) throws KenetreException {

    // Get Log path
    String logPath = Objects.toString(conf.get("log.file"), "");

    if (logPath.trim().isEmpty()) {
      throw new KenetreException("No log file defined");
    }

    try {

      Handler result = new FileHandler(logPath, true);

      if (conf.containsKey("log.level")) {
        String logLevelName = conf.get("log.level");
        Level logLevel = Level.parse(logLevelName.toUpperCase());
        result.setLevel(logLevel);
      }

      return result;
    } catch (SecurityException | IOException e) {
      throw new KenetreException(e);
    }
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   *
   * @param loggerName name of the logger
   * @param conf configuration
   * @throws KenetreException if an error occurs while creating the logger
   */
  public FileLogger(String loggerName, Map<String, String> conf) throws KenetreException {
    super(loggerName, conf);
  }
}
