package fr.ens.biologie.genomique.kenetre.nanopore.samplesheet;

import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * This class define a sample sheet for Nanopore software.
 *
 * @since 0.20
 * @author Laurent Jourdren
 */
public class SampleSheet {

  private String protocolRunId;
  private String flowCellId;
  private String positionId;
  private String sampleId;
  private String experimentId;
  private String flowCellProductCode;
  private String kit;
  private Map<String, String> otherFields = new LinkedHashMap<>();

  private final Map<String, Barcode> barcodes = new LinkedHashMap<>();

  private enum BarcodeType {
    TEST_SAMPLE,
    POSITIVE_CONTROL,
    NEGATIVE_CONTROL,
    NO_TEMPLATE_CONTROL
  }

  /** This subclass define a barcode. */
  public static class Barcode {

    private final String barcode;
    private final String internalBarcode;
    private final String externalBarcode;
    private String alias;
    private BarcodeType type;
    private String description;

    //
    // Getters
    //

    /**
     * Get the barcode.
     *
     * @return the barcode
     */
    public String getBarcode() {

      return this.barcode;
    }

    /**
     * Get the internal barcode.
     *
     * @return the internal barcode
     */
    public String getInternalBarcode() {

      return this.internalBarcode;
    }

    /**
     * Get the external barcode.
     *
     * @return the external barcode
     */
    public String getExternalBarcode() {

      return this.externalBarcode;
    }

    /**
     * Get the alias.
     *
     * @return the alias
     */
    public String getAlias() {

      return this.alias;
    }

    /**
     * Get the type of barcode sample.
     *
     * @return the type of barcode sample
     */
    public BarcodeType getType() {

      return this.type;
    }

    /**
     * Get the description of the sample.
     *
     * @return the description of the sample
     */
    public String getDescription() {

      return this.description;
    }

    //
    // Setters
    //

    /**
     * Set the barcode type.
     *
     * @param type the barcode type to set
     */
    public void setType(BarcodeType type) {

      requireNonNull(type);

      this.type = type;
    }

    /**
     * Set the barcode type.
     *
     * @param type the barcode type to set
     */
    public void setType(String type) {

      requireNonNull(type);

      try {
        setType(BarcodeType.valueOf(type.trim().toUpperCase()));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid type: " + type);
      }
    }

    /**
     * Set the description.
     *
     * @param description the description to set
     */
    public void setDescripton(String description) {

      requireNonNull(description);

      this.description = description;
    }

    //
    // Other method
    //
    /**
     * Check if barcode use a dual barcoding.
     *
     * @return true if barcode use a dual barcoding
     */
    public boolean isDualBarcoding() {

      return this.barcode == null;
    }

    /**
     * Rename the alias.
     *
     * @param newAlias the new alias for the barcode
     */
    public void renameAlias(String newAlias) {

      requireNonNull(newAlias);

      newAlias = newAlias.trim();

      if (!checkAlias(newAlias)) {
        throw new IllegalArgumentException("Invalid new alias: " + newAlias);
      }

      this.alias = newAlias;
    }

    private String getKey() {

      if (this.barcode == null) {
        return createKey(this.internalBarcode, this.externalBarcode);
      }

      return createKey(this.barcode);
    }

    //
    // Object methods
    //

    @Override
    public int hashCode() {

      return Objects.hash(
          this.barcode, this.internalBarcode, this.externalBarcode, this.alias, this.type);
    }

    @Override
    public boolean equals(Object o) {

      if (this == o) return true;
      if (!(o instanceof Barcode)) return false;
      Barcode other = (Barcode) o;

      return Objects.equals(this.barcode, other.barcode)
          && Objects.equals(this.internalBarcode, other.internalBarcode)
          && Objects.equals(this.externalBarcode, other.externalBarcode)
          && Objects.equals(this.alias, other.alias)
          && Objects.equals(this.type, other.type)
          && Objects.equals(this.description, other.description);
    }

    @Override
    public String toString() {
      return "Barcode [barcode="
          + barcode
          + ", internalBarcode="
          + internalBarcode
          + ", externalBarcode="
          + externalBarcode
          + ", alias="
          + alias
          + ", type="
          + type
          + ", description="
          + description
          + "]";
    }

