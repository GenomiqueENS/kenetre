package fr.ens.biologie.genomique.kenetre.bin.action;

import static java.util.Objects.requireNonNull;

import com.google.common.math.DoubleMath;
import fr.ens.biologie.genomique.kenetre.bin.Main;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * This class define a program that convert an Excel file to TSV files.
 *
 * @since 0.28
 * @author Laurent Jourdren
 */
public class XLSToTSVAction implements Action {

  private static final SimpleDateFormat DATE_FORMAT_ISO_8601 = new SimpleDateFormat("yyyy-MM-dd");

  //
  // Action methods
  //

  @Override
  public String getName() {
    return "xls2tsv";
  }

  @Override
  public String getDescription() {
    return "Convert an Excel file to TSV files";
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public void action(List<String> arguments) {

    boolean keepEmptyLines = false;

    final CommandLineParser parser = new DefaultParser();
    final String[] argsArray = arguments.toArray(new String[0]);

    // Create Options object
    final Options options = new Options();
    options.addOption("k", "keep-empty-lines", false, "keep empty lines");
    options.addOption("h", "help", false, "display this help");

    try {
      // parse the command line arguments
      final CommandLine line = parser.parse(options, argsArray, true);

      // Help option
      if (line.hasOption("keep-empty-lines")) {
        keepEmptyLines = true;
      }

      // Help option
      if (line.hasOption("help") || line.getArgList().size() != 2) {
        help(options);
      }

      convert(
          new File(line.getArgList().get(0)), new File(line.getArgList().get(1)), keepEmptyLines);

    } catch (ParseException e) {
      Main.errorExit(e, "Error while parsing command line arguments: " + e.getMessage());
    }
  }

  private void help(Options options) {

    new HelpFormatter().printHelp(getName() + " input_file output_directory", options);

    System.exit(1);
  }

  //
  // Convert methods
  //

  private static void convert(File inputFile, File outputDirectory, boolean keepEmptyLines) {

    if (!inputFile.isFile()) {
      System.err.println("input file does not exists: " + inputFile);
      System.exit(1);
    }

    if (!inputFile.canRead()) {
      System.err.println("input file cannot be read: " + inputFile);
      System.exit(1);
    }

    if (!outputDirectory.isDirectory()) {
      System.err.println("output directory does not exists: " + outputDirectory);
      System.exit(1);
    }

    try (InputStream in = new FileInputStream(inputFile)) {

      Workbook wb = open(in, inputFile.getName());
      FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

      List<String> sheetNames = getSheetNames(wb);

      for (int i = 0; i < sheetNames.size(); i++) {

        List<String> lines = readTab(wb, evaluator, i, keepEmptyLines);
        Path outputFile = new File(outputDirectory, sheetNames.get(i) + ".tsv").toPath();
        Files.writeString(outputFile, String.join("\n", lines) + '\n');

        // Set the same last modified time as the original file
        Files.setLastModifiedTime(outputFile, Files.getLastModifiedTime(inputFile.toPath()));
      }

      wb.close();

    } catch (IOException e) {
      Main.errorExit(e, "Error occurs while converting Excel file.");
    }
  }

  /**
   * Get the name of the sheets
   *
   * @param wb the workbook object
   * @return a list with the names of the sheets
   */
  private static List<String> getSheetNames(Workbook wb) {

    requireNonNull(wb);

    List<String> result = new ArrayList<String>();
    for (int i = 0; i < wb.getNumberOfSheets(); i++) {
      result.add(wb.getSheetName(i));
    }

    return result;
  }

  /**
   * Parse the content of a cell.
   *
   * @param cell cell to parse
   * @return a String with the cell content
   */
  private static String parseCell(final FormulaEvaluator evaluator, final Cell cell) {

    switch (cell.getCellType()) {
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          return DATE_FORMAT_ISO_8601.format(cell.getDateCellValue());
        }

        final double doubleValue = cell.getNumericCellValue();

        if (DoubleMath.isMathematicalInteger(doubleValue)) {
          return Long.toString((long) doubleValue);
        }

        return cell.toString();

      case FORMULA:
        try {
          CellValue cellValue = evaluator.evaluate(cell);

          if (cellValue.getCellType() == CellType.NUMERIC) {
            final double doubleValue2 = cell.getNumericCellValue();

            if (DoubleMath.isMathematicalInteger(doubleValue2)) {
              return Long.toString((long) doubleValue2);
            }
          }

          return Objects.toString(cellValue.getStringValue(), "");
        } catch (RuntimeException e) {
          return "";
        }

      default:
        return cell.toString();
    }
  }

