package fr.ens.biologie.genomique.kenetre.illumina;

import static fr.ens.biologie.genomique.kenetre.util.XMLUtils.getElementsByTagName;
import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This class allow to parse the DemultiplexingStats XML file.
 *
 * @author Laurent Jourdren
 * @since 0.18
 */
public class DemultiplexingStats {

  private List<Entry> entries = new ArrayList<>();

  public static class Entry {

    private final String flowcell;
    private final String projectName;
    private final String sampleName;
    private final String barcodeSeq;
    private final int laneNumber;
    private final int barcodeCount;
    private final int perfectBarcodeCount;

    /**
     * Get the flowcell id.
     *
     * @return the flowcell id
     */
    public String getFlowcell() {
      return flowcell;
    }

    /**
     * Get the project name.
     *
     * @return the project name
     */
    public String getProjectName() {
      return projectName;
    }

    /**
     * Get the sample name.
     *
     * @return the sample name
     */
    public String getSampleName() {
      return sampleName;
    }

    /**
     * Get the barcode sequence.
     *
     * @return the barcode sequence
     */
    public String getBarcodeSeq() {
      return barcodeSeq;
    }

    /**
     * Get the lane number.
     *
     * @return the lane number
     */
    public int getLaneNumber() {
      return laneNumber;
    }

    /**
     * Get the barcode count.
     *
     * @return the barcode count
     */
    public int getBarcodeCount() {
      return barcodeCount;
    }

    /**
     * Get the perfect barcode count.
     *
     * @return the perfect barcode count
     */
    public int getPerfectBarcodeCount() {
      return perfectBarcodeCount;
    }

    @Override
    public String toString() {
      return "Entry [flowcell="
          + flowcell
          + ", projectName="
          + projectName
          + ", sampleName="
          + sampleName
          + ", barcodeSeq="
          + barcodeSeq
          + ", laneNumber="
          + laneNumber
          + ", barcodeCount="
          + barcodeCount
          + ", perfectBarcodeCount="
          + perfectBarcodeCount
          + "]";
    }

    //
    // Constructor
    //

    private Entry(
        String flowcell,
        String projectName,
        String sampleName,
        String barcodeSeq,
        int laneNumber,
        int barcodeCount,
        int perfectBarcodeCount) {

      this.flowcell = flowcell;
      this.projectName = projectName;
      this.sampleName = sampleName;
      this.barcodeSeq = barcodeSeq;
      this.laneNumber = laneNumber;
      this.barcodeCount = barcodeCount;
      this.perfectBarcodeCount = perfectBarcodeCount;
    }
  }

  /**
   * Get the entries of the file.
   *
   * @return a list with the entries of the file
   */
  public List<Entry> entries() {

    return Collections.unmodifiableList(this.entries);
  }

  /**
   * Parses the document.
   *
   * @param document the document
   * @param data the data
   */
  private void parse(final Document document) {

    String flowcellId;
    String projectName;
    String sampleName;
    String barcodeSeq;
    int laneNumber;
    int barcodeCount;
    int perfectBarcodeCount;

    for (final Element flowcell : getElementsByTagName(document, "Flowcell")) {
      flowcellId = flowcell.getAttribute("flowcell-id");

      for (final Element project : getElementsByTagName(flowcell, "Project")) {
        projectName = project.getAttribute("name");

        for (final Element sample : getElementsByTagName(project, "Sample")) {
          sampleName = sample.getAttribute("name");

          for (final Element barcode : getElementsByTagName(sample, "Barcode")) {
            barcodeSeq = barcode.getAttribute("name");

            for (final Element lane : getElementsByTagName(barcode, "Lane")) {
              laneNumber = Integer.parseInt(lane.getAttribute("number"));

              barcodeCount = -1;
              perfectBarcodeCount = -1;

              List<Element> barcodeCountElements = getElementsByTagName(lane, "BarcodeCount");
              if (barcodeCountElements != null) {
                for (Element e : barcodeCountElements) {
                  barcodeCount = Integer.parseInt(e.getTextContent());
                }
              }

              List<Element> perfectBarcodeCountElements =
                  getElementsByTagName(lane, "PerfectBarcodeCount");
              if (perfectBarcodeCountElements != null) {
                for (Element e : perfectBarcodeCountElements) {
                  perfectBarcodeCount = Integer.parseInt(e.getTextContent());
                }
              }

              this.entries.add(
                  new Entry(
                      flowcellId,
                      projectName,
                      sampleName,
                      barcodeSeq,
                      laneNumber,
                      barcodeCount,
                      perfectBarcodeCount));
            }
          }
        }
      }
    }
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   *
   * @param file input file
   * @throws IOException if an error occurs while reading the input file
   */
  public DemultiplexingStats(Path file) throws IOException {

    this(new FileInputStream(file.toFile()));
  }

  /**
   * Public constructor.
   *
   * @param file input file
   * @throws IOException if an error occurs while reading the input file
   */
  public DemultiplexingStats(File file) throws IOException {

    this(new FileInputStream(file));
  }

  /**
   * Public constructor.
   *
   * @param in input stream
   * @throws IOException if an error occurs while reading the input file
   * @throws KenetreException
   */
  public DemultiplexingStats(InputStream in) throws IOException {

    requireNonNull(in);

    try (Reader reader = new InputStreamReader(in)) {

      final DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      final Document doc = dBuilder.parse(in);
      doc.getDocumentElement().normalize();

      // Parse document to update run data
      parse(doc);
    } catch (ParserConfigurationException | SAXException e) {
      throw new IOException("Unable to parse ConversionStats.xml file");
    }
  }
}