    //
    // Constructor
    //

    private Barcode(String barcode, String alias) {

      requireNonNull(barcode);
      requireNonNull(alias);

      barcode = barcode.trim().toLowerCase();
      alias = alias.trim();

      if (!checkBarcodeName("barcode", barcode)) {
        throw new IllegalArgumentException("Invalid barcode: " + barcode);
      }

      if (!checkAlias(alias)) {
        throw new IllegalArgumentException("Invalid alias: " + alias);
      }

      this.barcode = barcode;
      this.internalBarcode = null;
      this.externalBarcode = null;
      this.alias = alias;
    }

    private Barcode(String internalBarcode, String externalBarcode, String alias) {

      requireNonNull(internalBarcode);
      requireNonNull(externalBarcode);
      requireNonNull(alias);

      internalBarcode = internalBarcode.trim().toLowerCase();
      externalBarcode = externalBarcode.trim().toLowerCase();
      alias = alias.trim();

      if (!checkBarcodeName("internal", internalBarcode)) {
        throw new IllegalArgumentException("Invalid internal barcode: " + internalBarcode);
      }

      if (!checkBarcodeName("external", externalBarcode)) {
        throw new IllegalArgumentException("Invalid external barcode: " + externalBarcode);
      }

      if (!checkAlias(alias)) {
        throw new IllegalArgumentException("Invalid alias: " + alias);
      }

      this.barcode = null;
      this.internalBarcode = internalBarcode;
      this.externalBarcode = externalBarcode;
      this.alias = alias;
    }
  }

  //
  // Getters
  //

  /**
   * Get the protocol run Id.
   *
   * @return the protocol run Id.
   */
  public String getProtocolRunId() {
    return this.protocolRunId;
  }

  /**
   * Get the flow cell Id.
   *
   * @return the flow cell Id
   */
  public String getFlowCellId() {
    return this.flowCellId;
  }

  /**
   * Get the position Id.
   *
   * @return the position Id
   */
  public String getPositionId() {
    return this.positionId;
  }

  /**
   * Get the sample Id.
   *
   * @return the sample Id
   */
  public String getSampleId() {
    return this.sampleId;
  }

  /**
   * @return the experimentId
   */
  public String getExperimentId() {
    return this.experimentId;
  }

  /**
   * Get the flow cell product code.
   *
   * @return the flowCellProductCode
   */
  public String getFlowCellProductCode() {
    return this.flowCellProductCode;
  }

  /**
   * Get the kit.
   *
   * @return the kit
   */
  public String getKit() {
    return this.kit;
  }

  /**
   * Test is dual barcoding enabled.
   *
   * @return true if dual barcoding is enabled
   */
  public boolean isDualBarcoding() {

    if (!this.barcodes.isEmpty()) {
      return this.barcodes.values().iterator().next().isDualBarcoding();
    }

    return false;
  }

  /**
   * Get other field.
   *
   * @param fieldName field name
   * @return the value for the field
   */
  public String getOtherField(String fieldName) {

    return this.otherFields.get(fieldName);
  }

  /**
   * Get other field names.
   *
   * @return a set with the other field names
   */
  public Set<String> getOtherFieldNames() {

    return Collections.unmodifiableSet(this.otherFields.keySet());
  }

  //
  // Setters
  //

  /**
   * Set the protocol run id.
   *
   * @param protocolRunId the protocol run id to set
   */
  public void setProtocolRunId(String protocolRunId) {

    protocolRunId = protocolRunId.trim();

    try {
      UUID.fromString(protocolRunId);
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("protocolRunId argument is not a valid UUID");
    }

    this.protocolRunId = protocolRunId;
  }

  /**
   * Set the flow cell id.
   *
   * @param flowCellId the flowCellId to set
   */
  public void setFlowCellId(String flowCellId) {
    this.flowCellId = flowCellId;
  }

  /**
   * Set the position id.
   *
   * @param positionId the positionId to set
   */
  public void setPositionId(String positionId) {
    this.positionId = positionId;
  }

  /**
   * Set the sample Id.
   *
   * @param sampleId the sampleId to set
   */
  public void setSampleId(String sampleId) {
    this.sampleId = sampleId;
  }

