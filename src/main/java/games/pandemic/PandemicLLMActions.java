package games.pandemic;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.components.Card;
import core.interfaces.IActionListBuilder;
import core.properties.Property;
import core.properties.PropertyString;
import games.pandemic.actions.*;

import java.util.List;

import static core.CoreConstants.nameHash;

public class PandemicLLMActions implements IActionListBuilder {
    @Override
    public String buildActionsText(List<AbstractAction> actions, AbstractGameState gameState) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < actions.size(); i++) {
            if (i > 0) sb.append("\n");
            sb.append(i).append(" ").append(compactAction(actions.get(i), gameState));
        }
        return sb.toString();
    }

    private String compactAction(AbstractAction action, AbstractGameState gameState) {
        if (action instanceof MovePlayerWithCard moveWC) {
            String cardName;
            try {
                Card card = moveWC.getCard(gameState);
                Property np = card.getProperty(nameHash);
                cardName = (np != null) ? ((PropertyString) np).value : card.getComponentName();
            } catch (Exception e) {
                return moveWC.toString();
            }
            return moveWC.getMoveType() + " to " + moveWC.getDestination() + " with discarding " + cardName;
        }

        if (action instanceof MovePlayer move) {
            return move.getMoveType() + " to " + move.getDestination();
        }

        if (action instanceof ShareKnowledge shareKnowledge) {
            String cardName;
            try {
                Card card = shareKnowledge.getCard(gameState);
                Property np = card.getProperty(nameHash);
                cardName = (np != null) ? ((PropertyString) np).value : card.getComponentName();
            } catch (Exception e) {
                return shareKnowledge.toString();
            }

            return "P" + shareKnowledge.getGiver() + " gives " + cardName + " to P" + shareKnowledge.getReceiver();
        }

        // some ambigious/hard to understand research station adding actions
        if (action instanceof AddResearchStationWithCardFrom addRSWCF) {
            String cardName;
            try {
                Card card = addRSWCF.getCard(gameState);
                Property np = card.getProperty(nameHash);
                cardName = (np != null) ? ((PropertyString) np).value : card.getComponentName();
                return addRSWCF.toString().replaceAll("with card", "discarding " + cardName);
            } catch (Exception e) {
                return addRSWCF.toString();
            }
        }

        if (action instanceof AddResearchStationWithCard addRSWC) {
            String cardName;
            try {
                Card card = addRSWC.getCard(gameState);
                Property np = card.getProperty(nameHash);
                cardName = (np != null) ? ((PropertyString) np).value : card.getComponentName();
                return addRSWC.toString().replaceAll("with card", "discarding " + cardName);
            } catch (Exception e) {
                return addRSWC.toString();
            }
        }

        return action.toString();
    }
}
