package evaluation.listeners;

import core.AbstractPlayer;
import core.Game;
import evaluation.metrics.Event;
import players.IAnyTimePlayer;
import players.llm.phase.LLMPhaseConfig;
import players.llm.phase.LLMPhaseDetector;
import java.util.HashMap;
import java.util.Map;

public class LLMPhaseBudgetListener implements IGameListener {
    private final LLMPhaseConfig phaseConfig;
    private final Map<Integer, Integer> originalBudgets = new HashMap<>();
    private Game game;
    private boolean wasInLLMPhase = false;

    public LLMPhaseBudgetListener(LLMPhaseConfig phaseConfig) {
        this.phaseConfig = phaseConfig;
    }

    @Override
    public void onEvent(Event event) {
        if (!(event.type instanceof Event.GameEvent gameEvent)) return;

        switch (gameEvent) {
            case ABOUT_TO_START -> handleStart(event);
            case ACTION_TAKEN -> checkAndApply(event);
            case GAME_OVER -> restoreOriginals();
            default -> {}
        }
    }

    private void handleStart(Event event) {
        originalBudgets.clear();
        for (AbstractPlayer player : game.getPlayers()) {
            if (player instanceof IAnyTimePlayer atp) {
                originalBudgets.put(player.getPlayerID(), atp.getBudget());
            }
        }
        wasInLLMPhase = false;
        checkAndApply(event);
    }

    private void checkAndApply(Event event) {
        boolean isInLLMPhase = LLMPhaseDetector.isLLMPhase(event.state, phaseConfig);
        if (isInLLMPhase == wasInLLMPhase) return; // no transition

        int matchedBudget = isInLLMPhase ? phaseConfig.matchedBudgetMs : 0;
        for (AbstractPlayer player : game.getPlayers()) {
            if (!(player instanceof IAnyTimePlayer atp)) continue;
            int newBudget = isInLLMPhase ? matchedBudget : originalBudgets.get(player.getPlayerID());
            atp.setBudget(newBudget);
        }
        wasInLLMPhase = isInLLMPhase;
    }

    private void restoreOriginals() {
        for (AbstractPlayer player : game.getPlayers()) {
            if (player instanceof IAnyTimePlayer atp) {
                atp.setBudget(originalBudgets.get(player.getPlayerID()));
            }
        }
        wasInLLMPhase = false;
    }

    @Override
    public void report() { }

    @Override
    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public Game getGame() {
        return game;
    }
}
