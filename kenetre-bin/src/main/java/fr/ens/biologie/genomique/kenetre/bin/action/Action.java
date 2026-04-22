package fr.ens.biologie.genomique.kenetre.bin.action;

import fr.ens.biologie.genomique.kenetre.KenetreException;
import java.util.List;
import java.util.Map;

/**
 * This interface define an action.
 *
 * @since 0.28
 * @author Laurent Jourdren
 */
public interface Action {

  /**
   * Get the name of the action.
   *
   * @return the name of the action
   */
  String getName();

  /**
   * Get action description.
   *
   * @return the description description
   */
  String getDescription();

  /**
   * Execute action.
   *
   * @param conf Kenetre configuration
   * @param arguments arguments of the action
   */
  void action(Map<String, String> conf, List<String> arguments) throws KenetreException;

  /**
   * Test if the action must be hidden from the list of available actions.
   *
   * @return true if the action must be hidden
   */
  boolean isHidden();
}