  /**
   * Test if all the elements of a list are empty.
   *
   * @param list the list to test
   * @return true if all the elements of the list are empty
   */
  private static boolean isFieldsEmpty(final List<String> list) {

    if (list == null) {
      return true;
    }

    for (final String e : list) {
      if (e != null && !"".equals(e.trim())) {
        return false;
      }
    }

    return true;
  }

  /**
   * Get the index of the merged cell in all the merged cells if the given cell is in a merged cell.
   * Otherwise, it will return null.
   *
   * @param sheet The Sheet object
   * @param row The row number of this cell
   * @param column The column number of this cell
   * @return The index of all merged cells, which will be useful for {@link
   *     Sheet#getMergedRegion(int)}
   */
  private static Integer getIndexIfCellIsInMergedCells(Sheet sheet, int row, int column) {

    int numberOfMergedRegions = sheet.getNumMergedRegions();

    for (int i = 0; i < numberOfMergedRegions; i++) {
      CellRangeAddress mergedCell = sheet.getMergedRegion(i);

      if (mergedCell.isInRange(row, column)) {
        return i;
      }
    }

    return null;
  }

  private static Workbook open(InputStream in, String filename) throws IOException {

    if (filename.endsWith(".xls")) {

      // Create a POIFSFileSystem object to read the data
      final POIFSFileSystem fs = new POIFSFileSystem(in);

      // Create a workbook out of the input stream
      return new HSSFWorkbook(fs);
    } else if (filename.endsWith(".xlsx") || filename.endsWith(".xlsm")) {

      // Create a workbook out of the input stream
      return new XSSFWorkbook(in);
    } else {
      throw new IOException("Unknown file extension for file: " + filename);
    }
  }

  private static Cell getCell(Sheet sheet, int rowNum, int columnNum) {

    // Determine if this cell is in a merged cell
    Integer mergedCellIndex = getIndexIfCellIsInMergedCells(sheet, rowNum, columnNum);

    // Get the content of the cell
    if (mergedCellIndex != null) {
      CellRangeAddress cra = sheet.getMergedRegion(mergedCellIndex);
      return sheet.getRow(cra.getFirstRow()).getCell(cra.getFirstColumn());
    }

    return sheet.getRow(rowNum).getCell(columnNum);
  }

  private static List<String> readTab(
      Workbook wb, FormulaEvaluator evaluator, int sheetIndex, boolean keepEmptyLines)
      throws FileNotFoundException, IOException {

    List<String> result = new ArrayList<>();

    // Get the sheet
    final Sheet sheet = wb.getSheetAt(sheetIndex);

    final List<String> fields = new ArrayList<>();

    for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
      Row row = sheet.getRow(rowNum);
      if (row == null) {
        continue;
      }

      for (int columnNum = 0; columnNum <= row.getLastCellNum(); columnNum++) {

        // Get the content of the cell
        Cell cell = getCell(sheet, rowNum, columnNum);

        // Save the cell content
        if (cell != null) {

          fields.add(parseCell(evaluator, cell).replace('\n', ' ').replace('\t', ' '));
        } else {
          fields.add("");
        }
      }

      // Fill the result
      if (keepEmptyLines || !isFieldsEmpty(fields)) {
        result.add(String.join("\t", fields));
      }

      fields.clear();
    }

    return result;
  }
}