  /**
   * Set the experiment id.
   *
   * @param experimentId the experimentId to set
   */
  public void setExperimentId(String experimentId) {

    requireNonNull(experimentId);
    if (experimentId.isBlank()) {
      throw new IllegalArgumentException("experimentId argument cannot be empty");
    }
    this.experimentId = experimentId;
  }

  /**
   * Set the flow cell product code.
   *
   * @param flowCellProductCode the flowCellProductCode to set
   */
  public void setFlowCellProductCode(String flowCellProductCode) {

    this.flowCellProductCode = checkFlowCellProductCode(flowCellProductCode);
  }

  /**
   * Set the kit.
   *
   * @param kit the kit to set
   */
  public void setKit(String kit) {

    requireNonNull(kit);
    if (kit.isBlank()) {
      throw new IllegalArgumentException("kit argument cannot be empty");
    }
    if (!kit.startsWith("SQK-")) {
      throw new IllegalArgumentException("Invalid kit argument: " + kit);
    }

    this.kit = kit.trim();
  }

  /**
   * Set the sequencing kit.
   *
   * @param sequencingKit the sequencing kit
   */
  public void setSequencingKit(String sequencingKit) {

    setKit(checkSequencingKit(sequencingKit));
  }

  /**
   * Set the expansionKit.
   *
   * @param expansionKit the expension kit
   */
  public void addExpansionKit(String expansionKit) {

    this.kit = kit + ' ' + checkExpansionKit(expansionKit);
  }

  /**
   * Set other field.
   *
   * @param key field name
   * @param value field value
   */
  public void setOtherField(String key, String value) {

    requireNonNull(key);
    requireNonNull(value);

    this.otherFields.put(key, value);
  }

  /** Remove other fields. */
  public void removeOtherFields() {

    this.otherFields.clear();
  }

  //
  // Barcode management
  //

  /**
   * Test if a barcode exists in the sample sheet.
   *
   * @return true if a barcode exists in the sample sheet
   */
  public boolean isBarcode() {

    return !this.barcodes.isEmpty();
  }

  /**
   * Get the barcodes of the sample sheet.
   *
   * @return the barcodes of the sample sheet
   */
  public List<Barcode> getBarcodes() {

    return Collections.unmodifiableList(new ArrayList<>(this.barcodes.values()));
  }

  /**
   * Test if a barcode exists.
   *
   * @param barcode the name of the barcode
   * @return a barcode object
   */
  public boolean containsBarcode(String barcode) {

    requireNonNull(barcode);
    return this.barcodes.containsKey(createKey(barcode));
  }

  /**
   * Get a barcode object from the name of the barcode.
   *
   * @param barcode the name of the barcode
   * @return a barcode object
   */
  public Barcode getBarcode(String barcode) {

    requireNonNull(barcode);
    return this.barcodes.get(createKey(barcode));
  }

  /**
   * Test if a barcode exists.
   *
   * @param internalBarcode the name of the internal barcode
   * @param internalBarcode the name of the external barcode
   * @return a barcode object
   */
  public boolean containsBarcode(String internalBarcode, String externalBarcode) {

    requireNonNull(internalBarcode);
    requireNonNull(externalBarcode);
    return this.barcodes.containsKey(createKey(internalBarcode, externalBarcode));
  }

  /**
   * Get a barcode object from the names of the internal and external barcodes.
   *
   * @param internalBarcode the name of the internal barcode
   * @param internalBarcode the name of the external barcode
   * @return a barcode object
   */
  public Barcode getBarcode(String internalBarcode, String externalBarcode) {

    requireNonNull(internalBarcode);
    requireNonNull(externalBarcode);
    return this.barcodes.get(createKey(internalBarcode, externalBarcode));
  }

  /**
   * Add a new barcode.
   *
   * @param barcode the barcode to add
   * @param alias alias of the barcode
   * @return the added barcode
   */
  public Barcode addBarcode(String barcode, String alias) {

    Barcode b = new Barcode(barcode, alias);
    if (this.barcodes.containsKey(b.getKey())) {
      throw new IllegalArgumentException("Barcode already exists in sample sheet: " + barcode);
    }

    if (!this.barcodes.isEmpty()) {
      Barcode first = this.barcodes.values().iterator().next();
      if (first.isDualBarcoding()) {
        throw new IllegalArgumentException(
            "Cannot add a single barcode to a dual barcode sample sheet: " + barcode);
      }
    }

    this.barcodes.put(b.getBarcode(), b);

    return b;
  }

