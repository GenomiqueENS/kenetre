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
