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
  public void action(List<String> arguments) {

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
