package fr.ens.biologie.genomique.kenetre.bin;

import static java.util.Collections.unmodifiableList;

import com.google.common.base.Strings;
import fr.ens.biologie.genomique.kenetre.bin.action.Action;
import fr.ens.biologie.genomique.kenetre.bin.action.ActionService;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * This class define the main class for Kenetre bin.
 *
 * @since 0.28
 * @author Laurent Jourdren
 */
public class Main {

  private static Attributes manifestAttributes;
  private static final String MANIFEST_FILE = "/META-INF/MANIFEST.MF";
  private static final String UNKNOWN_VERSION = "UNKNOWN_VERSION";

  /** The name of the application. */
  public static final String APP_NAME = "Kenetre";

  /** The name of the application. */
  public static final String APP_NAME_LOWER_CASE = APP_NAME.toLowerCase();

  /** The version of the application. */
  public static final String APP_VERSION_STRING = getVersion();

  /** Project email. */
  public static final String CONTACT_EMAIL = APP_NAME_LOWER_CASE + "@biologie.ens.fr";

  private static final String WEBSITE_URL = "https://github.com/GenomiqueENS/Kenetre";

  /** Licence text. */
  public static final String LICENSE_TXT =
      "This program is developed under the GNU Lesser General Public License"
          + " version 2.1 or later and CeCILL-C.";

  /** About string, plain text version. */
  public static final String ABOUT_TXT =
      APP_NAME
          + " version "
          + APP_VERSION_STRING
          + " is a set of library for NGS analysis and useful programs.\n\nAuthors:\n"
          + " Laurent Jourdren (Project leader and maintainer)\n"
          + "Contacts:\n"
          + " Email: "
          + CONTACT_EMAIL
          + "\n"
          + " Website: "
          + WEBSITE_URL
          + "\n"
          + "Copyright IBENS genomics core facility\n"
          + LICENSE_TXT
          + "\n";

  /** The welcome message. */
  public static final String WELCOME_MSG = APP_NAME + " version " + APP_VERSION_STRING;

  private static Main main;
  private final List<String> args;
  private int errorExitCode = 0;
  private Action action;
  private List<String> actionArgs;

  /**
   * Exit the application.
   *
   * @param exitCode exit code
   */
  public static void exit(final int exitCode) {

    System.exit(exitCode);
  }

  /**
   * Print error message to the user and exits the application.
   *
   * @param e Exception
   * @param message message to show to the use
   */
  public static void errorExit(final Throwable e, final String message) {

    errorExit(e, message, true);
  }

  /**
   * Print error message to the user and exits the application.
   *
   * @param e Exception
   * @param message message to show to the use
   * @param logMessage true if message must be logged
   */
  public static void errorExit(final Throwable e, final String message, final boolean logMessage) {

    errorExit(e, message, logMessage, 1);
  }

  /**
   * Print error message to the user and exits the application.
   *
   * @param e Exception
   * @param message message to show to the use
   */
  public static void errorExit(final Throwable e, final String message, int exitCode) {

    errorExit(e, message, true, exitCode);
  }

  /**
   * Print error message to the user and exits the application.
   *
   * @param e Exception
   * @param message message to show to the use
   * @param logMessage true if message must be logged
   */
  public static void errorExit(
      final Throwable e, final String message, final boolean logMessage, int exitCode) {

    System.err.println("\n=== " + APP_NAME + " Error ===");
    System.err.println(message);

    printStackTrace(e);

    exit(exitCode);
  }

  /**
   * Show a message and then exit.
   *
   * @param message the message to show
   */
  public static void showMessageAndExit(final String message) {

    System.out.println(message);
    exit(0);
  }

  /**
   * Show a message and then exit.
   *
   * @param message the message to show
   */
  public static void showErrorMessageAndExit(final String message) {

    System.err.println(message);
    exit(1);
  }

  /**
   * Print the stack trace for an exception.
   *
   * @param e Exception
   */
  private static void printStackTrace(final Throwable e) {

    System.err.println("\n=== " + APP_NAME + " Debug Stack Trace ===");
    e.printStackTrace();
    System.err.println();
  }

  //
  // Parsing methods
  //

  /**
   * Show command line help.
   *
   * @param options Options of the software
   */
  protected void help(final Options options) {

    // Show help message
    final HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("Kenetre [options] action arguments", options);

    System.out.println("Available actions:");
    for (Action action : ActionService.getInstance().getActions()) {

      if (!action.isHidden()) {

        System.out.println(
            Strings.padEnd(" - " + action.getName(), 23, ' ') + action.getDescription());
      }
    }

    System.exit(0);
  }

