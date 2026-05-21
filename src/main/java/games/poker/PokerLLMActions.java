package games.poker;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IActionListBuilder;

import java.util.List;

public class PokerLLMActions implements IActionListBuilder {
    public String buildActionsText(List<AbstractAction> actions, AbstractGameState gameState) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < actions.size(); i++) {
            if (i > 0) sb.append("\n");
            sb.append(i).append(" ").append(compactAction(actions.get(i), gameState));
        }
        return sb.toString();
    }

    private String compactAction(AbstractAction action, AbstractGameState gameState) {
        try {
            return action.getString(gameState).replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            return action.toString();
        }
    }
}
