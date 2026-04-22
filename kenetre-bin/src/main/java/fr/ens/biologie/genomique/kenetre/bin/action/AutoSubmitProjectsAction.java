package fr.ens.biologie.genomique.kenetre.bin.action;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.commands.AutoSubmitProjectsCommand;
import java.util.List;
import java.util.Map;

public class AutoSubmitProjectsAction implements Action {

  @Override
  public String getName() {
    return "auto-submit-projects";
  }

  @Override
  public String getDescription() {
    return "Submits all non-submitted projects to the France Génomique SI";
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public void action(Map<String, String> conf, List<String> arguments) throws KenetreException {

    new AutoSubmitProjectsCommand(getName(), conf).execute(arguments);
  }
}
