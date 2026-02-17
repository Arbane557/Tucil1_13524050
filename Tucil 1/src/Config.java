
import java.util.List;

public class Config {

    public final int n;
    public final char[][] regionCharGrid;
    public final List<Character> regionSymbols;
    public final List<List<Cell>> regions;
    public final int[][] regionMap;

    public Config(int n, char[][] grid, List<Character> symbols, List<List<Cell>> regions, int[][] regionMap) {
        this.n = n;
        this.regionCharGrid = grid;
        this.regionSymbols = symbols;
        this.regions = regions;
        this.regionMap = regionMap;
    }
}
