package fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.io;

import static java.util.Objects.requireNonNull;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet;
import fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet.Barcode;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class parse a sample sheet.
 *
 * @since 0.20
 * @author Laurent Jourdren
 */
public class SampleSheetParser {

  private static final String PROTOCOL_RUN_ID_FIELDNAME = "protocol_run_id";
  private static final String FLOW_CELL_ID_FIELDNAME = "flow_cell_id";
  private static final String POSITION_ID_FIELDNAME = "position_id";
  private static final String SAMPLE_ID_FIELDNAME = "sample_id";
  private static final String EXPERIMENT_ID_FIELDNAME = "experiment_id";
  private static final String FLOW_CELL_PRODUCT_CODE_FIELDNAME = "flow_cell_product_code";
  private static final String KIT_FIELDNAME = "kit";
  private static final String ALIAS_FIELDNAME = "alias";
  private static final String TYPE_FIELDNAME = "type";
  private static final String BARCODE_FIELDNAME = "barcode";
  private static final String INTERNAL_BARCODE_FIELDNAME = "internal_barcode";
  private static final String EXTERNAL_BARCODE_FIELDNAME = "external_barcode";
  private static final String DESCRIPTION_FIELDNAME = "description";
  private static final String SAMPLE_REF_FIELDNAME = "sample_ref";

  private final SampleSheet samplesheet = new SampleSheet();
  private Map<String, Integer> fieldPositions = new HashMap<>();
  private boolean header = true;
  private boolean allowAnyfields = false;
  private Set<String> allowedFields = new HashSet<>();

  /**
   * Allow to accept any field.
   *
   * @param allow the value to set
   */
  void allowAnyField(boolean allow) {
    this.allowAnyfields = allow;
  }

  /**
   * Allow additional field name.
   *
   * @param fieldName field name to allow
   */
  void addAllowedField(String fieldName) {

    requireNonNull(fieldName);
    this.allowedFields.add(fieldName.trim().toLowerCase());
  }

  /**
   * Allow additional field names.
   *
   * @param fieldName field name to allow
   */
  void addAllowedFields(Collection<String> fieldNames) {

    requireNonNull(fieldNames);
    for (String fieldName : fieldNames) {
      addAllowedField(fieldName);
    }
  }

  /**
   * Get the parsed sample sheet.
   *
   * @return a SampleSheet object
   * @throws KenetreException if sample sheet is not valid
   */
  SampleSheet getSampleSheet() throws KenetreException {

    this.samplesheet.validate();
    return this.samplesheet;
  }

  /**
   * Parse a line of the sample sheet
   *
   * @param fields fields to parse
   * @param lineNumber line number
   * @throws IOException if an error occurs while parsing line
   */
  void parseLine(final List<String> fields, int lineNumber) throws IOException {

    // Parse header
    if (this.header) {
      parseHeader(fields);
      this.header = false;
      return;
    }

    Set<String> barcodeFields =
        new HashSet<>(
            Arrays.asList(
                ALIAS_FIELDNAME,
                BARCODE_FIELDNAME,
                INTERNAL_BARCODE_FIELDNAME,
                EXTERNAL_BARCODE_FIELDNAME,
                TYPE_FIELDNAME,
                DESCRIPTION_FIELDNAME,
                SAMPLE_REF_FIELDNAME));

    for (String field : fieldPositions.keySet()) {

      if (!barcodeFields.contains(field)) {

        int posField = fieldPositions.get(field);
        if (posField >= fields.size()) {
          continue;
        }
        String newValue = fields.get(posField).trim();
        String oldValue = get(this.samplesheet, field);

        if (oldValue != null && !oldValue.equals(newValue)) {
          throw new IOException("The field \"" + field + "\" cannot have multiple values");
        }

        set(this.samplesheet, field, newValue);
      }
    }

    // Handle barcodes
    if (this.fieldPositions.containsKey(ALIAS_FIELDNAME)) {

      String alias = getValue(fields, ALIAS_FIELDNAME, this.fieldPositions, lineNumber);

      Barcode b;

      if (this.fieldPositions.containsKey(BARCODE_FIELDNAME)) {

        String barcode = getValue(fields, BARCODE_FIELDNAME, this.fieldPositions, lineNumber);
        b = this.samplesheet.addBarcode(barcode, alias);

      } else {

        String internalBarcode =
            getValue(fields, INTERNAL_BARCODE_FIELDNAME, this.fieldPositions, lineNumber);
        String externalBarcode =
            getValue(fields, EXTERNAL_BARCODE_FIELDNAME, this.fieldPositions, lineNumber);
        b = this.samplesheet.addBarcode(internalBarcode, externalBarcode, alias);
      }

      if (this.fieldPositions.containsKey(TYPE_FIELDNAME)) {
        String type = getValue(fields, TYPE_FIELDNAME, this.fieldPositions, lineNumber);
        if (type != null && !type.isBlank()) {
          b.setType(type);
        }
      }

      if (this.fieldPositions.containsKey(DESCRIPTION_FIELDNAME)) {
        b.setDescripton(getValue(fields, DESCRIPTION_FIELDNAME, this.fieldPositions, lineNumber));
      }
    }
  }