  /**
   * Add a new barcode.
   *
   * @param internalBarcode the internal barcode to add
   * @param externalBarcode the external barcode to add
   * @param alias alias of the barcode
   * @return the added barcode
   */
  public Barcode addBarcode(String internalBarcode, String externalBarcode, String alias) {

    Barcode b = new Barcode(internalBarcode, externalBarcode, alias);
    if (this.barcodes.containsKey(b.getKey())) {
      throw new IllegalArgumentException(
          "Barcode already exists in sample sheet: " + internalBarcode + " + " + externalBarcode);
    }

    if (!this.barcodes.isEmpty()) {
      Barcode first = this.barcodes.values().iterator().next();
      if (!first.isDualBarcoding()) {
        throw new IllegalArgumentException(
            "Cannot add a dual barcode to a single barcode sample sheet: "
                + internalBarcode
                + " + "
                + externalBarcode);
      }
    }

    this.barcodes.put(b.getBarcode(), b);

    return b;
  }

  /**
   * Remove a barcode.
   *
   * @param barcode the barcode object to remove
   * @return true if the barcode have been removed
   */
  public boolean removeBarcode(Barcode barcode) {

    requireNonNull(barcode);
    return this.barcodes.remove(barcode.getKey()) != null;
  }

  /**
   * Remove a barcode.
   *
   * @param barcode the name of the barcode to remove
   * @return true if the barcode have been removed
   */
  public void removeBarcode(String barcode) {

    requireNonNull(barcode);
    this.barcodes.remove(createKey(barcode));
  }

  /**
   * Remove a barcode.
   *
   * @param internalBarcode the internal barcode of the barcode to remove
   * @param externalBarcode the external barcode of the barcode to remove
   * @return true if the barcode have been removed
   */
  public void removeBarcode(String internalBarcode, String externalBarcode) {

    requireNonNull(internalBarcode);
    requireNonNull(externalBarcode);
    this.barcodes.remove(createKey(internalBarcode, externalBarcode));
  }

  /** Remove barcode descriptions. */
  public void removeBarcodeDescription() {

    for (Barcode b : this.barcodes.values()) {
      b.description = null;
    }
  }

  //
  // Existing fields
  //

  /**
   * Check if protocol run Id exists.
   *
   * @return true if protocol run Id exists
   */
  public boolean isProtocolRunIdField() {

    return this.protocolRunId != null;
  }

  /**
   * Check if flow cell Id exists.
   *
   * @return true if flow cell Id exists
   */
  public boolean isFlowCellIdField() {

    return this.flowCellId != null;
  }

  /**
   * Check if position Id exists.
   *
   * @return true if position Id exists
   */
  public boolean isPositionIdField() {

    return this.positionId != null;
  }

  /**
   * Check if sample Id exists.
   *
   * @return true if sample Id exists
   */
  public boolean isSampleId() {

    return this.sampleId != null;
  }

  /**
   * Test if a type field exists.
   *
   * @return true if a type field exists
   */
  public boolean isTypeField() {

    for (Barcode b : getBarcodes()) {

      if (b.getType() != null) {
        return true;
      }
    }

    return false;
  }

  /**
   * Test if a description field exists.
   *
   * @return true if a description field exists
   */
  public boolean isDescriptionField() {

    for (Barcode b : getBarcodes()) {

      if (b.getDescription() != null) {
        return true;
      }
    }

    return false;
  }

  //
  // Other methods
  //

