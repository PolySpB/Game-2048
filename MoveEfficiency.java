public class MoveEfficiency implements Comparable<MoveEfficiency> {
    private int numberOfEmptyTiles;
    private int score;
    private Move move;

    public MoveEfficiency(int numberOfEmptyTiles, int score, Move move) {
        this.numberOfEmptyTiles = numberOfEmptyTiles;
        this.score = score;
        this.move = move;
    }

    public Move getMove() {
        return move;
    }

    @Override
    public int compareTo(MoveEfficiency o) {
        // MoveEfficiency obj = (MoveEfficiency) o;
        int result = Integer.compare(this.numberOfEmptyTiles, o.numberOfEmptyTiles);
        if (result == 0) {
            result = Integer.compare(this.score, o.score);
        }
        return result;
    }
}