  /**
   * Create options for command line
   *
   * @return an Options object
   */
  @SuppressWarnings("static-access")
  protected Options makeOptions() {

    // Create Options object
    final Options options = new Options();

    options.addOption("v", "version", false, "show version of the software");
    options.addOption("about", false, "display information about this software");
    options.addOption("h", "help", false, "display this help");
    options.addOption(
        "l", "license", false, "display information about the license of this software");
    options.addOption("e", "exit-code", false, "return a non zero exit code if an error occurs");

    return options;
  }

  /**
   * Parse the options of the command line
   *
   * @return the number of options argument in the command line
   */
  private int parseCommandLine() {

    final Options options = makeOptions();
    final CommandLineParser parser = new DefaultParser();
    final String[] argsArray = this.args.toArray(new String[0]);

    int argsOptions = 0;

    try {

      // parse the command line arguments
      final CommandLine line = parser.parse(options, argsArray, true);

      // Help option
      if (line.hasOption("help")) {
        help(options);
      }

      // About option
      if (line.hasOption("about")) {
        showMessageAndExit(ABOUT_TXT);
      }

      // Version option
      if (line.hasOption("version")) {
        showMessageAndExit(WELCOME_MSG);
      }

      // Licence option
      if (line.hasOption("license")) {
        showMessageAndExit(LICENSE_TXT);
      }

    } catch (ParseException e) {
      errorExit(e, "Error while parsing command line arguments: " + e.getMessage());
    }

    // No arguments found
    if (this.args == null || this.args.size() == argsOptions) {

      showErrorMessageAndExit(
          "This program needs one argument." + " Use the -h option to get more information.\n");
    }

    return argsOptions;
  }

  /**
   * Parse the action name and arguments from command line.
   *
   * @param optionsCount number of options in the command line
   */
  private void parseAction(final int optionsCount) {

    // Set action name and arguments
    final String actionName = this.args.get(optionsCount).trim().toLowerCase();
    this.actionArgs = this.args.subList(optionsCount + 1, this.args.size());

    // Search action
    this.action = ActionService.getInstance().newService(actionName);

    // Action not found ?
    if (this.action == null) {
      showErrorMessageAndExit(
          "Unknown action: "
              + actionName
              + ".\n"
              + "type: "
              + APP_NAME_LOWER_CASE
              + " -help for more help.\n");
    }
  }

  /**
   * Get command line arguments.
   *
   * @return Returns the arguments
   */
  public List<String> getArgs() {

    return unmodifiableList(this.args);
  }

  /**
   * Get the action.
   *
   * @return Returns the action
   */
  public Action getAction() {

    return this.action;
  }

  /**
   * Get the action arguments.
   *
   * @return Returns the actionArgs
   */
  public List<String> getActionArgs() {

    return unmodifiableList(this.actionArgs);
  }

  /**
   * Get the exit code to use when an error occurs.
   *
   * @return Returns the exit code to use when an error occurs
   */
  public int getErrorExitCode() {

    return this.errorExitCode;
  }

  //
  // Manifest methods
  //

  private static String getVersion() {

    final String version = getManifestProperty("Specification-Version");

    return version != null ? version : UNKNOWN_VERSION;
  }

  private static String getManifestProperty(final String propertyKey) {

    if (propertyKey == null) {
      return null;
    }

    readManifest();

    if (manifestAttributes == null) {
      return null;
    }

    return manifestAttributes.getValue(propertyKey);
  }

  private static synchronized void readManifest() {

    if (manifestAttributes != null) {
      return;
    }

    try {

      Class<?> clazz = Main.class;
      String className = clazz.getSimpleName() + ".class";
      String classPath = clazz.getResource(className).toString();

      final String manifestPath;
      if (!classPath.startsWith("jar")) {
        // Class not from JAR

        String basePath =
            classPath.substring(
                0, classPath.length() - clazz.getName().length() - ".class".length());
        manifestPath = basePath + MANIFEST_FILE;

      } else {
        manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + MANIFEST_FILE;
      }

      Manifest manifest = new Manifest(new URL(manifestPath).openStream());
      manifestAttributes = manifest.getMainAttributes();

    } catch (IOException e) {
    }
  }

  //
  // Constructor
  //

  /**
   * Constructor.
   *
   * @param args command line argument.
   */
  Main(final String[] args) {

    this.args = Arrays.asList(args);

    // Parse the command line
    final int optionsCount = parseCommandLine();

    // Parse action name and action arguments from command line
    parseAction(optionsCount);
  }

  //
  // Main method
  //

  /**
   * Main method of the program.
   *
   * @param args command line arguments
   */
  public static void main(final String[] args) {

    if (main != null) {
      throw new IllegalAccessError("Main method cannot be run twice.");
    }

    // Initialize the main class
    main = new Main(args);

    // Get the action to execute
    final Action action = main.getAction();

    try {

      // Run action
      action.action(main.getActionArgs());

    } catch (Throwable e) {
      errorExit(e, e.getMessage(), main.getErrorExitCode());
    }
  }
}
