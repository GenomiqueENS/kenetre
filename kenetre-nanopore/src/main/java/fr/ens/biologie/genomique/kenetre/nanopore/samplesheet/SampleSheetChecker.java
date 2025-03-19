package fr.ens.biologie.genomique.kenetre.nanopore.samplesheet;

import static fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet.checkExpansionKit;
import static fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet.checkFlowCellProductCode;
import static fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet.checkSequencingKit;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Splitter;
import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet.Barcode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class allow to check Nanopore product code (e.g. flow cells, sequencing kits, expansion
 * kits...) and other thing to be sure that a sample sheet can be used by MinKnow.
 *
 * @since 0.20
 * @author Laurent Jourdren
 */
public class SampleSheetChecker {

  private final Set<String> knownFlowCellProductCodes = new HashSet<>();
  private final Set<String> knownSequencingKits = new HashSet<>();
  private final Set<String> knownExpansionKits = new HashSet<>();

  //
  // Product codes management
  //

  /**
   * Add known flow cell product codes.
   *
   * @param flowcellProductCodes a collection with flow cell product codes
   */
  public void addKnownFlowCellProductCodes(Collection<String> flowcellProductCodes) {

    requireNonNull(flowcellProductCodes);
    for (String flowcellProductCode : flowcellProductCodes) {
      addKnownFlowCellProductCode(flowcellProductCode);
    }
  }

  /**
   * Add a known flow cell product code.
   *
   * @param flowcellProductCode a known flow cell product code
   */
  public void addKnownFlowCellProductCode(String flowcellProductCode) {

    requireNonNull(flowcellProductCode);
    this.knownFlowCellProductCodes.add(checkFlowCellProductCode(flowcellProductCode));
  }

  /**
   * Add known sequencing kit codes.
   *
   * @param sequencingKits a collection with known sequencing kits codes
   */
  public void addKnownSequencingKits(Collection<String> sequencingKits) {

    requireNonNull(sequencingKits);
    for (String sequencingKit : sequencingKits) {
      addKnownSequencingKit(sequencingKit);
    }
  }

  /**
   * Add a known sequencing kit code.
   *
   * @param sequencingKit a known sequencing kit code
   */
  public void addKnownSequencingKit(String sequencingKit) {

    requireNonNull(sequencingKit);
    this.knownSequencingKits.add(checkSequencingKit(sequencingKit));
  }

  /**
   * Add known expansion kit codes.
   *
   * @param expansionKits a collection with known expansion kits codes
   */
  public void addKnownExpansionKits(Collection<String> expansionKits) {

    requireNonNull(expansionKits);
    for (String expansionKit : expansionKits) {
      addKnownExpansionKit(expansionKit);
    }
  }

  /**
   * Add known expansion kit code.
   *
   * @param expansionKit a collection with known expansion kits code
   */
  public void addKnownExpansionKit(String expansionKit) {

    requireNonNull(expansionKit);
    this.knownExpansionKits.add(checkExpansionKit(expansionKit));
  }

  /**
   * Remove a known flow cell product code.
   *
   * @param flowcellProductCodeflow flow cell product code to remove
   */
  public void removeKnownFlowCellProductCodes(String flowcellProductCode) {

    requireNonNull(flowcellProductCode);
    this.knownFlowCellProductCodes.remove(flowcellProductCode.toUpperCase().trim());
  }

  /**
   * Remove a known sequencing kit code.
   *
   * @param sequencingKit sequencing kit code to remove
   */
  public void removeKnownSequencingKit(String sequencingKit) {

    requireNonNull(sequencingKit);
    this.knownSequencingKits.remove(sequencingKit.toUpperCase().trim());
  }

  /**
   * Remove a known expansing kit code.
   *
   * @param expansionKit expansion kit code to remove
   */
  public void removeKnownExpansionKit(String expansionKit) {

    requireNonNull(expansionKit);
    this.knownExpansionKits.remove(expansionKit.toUpperCase().trim());
  }

  /** Clear the known list of flow cell product codes. */
  public void clearKnownFlowCellProductCodes() {
    this.knownFlowCellProductCodes.clear();
  }

  /** Clear the known list of sequencing kits codes. */
  public void clearKnownSequencingKits() {
    this.knownSequencingKits.clear();
  }

  /** Clear the known list of expansion kits codes. */
  public void clearKnownExpensionKits() {
    this.knownExpansionKits.clear();
  }

  //
  // Check methods
  //

  /**
   * Check sample sheet.
   *
   * @param sampleSheet sample sheet to check
   * @throws KenetreException if there is an issue with sample sheet
   */
  public List<String> check(SampleSheet sampleSheet) throws KenetreException {

    requireNonNull(sampleSheet);

    List<String> warnings = new ArrayList<>();

    sampleSheet.validate();

    String flowCellProductCode = sampleSheet.getFlowCellProductCode().toUpperCase().trim();
    if (!this.knownFlowCellProductCodes.contains(flowCellProductCode)) {
      throw new KenetreException("Unknown flow cell product code: " + flowCellProductCode);
    }

    boolean first = true;
    for (String e :
        Splitter.on(' ').omitEmptyStrings().trimResults().splitToList(sampleSheet.getKit())) {

      if (first) {

        if (!this.knownSequencingKits.contains(e.toUpperCase())) {
          throw new KenetreException("Unknown sequencing kit: " + e);
        }
        first = false;
      } else {

        if (!this.knownExpansionKits.contains(e.toUpperCase())) {
          throw new KenetreException("Unknown expansion kit: " + e);
        }
      }
    }

    Set<String> aliases = new HashSet<>();
    for (Barcode b : sampleSheet.getBarcodes()) {
      String a = b.getAlias();
      if (aliases.contains(a)) {
        warnings.add("Duplicate alias found: " + a);
      }
      aliases.add(a);
    }

    return warnings;
  }

  //
  // Utility methods
  //

  private static List<String> readResource(String resource) {

    List<String> result = new ArrayList<>();

    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(SampleSheetChecker.class.getResourceAsStream(resource)))) {

      String line = null;

      while ((line = reader.readLine()) != null) {

        if (!line.isBlank()) {
          result.add(line.trim());
        }
      }
    } catch (IOException e) {
      return Collections.emptyList();
    }

    return result;
  }

  //
  // Objects methods
  //

  @Override
  public String toString() {
    return "SampleSheetChecker [knownFlowCellProductCodes="
        + knownFlowCellProductCodes
        + ", knownSequencingKits="
        + knownSequencingKits
        + ", knownExpensionKits="
        + knownExpansionKits
        + "]";
  }

  //
  // Constructor
  //

  /** Public constructor. */
  public SampleSheetChecker() {

    addKnownFlowCellProductCodes(readResource("/META-INF/flow_cell_product_codes.txt"));
    addKnownSequencingKits(readResource("/META-INF/sequencing_kits.txt"));
    addKnownExpansionKits(readResource("/META-INF/expansion_kits.txt"));
  }
}
