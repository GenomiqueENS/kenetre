package fr.ens.biologie.genomique.kenetre.illumina;

import static com.google.common.base.Preconditions.checkArgument;
import static fr.ens.biologie.genomique.kenetre.util.XMLUtils.getTagValue;
import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.KenetreRuntimeException;
import fr.ens.biologie.genomique.kenetre.util.XMLUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This class allow to collect data from the runParameters.xml file.
 *
 * @author Laurent Jourdren
 * @since 2.0
 */
public class RunParameters {

  private static final String UNKNOWN_VALUE = "Unknown";

  private final String runParametersVersion;
  private final String applicationName;
  private final String applicationVersion;
  private final String rtaVersion;
  private final int rtaMajorVersion;

  //
  // Getters
  //

  /**
   * Get the version of the format of the file.
   *
   * @return the version of the format of the file
   */
  public String getRunParametersVersion() {
    return this.runParametersVersion;
  }

  /**
   * Get application name.
   *
   * @return the application name
   */
  public String getApplicationName() {
    return this.applicationName;
  }

  /**
   * Get the application version.
   *
   * @return the application version
   */
  public String getApplicationVersion() {
    return this.applicationVersion;
  }

  /**
   * Get the RTA version.
   *
   * @return RTA version
   */
  public String getRTAVersion() {
    return this.rtaVersion;
  }

  /**
   * Get the RTA major version.
   *
   * @return the RTA major version
   */
  public int getRTAMajorVersion() {
    return this.rtaMajorVersion;
  }

  /**
   * Get the sequencer family.
   *
   * @return the sequencer family
   */
  public String getSequencerFamily() {

    if (this.applicationName != null) {
      return this.applicationName.substring(0, this.applicationName.indexOf(' '));
    } else {
      return UNKNOWN_VALUE;
    }
  }

  /**
   * Get the sequencer model.
   *
   * @return the sequencer model
   */
  public String getSequencerModel() {

    if (this.applicationName != null) {
      return this.applicationName.replace("Control Software", "").trim();
    } else {
      return UNKNOWN_VALUE;
    }
  }

  //
  // Object methods
  //

  @Override
  public String toString() {

    return "RunParameters{runParametersVersion="
        + runParametersVersion
        + ", applicationName="
        + this.applicationName
        + ", applicationVersion="
        + applicationVersion
        + ", rtaVersion="
        + rtaVersion
        + "}";
  }

  //
  // Parsers
  //

  /**
   * Parses the run parameter file.
   *
   * @param filepath the path to the run info file
   * @return a RunParameters object with the information of the parsed file
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static RunParameters parse(final String filepath)
      throws ParserConfigurationException, SAXException, IOException {

    requireNonNull(filepath, "RunInfo.xml path cannot be null");

    return parse(new File(filepath));
  }

  /**
   * Parses the run parameter file.
   *
   * @param file the run info file
   * @return a RunParameters object with the information of the parsed file
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static RunParameters parse(final File file)
      throws ParserConfigurationException, SAXException, IOException {

    requireNonNull(file, "file cannot be null");

    checkArgument(file.isFile(), "RunParameters.xml does not exists or is not a file");

    return parse(new FileInputStream(file));
  }

  /**
   * Parses the run parameter file.
   *
   * @param is the input stream on run info file
   * @return a RunParameters object with the information of the parsed file
   * @throws ParserConfigurationException the parser configuration exception
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static RunParameters parse(final InputStream is)
      throws ParserConfigurationException, SAXException, IOException {

    requireNonNull(is, "RunParameters.xml input stream cannot be null");

    try (InputStream in = is) {

      final Document doc;

      final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      doc = dBuilder.parse(in);
      doc.getDocumentElement().normalize();

      return parse(doc);
    }
  }

  /**
   * Parses the run info file.
   *
   * @param document the document from run info XML file
   */
  private static RunParameters parse(final Document document) {

    for (Element e : XMLUtils.getElementsByTagName(document, "RunParameters")) {

      String runParametersVersion = getTagValue(e, "RunParametersVersion");

      return getTagValue(e, "RtaVersion") == null
          ? parseRTA12(runParametersVersion, e)
          : parseRTA3(runParametersVersion, e);
    }

    throw new KenetreRuntimeException("Invalid format of the RunParameters.xml file");
  }

  private static RunParameters parseRTA12(final String runParametersVersion, Element e) {

    String rtaVersion = getTagValue(e, "RTAVersion");

    final List<Element> elements = XMLUtils.getElementsByTagName(e, "Setup");
    if (!elements.isEmpty()) {

      final Element setupElement = elements.get(0);

      final String applicationName = getTagValue(setupElement, "ApplicationName");
      final String applicationVersion = getTagValue(setupElement, "ApplicationVersion");

      if (rtaVersion == null) {
        rtaVersion = getTagValue(setupElement, "RTAVersion");
      }
      return new RunParameters(
          runParametersVersion, applicationName, applicationVersion, rtaVersion);
    }

    throw new KenetreRuntimeException("Invalid format of the RunParameters.xml file");
  }

  private static RunParameters parseRTA3(final String runParametersVersion, Element e) {

    String rtaVersion = getTagValue(e, "RtaVersion");
    String applicationName = getTagValue(e, "ApplicationName");
    String applicationVersion = getTagValue(e, "ApplicationVersion");

    return new RunParameters(runParametersVersion, applicationName, applicationVersion, rtaVersion);
  }

  //
  // Constructor
  //

  /** Private constructor. */
  private RunParameters(
      final String runParametersVersion,
      final String applicationName,
      final String applicationVersion,
      final String rtaVersion) {

    this.runParametersVersion = runParametersVersion != null ? runParametersVersion : UNKNOWN_VALUE;
    this.applicationName = applicationName;
    this.applicationVersion = applicationVersion;
    this.rtaVersion = rtaVersion;

    int rtaMajorVersion = -1;

    if (this.rtaVersion != null) {

      try {
        rtaMajorVersion = Integer.parseInt(rtaVersion.substring(0, rtaVersion.indexOf('.')));
      } catch (NumberFormatException e) {
        // Do nothing
      }
    }

    this.rtaMajorVersion = rtaMajorVersion;
  }
}
