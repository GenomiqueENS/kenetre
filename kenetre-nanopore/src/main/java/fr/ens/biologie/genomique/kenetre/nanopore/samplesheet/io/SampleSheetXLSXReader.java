package fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.io;

import com.google.common.math.DoubleMath;
import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.nanopore.samplesheet.SampleSheet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * This class reads a Nanopore sample sheet file in XLSX format.
 *
 * @since 0.20
 * @author Laurent Jourdren
 */
public class SampleSheetXLSXReader extends AbstractSampleSheetReader implements AutoCloseable {

  private final InputStream is;

  @Override
  public SampleSheet read() throws IOException {

    final SampleSheetParser parser = newSampleSheetParser();

    // Create a workbook out of the input stream
    final XSSFWorkbook wb = new XSSFWorkbook(this.is);

    // Get a reference to the worksheet
    final XSSFSheet sheet = wb.getSheetAt(0);

    // When we have a sheet object in hand we can iterator on
    // each sheet's rows and on each row's cells.
    final Iterator<Row> rows = sheet.rowIterator();
    final List<String> fields = new ArrayList<>();

    while (rows.hasNext()) {
      final XSSFRow row = (XSSFRow) rows.next();
      final Iterator<Cell> cells = row.cellIterator();

      while (cells.hasNext()) {
        final XSSFCell cell = (XSSFCell) cells.next();
        while (fields.size() != cell.getColumnIndex()) {
          fields.add("");
        }

        // Convert cell value to String
        fields.add(parseCell(cell));
      }

      // Parse the fields
      if (!isFieldsEmpty(fields)) {
        parser.parseLine(fields, row.getRowNum() + 1);
      }
      fields.clear();
    }

    wb.close();
    close();

    try {
      return parser.getSampleSheet();
    } catch (KenetreException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void close() throws IOException {
    this.is.close();
  }

  /**
   * Parse the content of a cell.
   *
   * @param cell cell to parse
   * @return a String with the cell content
   */
  private static String parseCell(final XSSFCell cell) {

    if (cell.getCellType() == CellType.NUMERIC) {
      final double doubleValue = cell.getNumericCellValue();

      if (DoubleMath.isMathematicalInteger(doubleValue)) {
        return Long.toString((long) doubleValue);
      }
    }

    return cell.toString();
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

  //
  // Constructors
  //

  /**
   * Public constructor.
   *
   * @param is InputStream to use
   */
  public SampleSheetXLSXReader(final InputStream is) {

    if (is == null) {
      throw new NullPointerException("InputStream is null");
    }

    this.is = is;
  }

  /**
   * Public constructor.
   *
   * @param file File to use
   * @throws FileNotFoundException if the file does not exists
   */
  public SampleSheetXLSXReader(final File file) throws FileNotFoundException {

    if (file == null) {
      throw new NullPointerException("File is null");
    }

    if (!file.isFile()) {
      throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
    }

    this.is = new FileInputStream(file);
  }

  /**
   * Public constructor.
   *
   * @param file file to open
   * @throws IOException if an error occurs while openning the file
   */
  public SampleSheetXLSXReader(final Path file) throws IOException {

    if (file == null) {
      throw new NullPointerException("File is null");
    }

    if (!Files.isRegularFile(file)) {
      throw new FileNotFoundException("File not found: " + file);
    }

    this.is = Files.newInputStream(file);
  }

  /**
   * Public constructor.
   *
   * @param filename Filename to use
   * @throws FileNotFoundException if the file does not exists
   */
  public SampleSheetXLSXReader(final String filename) throws FileNotFoundException {

    this(new File(filename));
  }
}
