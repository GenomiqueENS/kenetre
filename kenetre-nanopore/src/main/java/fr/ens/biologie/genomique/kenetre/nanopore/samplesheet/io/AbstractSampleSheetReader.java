package fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.io;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This abstract class contains common methods for SampleSheetReader classes.
 *
 * @since 0.32
 * @author Laurent Jourdren
 */
public abstract class AbstractSampleSheetReader implements SampleSheetReader {

  private boolean allowAnyfields = false;
  private Set<String> allowedFields = new HashSet<>();

  @Override
  public void allowAnyField(boolean allow) {
    this.allowAnyfields = allow;
  }

  @Override
  public void addAllowedField(String fieldName) {

    requireNonNull(fieldName);
    this.allowedFields.add(fieldName.trim().toLowerCase());
  }

  @Override
  public void addAllowedFields(Collection<String> fieldNames) {

    requireNonNull(fieldNames);
    for (String fieldName : fieldNames) {
      addAllowedField(fieldName);
    }
  }

  @Override
  public void addAllowedFields(String... fieldNames) {

    addAllowedFields(Arrays.asList(fieldNames));
  }

  /**
   * Create a new parser.
   *
   * @return a configurated SampleSheetParser
   */
  protected SampleSheetParser newSampleSheetParser() {

    SampleSheetParser parser = new SampleSheetParser();
    parser.allowAnyField(this.allowAnyfields);
    parser.addAllowedFields(this.allowedFields);

    return parser;
  }
}
