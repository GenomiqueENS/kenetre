package fr.ens.biologie.genomique.kenetre.bin.action;

import java.util.List;

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
   * @param arguments arguments of the action
   */
  void action(List<String> arguments);

  /**
   * Test if the action must be hidden from the list of available actions.
   *
   * @return true if the action must be hidden
   */
  boolean isHidden();
}
