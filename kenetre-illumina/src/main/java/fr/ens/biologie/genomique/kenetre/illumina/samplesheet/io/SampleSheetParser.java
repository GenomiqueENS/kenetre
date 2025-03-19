package fr.ens.biologie.genomique.kenetre.illumina.samplesheet.io;

import fr.ens.biologie.genomique.kenetre.illumina.samplesheet.SampleSheet;
import java.io.IOException;
import java.util.List;

public interface SampleSheetParser {

  void parseLine(final List<String> fields) throws IOException;

  SampleSheet getSampleSheet();
}
