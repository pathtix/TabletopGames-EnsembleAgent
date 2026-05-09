package core.interfaces;

import core.AbstractGameState;
import core.actions.AbstractAction;

import java.util.List;

public interface IActionListBuilder {

    // builds a string listing all available actions with their IDs for injecting into prompt
    String buildActionsText(List<AbstractAction> actions, AbstractGameState gameState);
}
