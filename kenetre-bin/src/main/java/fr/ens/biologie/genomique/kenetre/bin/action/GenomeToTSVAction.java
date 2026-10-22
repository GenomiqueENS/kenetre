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

package fr.ens.biologie.genomique.kenetre.bin.action;

import fr.ens.biologie.genomique.kenetre.bin.Main;
import fr.ens.biologie.genomique.kenetre.bio.BadBioEntryException;
import fr.ens.biologie.genomique.kenetre.bio.GenomeDescription;
import fr.ens.biologie.genomique.kenetre.io.CompressionType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * This program allow to create UCSC TSV chromosome files.
 *
 * @author Laurent Jourdren
 * @since 33
 */
public class GenomeToTSVAction implements Action {

  @Override
  public String getName() {

    return "genome2tsv";
  }

  @Override
  public String getDescription() {
    return "Convert a Genome in FASTA format to UCSC TSV file";
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public void action(Map<String, String> conf, List<String> arguments) {

    if (arguments.size() != 2) {
      System.err.println(getName() + " genome.fasta chromosomes.tsv");
    }

    Path inputGenomeFile = Paths.get(arguments.get(0));
    Path outputTSVFile = Paths.get(arguments.get(1));

    if (!Files.isRegularFile(inputGenomeFile)) {
      System.err.println("input FASTA file does not exists: " + inputGenomeFile);
      System.exit(1);
    }

    if (Files.isRegularFile(outputTSVFile)) {
      System.err.println("output TSV file already exists exists: " + outputTSVFile);
      System.exit(1);
    }

    try {
      GenomeDescription desc =
          GenomeDescription.createGenomeDescFromFasta(
              CompressionType.open(inputGenomeFile), inputGenomeFile.toFile().getName());

      desc.saveTSV(outputTSVFile.toFile());

    } catch (BadBioEntryException | IOException e) {
      Main.errorExit(e, "Error occurs while processing files.");
    }
  }
}