  /**
   * Parse header.
   *
   * @param fields
   * @param lineNumber line number
   * @throws IOException if an error occurs while header
   */
  private void parseHeader(final List<String> fields) throws IOException {

    int fieldCount = 0;

    for (String field : fields) {

      String trimmedField = field.trim().toLowerCase();

      switch (trimmedField) {
        case PROTOCOL_RUN_ID_FIELDNAME:
        case FLOW_CELL_ID_FIELDNAME:
        case POSITION_ID_FIELDNAME:
        case SAMPLE_ID_FIELDNAME:
        case EXPERIMENT_ID_FIELDNAME:
        case FLOW_CELL_PRODUCT_CODE_FIELDNAME:
        case KIT_FIELDNAME:
        case ALIAS_FIELDNAME:
        case TYPE_FIELDNAME:
        case BARCODE_FIELDNAME:
        case INTERNAL_BARCODE_FIELDNAME:
        case EXTERNAL_BARCODE_FIELDNAME:
        case DESCRIPTION_FIELDNAME:
          break;

        default:
          if (!allowAnyfields && !this.allowedFields.contains(trimmedField)) {
            throw new IOException("Unknown field in sample sheet: " + field);
          }
      }

      if (this.fieldPositions.containsKey(ALIAS_FIELDNAME)) {

        if (this.fieldPositions.containsKey(BARCODE_FIELDNAME)
            && (this.fieldPositions.containsKey(INTERNAL_BARCODE_FIELDNAME)
                || this.fieldPositions.containsKey(EXTERNAL_BARCODE_FIELDNAME))) {
          throw new IOException("A sample sheet cannot handle single and dual barcoding");
        }

        if (!this.fieldPositions.containsKey(INTERNAL_BARCODE_FIELDNAME)
            && this.fieldPositions.containsKey(EXTERNAL_BARCODE_FIELDNAME)) {
          throw new IOException("Internal barcode field is missing in the sample sheet");
        }

        if (this.fieldPositions.containsKey(INTERNAL_BARCODE_FIELDNAME)
            && !this.fieldPositions.containsKey(EXTERNAL_BARCODE_FIELDNAME)) {
          throw new IOException("External barcode field is missing in the sample sheet");
        }

        if (!this.fieldPositions.containsKey(BARCODE_FIELDNAME)
            && !this.fieldPositions.containsKey(INTERNAL_BARCODE_FIELDNAME)
            && !this.fieldPositions.containsKey(EXTERNAL_BARCODE_FIELDNAME)) {
          throw new IOException("Barcode field(s) are missing in the sample sheet");
        }
      }

      this.fieldPositions.put(trimmedField, fieldCount++);
    }
  }

  //
  // Utility methods
  //

  private static String getValue(
      List<String> fields, String fieldName, Map<String, Integer> fieldPositions, int lineCount)
      throws IOException {

    int posField = fieldPositions.get(fieldName);
    if (posField >= fields.size()) {
      throw new IOException("cannot found " + fieldName + " line: " + lineCount);
    }

    return fields.get(posField);
  }

  private static String get(SampleSheet sampleSheet, String fieldName) {

    switch (fieldName) {
      case PROTOCOL_RUN_ID_FIELDNAME:
        return sampleSheet.getProtocolRunId();

      case FLOW_CELL_ID_FIELDNAME:
        return sampleSheet.getFlowCellId();

      case POSITION_ID_FIELDNAME:
        return sampleSheet.getPositionId();

      case SAMPLE_ID_FIELDNAME:
        return sampleSheet.getSampleId();

      case EXPERIMENT_ID_FIELDNAME:
        return sampleSheet.getExperimentId();

      case FLOW_CELL_PRODUCT_CODE_FIELDNAME:
        return sampleSheet.getFlowCellProductCode();

      case KIT_FIELDNAME:
        return sampleSheet.getKit();

      default:
        return sampleSheet.getOtherField(fieldName);
    }
  }

  private static void set(SampleSheet sampleSheet, String fieldName, String value) {

    switch (fieldName) {
      case PROTOCOL_RUN_ID_FIELDNAME:
        sampleSheet.setProtocolRunId(value);
        break;

      case FLOW_CELL_ID_FIELDNAME:
        sampleSheet.setFlowCellId(value);
        break;

      case POSITION_ID_FIELDNAME:
        sampleSheet.setPositionId(value);
        break;

      case SAMPLE_ID_FIELDNAME:
        sampleSheet.setSampleId(value);
        break;

      case EXPERIMENT_ID_FIELDNAME:
        sampleSheet.setExperimentId(value);
        break;

      case FLOW_CELL_PRODUCT_CODE_FIELDNAME:
        sampleSheet.setFlowCellProductCode(value);
        break;

      case KIT_FIELDNAME:
        sampleSheet.setKit(value);
        break;

      default:
        sampleSheet.setOtherField(fieldName, value);
    }
  }
}
