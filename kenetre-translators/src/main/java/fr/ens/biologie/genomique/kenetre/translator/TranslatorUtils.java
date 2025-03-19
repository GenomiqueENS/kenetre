/*
 *                  Eoulsan development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public License version 2.1 or
 * later and CeCILL-C. This should be distributed with the code.
 * If you do not have a copy, see:
 *
 *      http://www.gnu.org/licenses/lgpl-2.1.txt
 *      http://www.cecill.info/licences/Licence_CeCILL-C_V1-en.txt
 *
 * Copyright for this code is held jointly by the Genomic platform
 * of the Institut de Biologie de l'École normale supérieure and
 * the individual authors. These should be listed in @author doc
 * comments.
 *
 * For more information on the Eoulsan project and its aims,
 * or to join the Eoulsan Google group, visit the home page
 * at:
 *
 *      http://outils.genomique.biologie.ens.fr/eoulsan
 *
 */

package fr.ens.biologie.genomique.kenetre.translator;

import fr.ens.biologie.genomique.kenetre.translator.io.TranslatorOutputFormat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class implements utility methods for translators.
 *
 * @since 2.0
 * @author Laurent Jourdren
 */
public class TranslatorUtils {

  /**
   * Create a file with additional annotation.
   *
   * @param inputFile input file
   * @param fieldToTranslate field to use with translator
   * @param translator translator to use
   * @param outputFormat output format
   * @throws IOException if an error occurs while creating the output file
   */
  public static void addTranslatorFields(
      final File inputFile,
      final int fieldToTranslate,
      final Translator translator,
      final TranslatorOutputFormat outputFormat)
      throws IOException {

    addTranslatorFields(new FileInputStream(inputFile), fieldToTranslate, translator, outputFormat);
  }

  /**
   * Create a file with additional annotation.
   *
   * @param is input stream of the file
   * @param fieldToTranslate field to use with translator
   * @param translator translator to use
   * @param outputFormat output format
   * @throws IOException if an error occurs while creating the output file
   */
  public static void addTranslatorFields(
      final InputStream is,
      final int fieldToTranslate,
      final Translator translator,
      final TranslatorOutputFormat outputFormat)
      throws IOException {

    if (is == null) {
      throw new NullPointerException("InputStream is null");
    }
    if (translator == null) {
      throw new NullPointerException("Translator is null");
    }
    if (outputFormat == null) {
      throw new NullPointerException("OutputFormat is null");
    }

    List<String> translatorFieldnames = translator.getFields();

    final BufferedReader reader =
        new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));
    String line;
    boolean first = true;

    while ((line = reader.readLine()) != null) {

      final List<String> fields = new ArrayList<>(Arrays.asList(line.split("\t")));

      if (first) {

        // Write original file header
        for (String field : fields) {
          outputFormat.addHeaderField(field);
        }

        // Write original file header
        for (String translatorFieldname : translatorFieldnames) {
          outputFormat.addHeaderField(translatorFieldname);
        }

        first = false;
      } else {

        outputFormat.newLine();

        // Write original file data
        for (String field1 : fields) {
          try {
            outputFormat.writeDouble(Double.parseDouble(field1));
          } catch (NumberFormatException e) {

            outputFormat.writeText(field1);
          }
        }

        // Write annotation
        for (final String field : translatorFieldnames) {

          final String valueToTranslate = fields.get(fieldToTranslate);

          final String value;

          if (field == null) {
            value = null;
          } else {
            value = translator.translateField(valueToTranslate, field);
          }

          String link;

          if (value == null || !translator.isLinkInfo(field)) {
            link = null;
          } else {
            link = translator.getLinkInfo(value, field);
          }

          if (value == null) {
            outputFormat.writeEmpty();
          } else {

            if (link == null) {
              outputFormat.writeText(value);
            } else {
              outputFormat.writeLink(value, link);
            }
          }
        }
      }
    }

    reader.close();
    outputFormat.close();
  }

  /**
   * Create a translator that contains one column named "EnsemblID" that duplicate the Id if the the
   * is an EnsemblID
   *
   * @return a new translator
   */
  public static Translator createDuplicatedEnsemblIdTranslator() {

    return new AbstractTranslator() {

      private static final String FIELD_NAME = "EnsemblID";
      private final Pattern pattern = Pattern.compile("ENS[A-Z]+[0-9]{11}");

      @Override
      public String translateField(final String id, final String field) {

        if (id == null || field == null) {
          return null;
        }

        if (FIELD_NAME.equals(field) && pattern.matcher(id).matches()) {
          return id;
        }

        return null;
      }

      @Override
      public List<String> getFields() {

        return Collections.singletonList(FIELD_NAME);
      }
    };
  }

  //
  // Constructor
  //

  private TranslatorUtils() {}
}
