
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class Solver {

    public enum Mode {
        PURE,
        ROW_CONS
    }

    private List<Cell> placedQueens = new ArrayList<>();
    private long iterationsChecked = 0;
    private long elapsedMs = 0;

    public long uiUpdateRate = 200_000;

    public List<Cell> getPlacedQueens() {
        return new ArrayList<>(placedQueens);
    }

    public long getIterationsChecked() {
        return iterationsChecked;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public boolean solve(Config cfg, Mode mode) {
        return solve(cfg, mode, null);
    }

    public boolean solve(Config cfg, Mode mode, BiConsumer<Long, List<Cell>> onIteration) {
        placedQueens.clear();
        iterationsChecked = 0;
        elapsedMs = 0;

        if (!basicCheck(cfg)) {
            return false;
        }

        long start = System.currentTimeMillis();
        boolean solved;

        if (mode == Mode.ROW_CONS) {
            solved = solveRowCons(cfg.n, cfg.regionMap, onIteration);
        } else {
            solved = solvePure(cfg.n, cfg.regionMap, onIteration);
        }

        elapsedMs = System.currentTimeMillis() - start;
        return solved;
    }

    private boolean basicCheck(Config cfg) {
        if (cfg == null) {
            return false;
        }

        int n = cfg.n;
        if (n <= 0) {
            return false;
        }

        if (cfg.regions == null || cfg.regions.size() != n) {
            return false;
        }

        if (cfg.regionMap == null || cfg.regionMap.length != n) {
            return false;
        }
        for (int r = 0; r < n; r++) {
            if (cfg.regionMap[r] == null || cfg.regionMap[r].length != n) {
                return false;
            }
        }
        return true;
    }

    private boolean solvePure(int n, int[][] regionMap, BiConsumer<Long, List<Cell>> onIteration) {
        int m = n * n;
        int[] comb = new int[n];
        for (int i = 0; i < n; i++) {
            comb[i] = i;
        }

        while (true) {
            iterationsChecked++;

            List<Cell> candidate = new ArrayList<>(n);
            for (int k = 0; k < n; k++) {
                int idx = comb[k];
                candidate.add(new Cell(idx / n, idx % n));
            }

            if (queenCheck(candidate, n, regionMap)) {
                placedQueens = candidate;
                return true;
            }

            if (onIteration != null && iterationsChecked % uiUpdateRate == 0) {
                onIteration.accept(iterationsChecked, candidate);
            }

            int i = n - 1;
            while (i >= 0 && comb[i] == (m - n + i)) {
                i--;
            }
            if (i < 0) {
                break;
            }

            comb[i]++;
            for (int j = i + 1; j < n; j++) {
                comb[j] = comb[j - 1] + 1;
            }
        }

        return false;
    }

private boolean solveRowCons(int n, int[][] regionMap, BiConsumer<Long, List<Cell>> onIteration) {
    int[] colsPick = new int[n];

    for (int i = 0; i < n; i++) {
        colsPick[i] = 0;
    }

    while (true) {

        iterationsChecked++;

        List<Cell> candidate = new ArrayList<>(n);
        for (int r = 0; r < n; r++) {
            candidate.add(new Cell(r, colsPick[r]));
        }

        if (queenCheck(candidate, n, regionMap)) {
            placedQueens = candidate;
            return true;
        }

        if (onIteration != null && iterationsChecked % uiUpdateRate == 0) {
            onIteration.accept(iterationsChecked, candidate);
        }

        int i = n - 1;

        while (i >= 0) {

            colsPick[i]++;

            if (colsPick[i] < n) {
                break;
            }

            colsPick[i] = 0;
            i--;
        }

        if (i < 0) {
            break;
        }
    }

    return false;
}


    private boolean queenCheck(List<Cell> queens, int n, int[][] regionMap) {
        if (queens == null || queens.size() != n) {
            return false;
        }

        boolean[] usedRow = new boolean[n];
        boolean[] usedCol = new boolean[n];
        boolean[] usedRegion = new boolean[n];
        boolean[][] occupied = new boolean[n][n];

        for (Cell q : queens) {
            int r = q.x;
            int c = q.y;

            if (r < 0 || r >= n || c < 0 || c >= n) {
                return false;
            }

            if (usedRow[r]) {
                return false;
            }
            usedRow[r] = true;

            if (usedCol[c]) {
                return false;
            }
            usedCol[c] = true;

            int reg = regionMap[r][c];
            if (reg < 0 || reg >= n) {
                return false;
            }
            if (usedRegion[reg]) {
                return false;
            }
            usedRegion[reg] = true;

            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (dr == 0 && dc == 0) {
                        continue;
                    }
                    int rr = r + dr;
                    int cc = c + dc;
                    if (rr < 0 || rr >= n || cc < 0 || cc >= n) {
                        continue;
                    }
                    if (occupied[rr][cc]) {
                        return false;
                    }
                }
            }

            occupied[r][c] = true;
        }

        return true;
    }
}