  /**
   * Validate the the sample sheet.
   *
   * @throws KenetreException if the sample sheet is not valid
   */
  public void validate() throws KenetreException {

    if (this.experimentId == null || this.experimentId.isBlank()) {
      throw new KenetreException("Experiment Id is missing or empty.");
    }

    if (this.flowCellProductCode == null || this.flowCellProductCode.isBlank()) {
      throw new KenetreException("Flow cell product code is missing or empty.");
    }

    if (this.kit == null || this.kit.isBlank()) {
      throw new KenetreException("Kit is missing or empty.");
    }

    if ((this.flowCellId == null || this.flowCellId.isBlank())
        && (this.positionId == null || this.positionId.isBlank())) {
      throw new KenetreException("Flow cell Id or position Id is missing or empty.");
    }

    if (checkDuplicateAliases() != null) {
      throw new KenetreException("Duplicate alias: " + checkDuplicateAliases());
    }
  }

  /**
   * Test if the sample sheet is valid
   *
   * @return true if the sample sheet is valid
   */
  public boolean isValid() {

    return this.experimentId != null
        && !this.experimentId.isBlank()
        && this.flowCellProductCode != null
        && !this.flowCellProductCode.isBlank()
        && this.kit != null
        && !this.kit.isBlank()
        && ((this.flowCellId != null && !this.flowCellId.isBlank())
            || (this.positionId != null && !this.positionId.isBlank()))
        && checkDuplicateAliases() == null;
  }

  static String checkFlowCellProductCode(String flowCellProductCode) {

    requireNonNull(flowCellProductCode);
    if (flowCellProductCode.isBlank()) {
      throw new IllegalArgumentException("flowCellProductCode argument cannot be empty");
    }

    return flowCellProductCode.trim().toUpperCase();
  }

  static String checkSequencingKit(String sequencingKit) {

    requireNonNull(sequencingKit);
    if (sequencingKit.trim().contains(" ")) {
      throw new IllegalArgumentException("Invalid sequencing kit: " + sequencingKit);
    }

    if (!sequencingKit.trim().startsWith("SQK-")) {
      throw new IllegalArgumentException("Invalid kit argument: " + sequencingKit);
    }

    return sequencingKit.trim().toUpperCase();
  }

  static String checkExpansionKit(String expansionKit) {

    requireNonNull(expansionKit);
    if (expansionKit.trim().contains(" ")) {
      throw new IllegalArgumentException("Invalid expansion kit: " + expansionKit);
    }
    if (!expansionKit.startsWith("EXP-")) {
      throw new IllegalArgumentException("Invalid kit argument: " + expansionKit);
    }

    return expansionKit.trim().toUpperCase();
  }

  public String toCSV() {

    StringBuilder sb = new StringBuilder();

    // Header
    if (isProtocolRunIdField()) {
      sb.append("protocol_run_id");
      sb.append(',');
    }

    if (isFlowCellIdField()) {
      sb.append("flow_cell_id");
      sb.append(',');
    }

    if (isPositionIdField()) {
      sb.append("position_id");
      sb.append(',');
    }

    if (isSampleId()) {
      sb.append("sample_id");
      sb.append(',');
    }

    sb.append("experiment_id");
    sb.append(',');
    sb.append("flow_cell_product_code");
    sb.append(',');
    sb.append("kit");

    for (String field : this.otherFields.keySet()) {
      sb.append(',');
      sb.append(field);
    }

    if (isBarcode()) {

      sb.append(',');

      if (isDualBarcoding()) {
        sb.append("internal_barcode");
        sb.append(',');
        sb.append("external_barcode");
      } else {
        sb.append("barcode");
      }

      sb.append(',');
      sb.append("alias");

      if (isTypeField()) {
        sb.append(',');
        sb.append("type");
      }

      if (isDescriptionField()) {
        sb.append(',');
        sb.append("description");
      }
    }

    String commonFields = commonFieldsToCSV();

    if (isBarcode()) {
      boolean isType = isTypeField();
      boolean isDesc = isDescriptionField();

      for (Barcode b : getBarcodes()) {

        sb.append('\n');
        sb.append(commonFields);

        sb.append(',');

        if (isDualBarcoding()) {
          sb.append(b.getInternalBarcode());
          sb.append(',');
          sb.append(b.getExternalBarcode());
        } else {
          sb.append(b.getBarcode());
        }

        sb.append(',');
        sb.append(quoteStringWithComma(b.getAlias()));

        if (isType) {
          sb.append(',');
          BarcodeType t = b.getType();
          if (t != null) {
            sb.append(t.toString().toLowerCase());
          }
        }

        if (isDesc) {
          sb.append(',');
          String d = b.getDescription();
          if (d != null) {
            sb.append(quoteStringWithComma(d.toString()));
          }
        }
      }

    } else {
      sb.append('\n');
      sb.append(commonFields);
    }

    return sb.toString();
  }

