
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Reader {

    public Config readConfig(String path) throws IOException {
        List<String> lines = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(path))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }

        if (lines.isEmpty()) {
            return null;
        }

        int n = lines.size();
        int cols = lines.get(0).length();
        if (cols == 0) {
            return null;
        }

        for (String s : lines) {
            if (s.length() != cols) {
                return null;
            }
        }
        if (n != cols) {
            return null;
        }

        char[][] grid = new char[n][n];

        Set<Character> symbolsSet = new HashSet<>();
        for (int r = 0; r < n; r++) {
            String line = lines.get(r);
            for (int c = 0; c < n; c++) {
                char ch = line.charAt(c);
                if (ch < 'A' || ch > 'Z') {
                    return null;
                }
                grid[r][c] = ch;
                symbolsSet.add(ch);
                if (symbolsSet.size() > 26) {
                    return null;
                }
            }
        }

        if (symbolsSet.size() != n) {
            return null;
        }

        List<Character> symbols = new ArrayList<>(n);
        Map<Character, Integer> idxOf = new HashMap<>();
        for (char ch : symbolsSet) {
            symbols.add(ch);
        }
        symbols.sort(Character::compareTo);
        for (int i = 0; i < symbols.size(); i++) {
            idxOf.put(symbols.get(i), i);
        }

        int[][] regionMap = new int[n][n];
        List<List<Cell>> regions = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            regions.add(new ArrayList<>());
        }

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                int idx = idxOf.get(grid[r][c]);
                regionMap[r][c] = idx;
                regions.get(idx).add(new Cell(r, c));
            }
        }

        if (!allRegionsContiguous(grid, symbols, n)) {
            return null;
        }

        return new Config(n, grid, symbols, regions, regionMap);
    }

    private boolean allRegionsContiguous(char[][] grid, List<Character> symbols, int n) {
        for (char sym : symbols) {
            boolean[][] vis = new boolean[n][n];

            int sr = -1, sc = -1, total = 0;
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    if (grid[r][c] == sym) {
                        total++;
                        if (sr == -1) {
                            sr = r;
                            sc = c;
                        }
                    }
                }
            }
            if (total == 0) {
                return false;
            }

            int reached = 0;
            Deque<Cell> dq = new ArrayDeque<>();
            dq.add(new Cell(sr, sc));
            vis[sr][sc] = true;

            int[] dr = {-1, 1, 0, 0};
            int[] dc = {0, 0, -1, 1};

            while (!dq.isEmpty()) {
                Cell cur = dq.removeFirst();
                reached++;

                for (int k = 0; k < 4; k++) {
                    int rr = cur.x + dr[k];
                    int cc = cur.y + dc[k];
                    if (rr < 0 || rr >= n || cc < 0 || cc >= n) {
                        continue;
                    }
                    if (vis[rr][cc]) {
                        continue;
                    }
                    if (grid[rr][cc] != sym) {
                        continue;
                    }
                    vis[rr][cc] = true;
                    dq.addLast(new Cell(rr, cc));
                }
            }

            if (reached != total) {
                return false;
            }
        }
        return true;
    }
}
