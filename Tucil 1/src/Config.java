import java.util.List;

public class Config {

    public int n;
    public char[][] regionCharGrid;
    public List<Character> regionSymbols;
    public List<List<Cell>> regions;
    public int[][] regionMap;

    public Config(int n, char[][] grid, List<Character> symbols, List<List<Cell>> regions, int[][] regionMap) {
        this.n = n;
        this.regionCharGrid = grid;
        this.regionSymbols = symbols;
        this.regions = regions;
        this.regionMap = regionMap;
    }
}