  private String commonFieldsToCSV() {

    StringBuilder sb = new StringBuilder();

    if (isProtocolRunIdField()) {
      sb.append(getProtocolRunId());
      sb.append(',');
    }

    if (isFlowCellIdField()) {
      sb.append(getFlowCellId());
      sb.append(',');
    }

    if (isPositionIdField()) {
      sb.append(getPositionId());
      sb.append(',');
    }

    if (isSampleId()) {
      sb.append(quoteStringWithComma(getSampleId()));
      sb.append(',');
    }

    sb.append(quoteStringWithComma(getExperimentId()));
    sb.append(',');
    sb.append(getFlowCellProductCode());
    sb.append(',');
    sb.append(getKit());

    for (String field : this.otherFields.keySet()) {
      sb.append(',');
      sb.append(quoteStringWithComma(this.otherFields.get(field)));
    }

    return sb.toString();
  }

  private static boolean checkBarcodeName(String prefix, String s) {

    requireNonNull(prefix);
    requireNonNull(s);

    s = s.trim();

    if (s.length() != prefix.length() + 2) {
      return false;
    }

    if (!s.startsWith(prefix)) {
      return false;
    }

    try {
      Integer.parseInt(s.substring(prefix.length()));
    } catch (NumberFormatException e) {
      return false;
    }

    return true;
  }

  private static boolean checkAlias(String alias) {

    requireNonNull(alias);

    return alias.matches("^[0-9a-zA-Z-_]+$") && alias.length() <= 40;
  }

  private static String createKey(String barcode) {

    requireNonNull(barcode);

    return barcode.toLowerCase().trim();
  }

  private static String createKey(String internalBarcode, String externalBarcode) {

    requireNonNull(internalBarcode);
    requireNonNull(externalBarcode);

    return internalBarcode.toLowerCase().trim() + '\t' + externalBarcode.toLowerCase().trim();
  }

  private String checkDuplicateAliases() {

    if (!isBarcode()) {
      return null;
    }

    Set<String> aliases = new HashSet<>();

    for (Barcode b : getBarcodes()) {

      if (aliases.contains(b.getAlias())) {
        return b.getAlias();
      }
    }

    return null;
  }

  /**
   * Quote only string containing comma
   *
   * @param String string to process
   */
  private static String quoteStringWithComma(final String s) {

    if (s == null) {
      return "";
    }

    if (s.contains(",")) {
      return "\"" + s + "\"";
    }

    return s;
  }

  //
  // Object methods
  //

  @Override
  public int hashCode() {

    return Objects.hash(
        this.protocolRunId,
        this.flowCellId,
        this.positionId,
        this.sampleId,
        this.experimentId,
        this.flowCellProductCode,
        this.kit,
        this.barcodes);
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) return true;
    if (!(o instanceof SampleSheet)) return false;
    SampleSheet other = (SampleSheet) o;

    return Objects.equals(this.protocolRunId, other.protocolRunId)
        && Objects.equals(this.flowCellId, other.flowCellId)
        && Objects.equals(this.positionId, other.positionId)
        && Objects.equals(this.sampleId, other.sampleId)
        && Objects.equals(this.experimentId, other.experimentId)
        && Objects.equals(this.flowCellProductCode, other.flowCellProductCode)
        && Objects.equals(this.kit, other.kit)
        && Objects.equals(this.barcodes, other.barcodes);
  }

  @Override
  public String toString() {
    return "SampleSheet [protocolRunId="
        + protocolRunId
        + ", flowCellId="
        + flowCellId
        + ", positionId="
        + positionId
        + ", sampleId="
        + sampleId
        + ", experimentId="
        + experimentId
        + ", flowCellProductCode="
        + flowCellProductCode
        + ", kit="
        + kit
        + ", barcodes="
        + barcodes
        + "]";
  }
}
