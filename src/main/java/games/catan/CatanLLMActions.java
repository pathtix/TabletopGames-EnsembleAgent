package games.catan;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IActionListBuilder;
import games.catan.actions.robber.MoveRobberAndSteal;
import games.catan.components.Building;
import games.catan.actions.setup.PlaceSettlementWithRoad;
import games.catan.components.CatanTile;
import games.puertorico.actions.Build;

import java.util.*;

public class CatanLLMActions implements IActionListBuilder {
    private static final boolean SHOW_TOTALS = false;
    private static final boolean SHOW_PIPS = false;

    @Override
    public String buildActionsText(List<AbstractAction> actions, AbstractGameState gameState) {
        Set<String> seenTileSets = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (int i = 0; i < actions.size(); i++) {
            AbstractAction action = actions.get(i);

            if (action instanceof PlaceSettlementWithRoad pswr) {
                String signiture = tileSignature(pswr, gameState);
                if (!seenTileSets.add(signiture)) continue;
            }

            if (!first) sb.append("\n");
            sb.append(i).append(" ").append(compactAction(action, gameState));
            first = false;
        }
        return sb.toString();
    }

    private String compactAction(AbstractAction action, AbstractGameState gameState) {
        if (action instanceof PlaceSettlementWithRoad pswr) {
            return formatSettlement(getTouchingTiles(pswr, gameState), gameState);
        }

        if (action instanceof MoveRobberAndSteal robber) {
            return formatRobber(robber, gameState);
        }

        try {
            return action.getString(gameState).replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            return action.toString();
        }
    }

    private String tileSignature(PlaceSettlementWithRoad pswr, AbstractGameState gameState) {
        List<String> tileKeys = new ArrayList<>();
        for (ProductiveTile t : productiveTiles(getTouchingTiles(pswr, gameState), gameState)) {
            tileKeys.add(t.resource().name() + " " + t.number());
        }

        Collections.sort(tileKeys);
        return String.join(",", tileKeys);
    }

    private String formatSettlement(List<CatanTile> touching, AbstractGameState gameState) {
        int totalPips = 0;
        Set<String> resources = new LinkedHashSet<>();
        List<String> parts = new ArrayList<>();

        // combine resoirces into one string -> [GRAIN 5(4pip), WOOL 9(4pip)]
        for (ProductiveTile t : productiveTiles(touching, gameState)) {
            totalPips += t.pips();
            resources.add(t.resource().name());
            parts.add(SHOW_PIPS
                ? t.resource() + " " + t.number() + "(" + t.pips() + "pip)"
                : t.resource() + " " + t.number());
        }

        if (parts.isEmpty()) return "SEA/DESERT only";
        String tiles = "[" + String.join(", ", parts) + "]";
        return SHOW_TOTALS ? String.format("%s -> %d pips, %d resources", tiles, totalPips, resources.size()) : tiles;
        // return String.format("[%s] -> %d pips, %d resources", String.join(", ", parts), totalPips, resources.size());
    }

    private String formatRobber(MoveRobberAndSteal robber, AbstractGameState gameState) {
        CatanGameState cgs = (CatanGameState) gameState;
        CatanParameters params = (CatanParameters) cgs.getGameParameters();
        CatanTile tile = cgs.getBoard()[robber.x][robber.y];

        String tileString;
        CatanParameters.Resource resource = params.productMapping.get(tile.getTileType());

        // tile information
        if (resource == null || tile.getNumber() == 0) {
            tileString = "desert/sea (no production)";
        } else {
            tileString = SHOW_PIPS ? resource + " " + tile.getNumber() + " (" + (6 - Math.abs(tile.getNumber() - 7)) + "pip)"
            : resource + " " + tile.getNumber();
        }

        // settlement, ownership information
        List<String> blocked = new ArrayList<>();
        for (Building b : cgs.getBuildings(tile)) {
            if (b.getOwnerId() == -1) continue;

            blocked.add(b.getOwnerId() == robber.player ? "you" : "p" + b.getOwnerId());
        }

        String blocking = blocked.isEmpty() ? "blocks 0 player" : "blocks " + String.join(",", blocked);

        // stealing information
        String stealing = robber.targetPlayer < 0 ? "steal: none"
                      : "steal p" + robber.targetPlayer + " (" + cgs.getNResourcesInHand(robber.targetPlayer) + " cards)";


        return "move robber to " + tileString + " | " + blocking + " | " + stealing;
    }

    private List<CatanTile> getTouchingTiles(PlaceSettlementWithRoad pswr, AbstractGameState gameState) {
        CatanGameState cgs = (CatanGameState) gameState;
        CatanTile[][] board = cgs.getBoard();

        List<CatanTile> touching = new ArrayList<>();
        touching.add(board[pswr.x][pswr.y]);
        for (int[] n : board[pswr.x][pswr.y].getNeighboursOnVertex(pswr.vertex)) {
            if (n[0] >= 0 && n[0] < board.length
                    && n[1] >= 0 && n[1] < board[0].length)
                touching.add(board[n[0]][n[1]]);
        }
        return touching;
    }

    private List<ProductiveTile> productiveTiles(List<CatanTile> touching, AbstractGameState gameState) {
        CatanParameters params = (CatanParameters) gameState.getGameParameters();

        // calculate up to 3 tiles around a vertex, its resources and pips of each tile
        List<ProductiveTile> out = new ArrayList<>();
        for (CatanTile tile : touching) {
            if (tile.getTileType() == CatanTile.TileType.SEA || tile.getTileType() == CatanTile.TileType.DESERT || tile.getNumber() == 0)
                continue;
            CatanParameters.Resource r = params.productMapping.get(tile.getTileType());
            if (r == null) continue;
            out.add(new ProductiveTile(r, tile.getNumber()));
        }
        return out;
    }

    private record ProductiveTile(
            CatanParameters.Resource resource,
            int number
    )  {
        int pips() { return 6 - Math.abs(number - 7); }
    }
}
