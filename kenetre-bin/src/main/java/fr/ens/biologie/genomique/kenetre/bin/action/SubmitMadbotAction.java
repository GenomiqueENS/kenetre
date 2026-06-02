package fr.ens.biologie.genomique.kenetre.bin.action;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import fr.ens.biologie.genomique.kenetre.db.commands.SubmitMadbotCommand;
import java.util.List;
import java.util.Map;

public class SubmitMadbotAction implements Action {

  @Override
  public String getName() {
    return "submit-madbot";
  }

  @Override
  public String getDescription() {
    return "Submits a project to Madbot";
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public void action(Map<String, String> conf, List<String> arguments) throws KenetreException {

    new SubmitMadbotCommand(this.getName(), conf, arguments).execute();
  }
}
