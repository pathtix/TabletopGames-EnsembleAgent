package games.dotsboxes;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IActionListBuilder;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBLLMActions implements IActionListBuilder {
    private static final Pattern EDGE = Pattern.compile("\\((\\d+),(\\d+)\\)\\s*->\\s*\\((\\d+),(\\d+)\\)");

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
        String oneLine;
        try {
            oneLine = action.getString(gameState).replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            oneLine = action.toString();
        }

        Matcher m = EDGE.matcher(oneLine);
        if (!m.find()) return oneLine;

        int x1 = Integer.parseInt(m.group(1));
        int y1 = Integer.parseInt(m.group(2));
        int x2 = Integer.parseInt(m.group(3));
        int y2 = Integer.parseInt(m.group(4));

        if (y1 == y2) return "H_" + y1 + "_" + Math.min(x1, x2);
        if (x1 == x2) return "V_" + Math.min(y1, y2) + "_" + x1;
        return "E_" + x1 + "_" + y1 + x2 + y2;
    }
}
