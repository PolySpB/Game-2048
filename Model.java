import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
    protected int score;
    protected int maxTile;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();
    private boolean isSaveNeeded = true;

    public Model() {
        resetGameTiles();
        score = 0;
        maxTile = 0;
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (emptyTiles.size() != 0) {
            Tile newTile = emptyTiles.get((int) (emptyTiles.size() * Math.random()));
            newTile.value = Math.random() < 0.9 ? 2 : 4;
        }
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> emptyTiles = new ArrayList<>();
        for (int i=0; i < FIELD_WIDTH; i++) {
            for (int j=0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].isEmpty())
                    emptyTiles.add(gameTiles[i][j]);
            }
        }
        return emptyTiles;
    }

    void resetGameTiles() {
        for (int i=0; i < FIELD_WIDTH; i++) {
            for (int j=0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private boolean compressTiles(Tile[] tiles) {
        boolean isChanged = false;
        for (int i=0; i < tiles.length; i++) {
            if (tiles[i].isEmpty()) {
                int j = i;
                while (tiles[j].isEmpty() && j < tiles.length-1) { j++; }
                if(!tiles[j].isEmpty()) isChanged = true;
                tiles[i] = tiles[j];
                tiles[j] = new Tile();
            }
        }
        return isChanged;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean isChanged = false;
        for (int i=1; i < tiles.length; i++) {
            if (!tiles[i-1].isEmpty() && tiles[i-1].value == tiles[i].value) {
                tiles[i-1].value *= 2;
                tiles[i].value = 0;
                if (tiles[i-1].value > maxTile) maxTile = tiles[i-1].value;
                score += tiles[i-1].value;
                compressTiles(tiles);
                isChanged = true;
            }
        }
        return isChanged;
    }

    public void left() {
        if (isSaveNeeded) saveState(gameTiles);
        boolean isChanged = false;
        for (int i=0; i < gameTiles.length; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i])) {
                isChanged = true;
            }
        }
        if (isChanged) addTile();
        isSaveNeeded = true;
    }

    private void rotateClockwise() {
        int[][] newArray = new int[FIELD_WIDTH][FIELD_WIDTH];
        for (int i=0; i < FIELD_WIDTH; i++) {
            for (int j=0; j < FIELD_WIDTH; j++) {
                newArray[i][j] = gameTiles[FIELD_WIDTH - 1 - j][i].value;
            }
        }
        for (int i=0; i < FIELD_WIDTH; i++) {
            for (int j=0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j].value = newArray[i][j];
            }
        }
    }

    public void right() {
        saveState(gameTiles);
        rotateClockwise();
        rotateClockwise();
        left();
        rotateClockwise();
        rotateClockwise();
    }

    public void up() {
        saveState(gameTiles);
        rotateClockwise();
        rotateClockwise();
        rotateClockwise();
        left();
        rotateClockwise();
    }

    public void down() {
        saveState(gameTiles);
        rotateClockwise();
        left();
        rotateClockwise();
        rotateClockwise();
        rotateClockwise();
    }

    public boolean canMove() {
        for (int i=0; i < gameTiles.length; i++) {
            for (int j=0; j < gameTiles[0].length; j++) {
                if (gameTiles[i][j].value == 0)
                    return true;
                if (i != 0 && gameTiles[i-1][j].value == gameTiles[i][j].value)
                    return true;
                if (j != 0 && gameTiles[i][j-1].value == gameTiles[i][j].value)
                    return true;
            }
        }
        return false;
    }

    private void saveState(Tile[][] currentGameTile) {
        Tile[][] tilesToBeSaved = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i=0; i < FIELD_WIDTH; i++) {
            for (int j=0; j < FIELD_WIDTH; j++) {
                tilesToBeSaved[i][j] = new Tile(currentGameTile[i][j].value);
            }
        }
        int tempScore = score;
        previousStates.push(tilesToBeSaved);
        previousScores.push(tempScore);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousStates.isEmpty() && !previousScores.isEmpty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    void randomMove() {
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n) {
            case 0: left(); break;
            case 1: right(); break;
            case 2: up(); break;
            case 3: down(); break;
        }
    }

    boolean hasBoardChanged() {
        int currentGameTilesValue = 0;
        int previousGameTilesValue = 0;
        Tile[][] previousGameTiles = previousStates.peek();

        for (int i=0; i < gameTiles.length; i++) {
            for (int j=0; j < gameTiles[0].length; j++) {
                currentGameTilesValue += gameTiles[i][j].value;
            }
        }

        for (int i=0; i < previousGameTiles.length; i++) {
            for (int j=0; j < previousGameTiles[0].length; j++) {
                previousGameTilesValue += previousGameTiles[i][j].value;
            }
        }

        return currentGameTilesValue - previousGameTilesValue > 0;
    }

    MoveEfficiency getMoveEfficiency(Move move) {
        MoveEfficiency moveEfficiency = null;
        move.move();
        if (hasBoardChanged()) {
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
            rollback();
        }
        else {
            moveEfficiency = new MoveEfficiency(-1, 0, move);
            rollback();
        }
        return moveEfficiency;
    }

    void autoMove() {
        PriorityQueue<MoveEfficiency> priorityQueue =
                new PriorityQueue<>(4, Collections.reverseOrder());
        priorityQueue.offer(getMoveEfficiency(this::left));
        priorityQueue.offer(getMoveEfficiency(this::right));
        priorityQueue.offer(getMoveEfficiency(this::up));
        priorityQueue.offer(getMoveEfficiency(this::down));
        priorityQueue.peek().getMove().move();
    }
}
