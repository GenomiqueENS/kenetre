package fr.ens.biologie.genomique.kenetre.illumina.samplesheet;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SampleSheetChecker {

  private static final int SAMPLE_ID_MAX_LENGTH = 20;

  private int sampleIdMaxLength = SAMPLE_ID_MAX_LENGTH;
  private boolean allowUnderscoreInSampleID;

  //
  // Getters
  //

  /**
   * Get the maximum sample ID length.
   *
   * @return the maximum sample ID length
   */
  public int getSampleIdMaxLength() {
    return this.sampleIdMaxLength;
  }

  /**
   * Test if underscore is allowed in sample ID.
   *
   * @return true if underscore is allowed in sample ID
   */
  public boolean isAllowUnderscoreInSampleID() {
    return allowUnderscoreInSampleID;
  }

  //
  // Setters
  //

  /**
   * Set he maximum sample ID length.
   *
   * @param sampleIdMaxLength the maximum sample ID length
   */
  public void setSampleIdMaxLength(boolean sampleIdMaxLength) {

    this.sampleIdMaxLength = sampleIdMaxLength ? SAMPLE_ID_MAX_LENGTH : -1;
  }

  /**
   * Set if underscore is allowed in sample ID.
   *
   * @param allowUnderscoreInSampleID true if underscore is allowed in sample ID
   */
  public void setAllowUnderscoreInSampleID(boolean allowUnderscoreInSampleID) {
    this.allowUnderscoreInSampleID = allowUnderscoreInSampleID;
  }

  //
  // Check methods
  //

  /**
   * Check a samplesheet.
   *
   * @param samplesheet Bcl2fastq samplesheet object to check
   * @return a list of warnings
   * @throws KenetreException if the samplesheet is not valid
   */
  public List<String> checkSampleSheet(final SampleSheet samplesheet) throws KenetreException {

    return checkSampleSheet(samplesheet, null);
  }

  /**
   * Check a samplesheet.
   *
   * @param samplesheet the samplesheet
   * @param flowCellId the flow cell id
   * @param allowUnderscoreInSampleID allow underscore characters in Sample_ID fields
   * @return the list
   * @throws KenetreException if the samplesheet is not valid
   */
  public List<String> checkSampleSheet(final SampleSheet samplesheet, final String flowCellId)
      throws KenetreException {

    if (samplesheet == null) {
      throw new NullPointerException("The samplesheet object is null");
    }

    TableSection table = SampleSheetUtils.getCheckedDemuxTableSection(samplesheet);

    if (table.size() == 0) {
      throw new KenetreException("No samples found in the samplesheet.");
    }

    // checkSampleSheet(flowCellId);

    final List<String> warnings = new ArrayList<String>();

    final Map<Integer, Set<String>> indexes = new HashMap<Integer, Set<String>>();
    final Set<String> sampleIds = new HashSet<String>();
    final Set<String> sampleNames = new HashSet<String>();
    final Set<Integer> laneWithIndexes = new HashSet<Integer>();
    final Set<Integer> laneWithoutIndexes = new HashSet<Integer>();
    final Map<String, Set<Integer>> sampleInLanes = new HashMap<String, Set<Integer>>();
    final Map<String, String> samplesProjects = new HashMap<String, String>();
    final Map<String, String> samplesIndex = new HashMap<String, String>();

    for (Sample sample : table) {

      // Check Flow cell id
      if (sample.isField("FCID")) {

        final String sampleFCID = sample.get("FCID");

        checkFCID(sampleFCID);

        if (flowCellId != null) {

          // Check if the flow cell id is the flow cell id expected
          if (!flowCellId.trim().toUpperCase().equals(sampleFCID.toUpperCase())) {
            throw new KenetreException(
                "Bad flowcell name found: " + sampleFCID + " (" + flowCellId + " expected).");
          }
        }
      }

      // Check if the sample is null or empty
      checkSampleId(sample.getSampleId(), sampleIds);

      // Check if the sample is null or empty
      if (sample.isSampleNameField()) {
        checkSampleName(sample.getSampleName(), sampleNames, true, warnings);
      }

      // Check sample reference
      if (sample.isSampleRefField()) {
        checkSampleRef(sample.getSampleId(), sample.getSampleRef());
      }

      // Check index
      if (sample.isIndex1Field()) {
        checkIndex(sample.getIndex1());
      }
      if (sample.isIndex2Field()) {
        checkIndex(sample.getIndex2());
      }
      if (!sample.isIndex1Field() && sample.isIndex2Field()) {
        warnings.add("Found a column in the samplesheet for index 2 " + "but not for index 1");
      }

      // Check sample Index
      checkSampleIndex(sample.getSampleId(), sample.getIndex1(), sample.getIndex2(), samplesIndex);

      // Check the description
      if (sample.isDescriptionField()) {
        checkSampleDescription(sample.getSampleId(), sample.getDescription(), true, warnings);
      }

      // Check sample project
      if (sample.isSampleProjectField()) {
        checkSampleProject(sample.getSampleProject());
        checkCharset(sample.getSampleProject());
      }

      final String index;

      if (sample.getIndex1() == null) {
        index = null;
      } else {

        index = sample.getIndex1() + (sample.getIndex2() != null ? '-' + sample.getIndex2() : "");
      }

      final int lane = sample.getLane();

      // Check if mixing lane with index and lanes without index
      if (index == null || "".equals(index.trim())) {

        if (laneWithoutIndexes.contains(lane)) {
          throw new KenetreException(
              "Found two samples without index for the same lane: " + lane + ".");
        }

        if (laneWithIndexes.contains(lane)) {
          throw new KenetreException(
              "Found a lane with indexed and non indexed samples: " + lane + ".");
        }

        laneWithoutIndexes.add(lane);
      } else {

        if (laneWithoutIndexes.contains(lane)) {
          throw new KenetreException(
              "Found a lane with indexed and non indexed samples: " + lane + ".");
        }
        laneWithIndexes.add(lane);
      }

      // check if a lane has not two or more same indexes
      if (indexes.containsKey(lane)) {

        if (indexes.get(lane).contains(index)) {
          throw new KenetreException(
              "Found a lane with two time the same index: " + lane + " (" + index + ").");
        }

      } else {
        indexes.put(lane, new HashSet<String>());
      }

      // Check sample and project
      if (sample.isSampleProjectField()) {
        checkSampleAndProject(
            sample.getSampleId(),
            sample.getSampleProject(),
            sample.getLane(),
            sampleInLanes,
            samplesProjects,
            warnings);
      }

      indexes.get(lane).add(index);
    }

    // Add warnings for samples in several lanes
    checkSampleInLanes(sampleInLanes, warnings);

    // Return unique warnings
    final List<String> result = new ArrayList<String>(new HashSet<String>(warnings));
    Collections.sort(result);

    return result;
  }

  /**
   * Check charset of a string.
   *
   * @param s the string to check
   * @throws KenetreException if an invalid characted is found
   */
  private static void checkCharset(final String s) throws KenetreException {

    if (s == null) {
      return;
    }

    for (int i = 0; i < s.length(); i++) {

      final int c = s.codePointAt(i);

      if (c < ' ' || c >= 127) {
        throw new KenetreException("Found invalid character '" + (char) c + "' in \"" + s + "\".");
      }
    }
  }

  /**
   * Check fcid.
   *
   * @param fcid the fcid
   * @throws KenetreException if the FCID value is invalid
   */
  private static void checkFCID(final String fcid) throws KenetreException {

    if (isNullOrEmpty(fcid)) {
      throw new KenetreException("Flow cell id is null or empty.");
    }

    // Check charset
    checkCharset(fcid);

    for (int i = 0; i < fcid.length(); i++) {

      final char c = fcid.charAt(i);
      if (!(Character.isLetterOrDigit(c))) {
        throw new KenetreException(
            "Invalid flow cell id, only letters or digits are allowed: " + fcid + ".");
      }
    }
  }

  /**
   * Check sample id.
   *
   * @param sampleId the sample id
   * @param sampleIds the sample ids
   * @throws KenetreException if the sample id is invalid
   */
  private void checkSampleId(final String sampleId, final Set<String> sampleIds)
      throws KenetreException {

    // Check if null of empty
    if (isNullOrEmpty(sampleId)) {
      throw new KenetreException("Found a null or empty sample id.");
    }

    // Check charset
    checkCharset(sampleId);

    // Check for forbidden characters
    if (hasForbiddenCharacter(sampleId, this.allowUnderscoreInSampleID)) {
      throw new KenetreException(
          "Invalid sample id, only letters, digits, '-' or '_' characters are allowed: "
              + sampleId
              + ".");
    }

    // Check the length of the Id
    if (this.sampleIdMaxLength > 0 && sampleId.length() > this.sampleIdMaxLength) {
      throw new KenetreException(
          "Invalid sample id, a valid id cannot be longuer than "
              + this.sampleIdMaxLength
              + " characters: "
              + sampleId
              + ".");
    }

    sampleIds.add(sampleId);
  }

  /**
   * Check if a string has a forbidden character in samplesheet
   *
   * @param s the string to test
   * @param allowUnderscoreInSampleID allow underscore characters
   * @return true if a forbidden character is found
   */
  public static boolean hasForbiddenCharacter(String s, boolean allowUnderscoreInSampleID) {

    for (int i = 0; i < s.length(); i++) {

      final char c = s.charAt(i);

      if (!(Character.isLetterOrDigit(c) || (c == '_' && !allowUnderscoreInSampleID) || c == '-')) {

        return true;
      }
    }

    return false;
  }

  /**
   * Check sample name.
   *
   * @param sampleName the sample name
   * @param sampleNames the sample names
   * @throws KenetreException if the sample name is invalid
   */
  private void checkSampleName(
      final String sampleName,
      final Set<String> sampleNames,
      Boolean isBcl2Fastq2,
      final List<String> warnings)
      throws KenetreException {

    if (isNullOrEmpty(sampleName)) {

      if (!isBcl2Fastq2) {
        throw new KenetreException("Sample Name is null or empty.");

      } else {
        warnings.add("Empty or null Sample Name detected.");
      }
    }
    // Check charset
    checkCharset(sampleName);

    // Check for forbidden characters
    if (hasForbiddenCharacter(sampleName, allowUnderscoreInSampleID)) {
      throw new KenetreException(
          "Invalid sample id, only letters, digits, '-' or '_' characters are allowed : "
              + sampleName
              + ".");
    }

    sampleNames.add(sampleName);
  }

  /**
   * Check sample ref.
   *
   * @param sampleId the sample id
   * @param sampleRef the sample ref
   * @throws KenetreException if the sample ref is invalid
   */
  private static void checkSampleRef(final String sampleId, final String sampleRef)
      throws KenetreException {

    // Check if null of empty
    if (isNullOrEmpty(sampleRef)) {
      throw new KenetreException(
          "Found a null or empty sample reference for sample: " + sampleId + ".");
    }

    // Check charset
    checkCharset(sampleRef);

    // Check for forbidden characters
    for (int i = 0; i < sampleRef.length(); i++) {
      final char c = sampleRef.charAt(i);
      if (!(Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '_')) {
        throw new KenetreException(
            "Invalid sample reference, only letters, digits, ' ', '-' or '_' characters are allowed: "
                + sampleRef
                + ".");
      }
    }
  }

  /**
   * Check index.
   *
   * @param index the index
   * @throws KenetreException the index is invalid
   */
  static void checkIndex(final String index) throws KenetreException {

    if (index == null) {
      return;
    }

    for (int i = 0; i < index.length(); i++) {
      switch (index.codePointAt(i)) {
        case 'A':
        case 'a':
        case 'T':
        case 't':
        case 'G':
        case 'g':
        case 'C':
        case 'c':
          break;

        default:
          throw new KenetreException("Invalid index found: " + index + ".");
      }
    }
  }

  /**
   * Check sample description.
   *
   * @param sampleId the sample id
   * @param sampleDescription the sample description
   * @throws KenetreException if the sample description is invalid
   */
  private static void checkSampleDescription(
      final String sampleId,
      final String sampleDescription,
      Boolean isBcl2Fastq2,
      final List<String> warnings)
      throws KenetreException {

    // Check if null of empty
    if (isNullOrEmpty(sampleDescription)) {

      if (!isBcl2Fastq2) {
        throw new KenetreException("Found a null or empty description for sample: " + sampleId);

      } else {
        warnings.add("Found a null or empty description for sample: " + sampleId);
      }
    }

    // Check charset
    checkCharset(sampleDescription);

    // Check for forbidden characters
    for (int i = 0; i < sampleDescription.length(); i++) {
      final char c = sampleDescription.charAt(i);
      if (c == '\'' || c == '\"') {
        throw new KenetreException(
            "Invalid sample description, '"
                + c
                + "' character is not allowed: "
                + sampleDescription
                + ".");
      }
    }
  }

  /**
   * Check sample project.
   *
   * @param sampleProject the sample project
   * @throws KenetreException if the sample project is invalid
   */
  private static void checkSampleProject(final String sampleProject) throws KenetreException {

    // Check if null of empty
    if (isNullOrEmpty(sampleProject)) {
      throw new KenetreException("Found a null or empty sample project name.");
    }

    // Check for forbidden characters
    for (int i = 0; i < sampleProject.length(); i++) {
      final char c = sampleProject.charAt(i);
      if (!(Character.isLetterOrDigit(c) || c == '-' || c == '_')) {
        throw new KenetreException(
            "Invalid sample project, only letters, digits, '-' or '_' characters are allowed: "
                + sampleProject
                + ".");
      }
    }
  }

  /**
   * Check sample and project.
   *
   * @param sampleId the sample id
   * @param projectName the project name
   * @param lane the lane
   * @param sampleInLanes the sample in lanes
   * @param samplesProjects the samples projects
   * @param warnings the warnings
   * @throws KenetreException if the sample and/or project is invalid
   */
  private static void checkSampleAndProject(
      final String sampleId,
      final String projectName,
      final int lane,
      final Map<String, Set<Integer>> sampleInLanes,
      final Map<String, String> samplesProjects,
      final List<String> warnings)
      throws KenetreException {

    // Check if two or more project use the same sample
    if (samplesProjects.containsKey(sampleId)
        && !samplesProjects.get(sampleId).equals(projectName)) {
      throw new KenetreException(
          "The sample \"" + sampleId + "\" is used by two or more projects.");
    }

    samplesProjects.put(sampleId, projectName);

    final Set<Integer> lanes;
    if (!sampleInLanes.containsKey(sampleId)) {
      lanes = new HashSet<Integer>();
      sampleInLanes.put(sampleId, lanes);
    } else {
      lanes = sampleInLanes.get(sampleId);
    }

    if (lanes.contains(lane)) {
      warnings.add(
          "The sample \"" + sampleId + "\" exists two or more times in the lane " + lane + ".");
    }

    lanes.add(lane);
  }

  /**
   * Check sample in lanes.
   *
   * @param sampleInLanes the sample in lanes
   * @param warnings the warnings
   */
  private static void checkSampleInLanes(
      final Map<String, Set<Integer>> sampleInLanes, final List<String> warnings) {

    for (Map.Entry<String, Set<Integer>> e : sampleInLanes.entrySet()) {

      final Set<Integer> lanes = e.getValue();
      if (lanes.size() > 1) {

        final StringBuilder sb = new StringBuilder();
        sb.append("The sample \"");
        sb.append(e.getKey());
        sb.append("\" exists in lanes: ");

        final List<Integer> laneSorted = new ArrayList<Integer>(lanes);
        Collections.sort(laneSorted);

        boolean first = true;
        for (int lane : laneSorted) {

          if (first) {
            first = false;
          } else {
            sb.append(", ");
          }
          sb.append(lane);
        }
        sb.append('.');

        warnings.add(sb.toString());
      }
    }
  }

  /**
   * Check sample index.
   *
   * @param sampleName the sample name
   * @param index1 the first index * @param index2 the second index
   * @param samplesIndex the samples index
   * @throws KenetreException if the sample index is invalid
   */
  private static void checkSampleIndex(
      final String sampleName,
      final String index1,
      final String index2,
      final Map<String, String> samplesIndex)
      throws KenetreException {

    if (index1 == null) {
      return;
    }

    final String key = index1 + (index2 != null ? '-' + index2 : "");

    if (samplesIndex.containsKey(sampleName) && !samplesIndex.get(sampleName).equals(key)) {
      throw new KenetreException(
          "The sample \""
              + sampleName
              + "\" is defined in several lanes but without the same index.");
    }

    samplesIndex.put(sampleName, key);
  }

  //
  // Private utility methods
  //

  /**
   * Test if a string is null or empty.
   *
   * @param s string to test
   * @return true if the input string is null or empty
   */
  private static boolean isNullOrEmpty(final String s) {

    return s == null || s.isEmpty();
  }
}
