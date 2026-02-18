
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class App extends Application {

    private double boardTargetPx = 520.0;
    private double cellMin = 18.0;
    private double cellMax = 44.0;
    private double gridGap = 1.0;
    private Reader reader = new Reader();
    private Solver solver = new Solver();
    private Config cfg;
    private GridPane board = new GridPane();
    private VBox boardCard = new VBox();
    private Label iterationsLabel = new Label("0");
    private Label timeLabel = new Label("0 ms");
    private Label statusLabel = new Label("");
    private Button faceButton = new Button(":)");
    private ChoiceBox<Solver.Mode> modeChoice = new ChoiceBox<>();
    private double cellSize = 30.0;
    private Thread solverThread = null;
    private String panelStyle = "-fx-background-color: #C0C0C0; -fx-border-color: #7B7B7B #FFF #FFF #7B7B7B; -fx-border-width: 2;";
    private String buttonStyle = "-fx-background-color: #C0C0C0; -fx-border-color: #FFF #7B7B7B #7B7B7B #FFF; -fx-border-width: 2; -fx-font-weight: bold;";

    @Override
    public void start(Stage stage) {
        stage.setTitle("Queensweeper");

        board.setHgap(gridGap);
        board.setVgap(gridGap);
        board.setAlignment(Pos.CENTER);

        boardCard.setStyle(panelStyle);
        boardCard.setPadding(new Insets(8));
        boardCard.setSpacing(8);
        boardCard.getChildren().add(board);

        ScrollPane scroll = new ScrollPane(boardCard);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        Button loadButton = new Button("LOAD");
        Button solveButton = new Button("SOLVE");
        Button saveButton = new Button("SAVE PNG");
        Button saveTxt = new Button("SAVE TXT");

        loadButton.setStyle(buttonStyle);
        solveButton.setStyle(buttonStyle);
        saveButton.setStyle(buttonStyle);
        faceButton.setStyle(buttonStyle);
        saveTxt.setStyle(buttonStyle);

        modeChoice.getItems().addAll(Solver.Mode.PURE, Solver.Mode.CONSTRAINTS);
        modeChoice.setValue(Solver.Mode.CONSTRAINTS);

        HBox top = new HBox(8, loadButton, solveButton, saveButton, saveTxt, modeChoice);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(8));
        top.setStyle(panelStyle);

        Label itText = new Label("Iter:");
        Label msText = new Label("Time:");
        statusLabel.setTextFill(Color.web("#CC0000"));
        statusLabel.setStyle("-fx-font-size: 11;");
        HBox bottom = new HBox(10, faceButton, itText, iterationsLabel, msText, timeLabel, statusLabel);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(8));
        bottom.setStyle(panelStyle);

        VBox root = new VBox(8, top, scroll, bottom);
        root.setPadding(new Insets(8));
        root.setStyle("-fx-background-color: #808080;");

        loadButton.setOnAction(e -> loadFile(stage));
        solveButton.setOnAction(e -> runSolver(loadButton, solveButton, saveButton));
        saveButton.setOnAction(e -> saveSnapshot(stage));
        saveTxt.setOnAction(e -> saveText(stage));
        faceButton.setOnAction(e -> {
            if (solverThread != null && solverThread.isAlive()) {
                solver.cancel();
                solverThread.interrupt();
            }
            if (cfg != null) {
                clearQueens();
                iterationsLabel.setText("0");
                timeLabel.setText("0 ms");
                faceButton.setText(":)");
                loadButton.setDisable(false);
                solveButton.setDisable(false);
                saveButton.setDisable(false);
                saveTxt.setDisable(false);
            }
        });

        Scene scene = new Scene(root, 720, 720);
        stage.setScene(scene);
        stage.show();
    }

    private void loadFile(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Config File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            Config next = reader.readConfig(file.getAbsolutePath());
            cfg = next;
            cellSize = computeCellSize(cfg.n);
            drawBoard();
            clearQueens();
            iterationsLabel.setText("0");
            timeLabel.setText("0 ms");
            faceButton.setText(":)");
            statusLabel.setText("");
        } catch (IllegalArgumentException ex) {
            faceButton.setText("X(");
            statusLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            faceButton.setText("X(");
            statusLabel.setText("Failed to read file");
        }
    }

    private void runSolver(Button loadButton, Button solveButton, Button saveButton) {
        if (cfg == null) {
            faceButton.setText("X(");
            return;
        }

        loadButton.setDisable(true);
        solveButton.setDisable(true);
        saveButton.setDisable(true);
        faceButton.setText(":|");

        clearQueens();
        iterationsLabel.setText("0");
        timeLabel.setText("0 ms");
        statusLabel.setText("");

        Solver.Mode mode = modeChoice.getValue();

        solverThread = new Thread(() -> {
            boolean solved = solver.solve(cfg, mode, (it, cand) -> {
                Platform.runLater(() -> {
                    showQueens(cand);
                    iterationsLabel.setText(Long.toString(it));
                });
            });

            Platform.runLater(() -> {
                if (solved) {
                    showQueens(solver.getPlacedQueens());
                    faceButton.setText(":)");
                    statusLabel.setText("");
                } else {
                    faceButton.setText("X(");
                    statusLabel.setText("No solution found");
                }

                iterationsLabel.setText(Long.toString(solver.getItteration()));
                timeLabel.setText(Long.toString(solver.getTime()) + " ms");

                loadButton.setDisable(false);
                solveButton.setDisable(false);
                saveButton.setDisable(false);
            });
        });

        solverThread.setDaemon(true);
        solverThread.start();
    }

    private void drawBoard() {
        board.getChildren().clear();
        int n = cfg.n;

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                Rectangle tile = new Rectangle(cellSize, cellSize);
                tile.setFill(colorForRegion(cfg.regionMap[r][c]));
                tile.setStroke(Color.web("#7B7B7B"));
                tile.setStrokeWidth(0.6);

                GridPane.setHalignment(tile, HPos.CENTER);
                GridPane.setValignment(tile, VPos.CENTER);
                board.add(tile, c, r);
            }
        }
    }

    private Color colorForRegion(int idx) {
        double hue = (idx * 40) % 360;
        return Color.hsb(hue, 0.35, 0.85);
    }

    private void clearQueens() {
        board.getChildren().removeIf(n -> n instanceof Circle);
    }

    private void showQueens(List<Cell> queens) {
        clearQueens();
        if (cfg == null || queens == null) {
            return;
        }

        for (Cell q : queens) {
            Circle queen = new Circle(cellSize * 0.28);
            queen.setFill(Color.web("#202020"));

            GridPane.setHalignment(queen, HPos.CENTER);
            GridPane.setValignment(queen, VPos.CENTER);
            board.add(queen, q.y, q.x);
        }
    }

    private double computeCellSize(int n) {
        double gaps = Math.max(0, n - 1) * gridGap;
        double candidate = Math.floor((boardTargetPx - gaps) / Math.max(1, n));
        return clamp(candidate, cellMin, cellMax);
    }

    private double clamp(double v, double a, double b) {
        return Math.max(a, Math.min(b, v));
    }

    private void saveSnapshot(Stage stage) {
        if (cfg == null) {
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Board PNG");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        chooser.setInitialFileName("queensweeper.png");

        File out = chooser.showSaveDialog(stage);
        if (out == null) {
            return;
        }

        try {
            WritableImage img = boardCard.snapshot(new SnapshotParameters(), null);
            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", out);
        } catch (Exception ex) {
            faceButton.setText("X(");
        }
    }

    private void saveText(Stage stage) {
        if (cfg == null) {
            return;
        }

        boolean hasQueen[][] = new boolean[cfg.n][cfg.n];
        for (Cell q : solver.getPlacedQueens()) {
            hasQueen[q.x][q.y] = true;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Solution TXT");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
        chooser.setInitialFileName("solution.txt");

        File out = chooser.showSaveDialog(stage);
        if (out == null) {
            return;
        }

        try (java.io.PrintWriter pw = new java.io.PrintWriter(out)) {
            for (int r = 0; r < cfg.n; r++) {
                for (int c = 0; c < cfg.n; c++) {
                    pw.print(hasQueen[r][c] ? '$' : cfg.regionCharGrid[r][c]);
                }
                pw.println();
            }
        } catch (Exception ex) {
            faceButton.setText("X(");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
