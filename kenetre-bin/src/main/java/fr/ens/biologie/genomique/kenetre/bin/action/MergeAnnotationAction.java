package fr.ens.biologie.genomique.kenetre.bin.action;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.bin.MergeAnnotation;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MergeAnnotationAction implements Action {

  @Override
  public String getName() {

    return "merge-annotation";
  }

  @Override
  public String getDescription() {

    return "Merge GTF annotations";
  }

  @Override
  public boolean isHidden() {

    return false;
  }

  @Override
  public void action(Map<String, String> conf, List<String> arguments) {

    if (arguments.size() != 2) {
      System.err.println("Syntax: merge-annotation reference.gtf input.gtf");
      System.exit(1);
    }

    File officialGTF = new File(arguments.get(0));
    File inputGTF = new File(arguments.get(1));
    File outputGTF = new File("/dev/stdout");

    try {
      MergeAnnotation.execute(officialGTF, inputGTF, outputGTF);
    } catch (IOException | KenetreException e) {
      System.err.println("ERROR: " + e.getMessage());
      System.err.println();
      System.err.println("=== Stack trace ===");
      e.printStackTrace();
    }
  }
}
