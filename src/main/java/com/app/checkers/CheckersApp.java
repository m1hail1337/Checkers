package com.app.checkers;

import com.app.checkers.logic.BoardListener;
import com.app.checkers.logic.CheckersGame;
import com.app.checkers.models.Piece;
import com.app.checkers.models.Tile;
import com.app.checkers.view.FinishDialog;
import com.app.checkers.view.RulesWindow;
import com.app.checkers.view.StartingDialog;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.*;

public class CheckersApp extends Application implements BoardListener {

    public static final int TILE_SIZE = 100;

    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();
    private Group highlightedTileGroup = new Group();

    private CheckersGame checkers;

    private Pane root;
    private Scene scene;

    private static final double PIECE_RADIUS = TILE_SIZE * 0.3125;

    private void drawPiece(Piece.Type player, int x, int y) {    //Рисует шашку
        Circle thisPiece = new Circle();
        thisPiece.setRadius(PIECE_RADIUS);
        thisPiece.setTranslateX(x * TILE_SIZE + TILE_SIZE / 2.0);
        thisPiece.setTranslateY( y * TILE_SIZE + TILE_SIZE / 2.0);

        thisPiece.setFill(player == Piece.Type.WHITE ? Color.WHITE : Color.SADDLEBROWN);
        thisPiece.setStroke(player == Piece.Type.WHITE ? Color.SADDLEBROWN : Color.WHITE);
        thisPiece.setStrokeWidth(TILE_SIZE * 0.05);
        pieceGroup.getChildren().add(thisPiece);
    }

    private void drawQueen(Piece.Type player, int x, int y) {    //Рисует шашку
        Circle thisPiece = new Circle();
        thisPiece.setRadius(PIECE_RADIUS);
        thisPiece.setTranslateX(x * TILE_SIZE + TILE_SIZE / 2.0);
        thisPiece.setTranslateY( y * TILE_SIZE + TILE_SIZE / 2.0);

        thisPiece.setFill(player == Piece.Type.WHITE ? Color.WHITE : Color.SADDLEBROWN);
        thisPiece.setStroke(Color.GOLD);
        thisPiece.setStrokeWidth(TILE_SIZE * 0.1);
        pieceGroup.getChildren().add(thisPiece);
    }

    private void drawHighlightedTile(int x, int y) {    // рисует выделение клетки поля
        Rectangle tile = (Rectangle) tileGroup.getChildren().stream().filter(predicate -> predicate.getLayoutX() / TILE_SIZE == x && predicate.getLayoutY() / TILE_SIZE == y).findFirst().get();

        tileGroup.getChildren().remove(tile);
        highlightedTileGroup.getChildren().add(tile);

        tile.setFill(Color.PINK);
    }

    private void clearHighlighting() {
        List<Rectangle> tiles = new ArrayList<>();

        for (Node highlighted: highlightedTileGroup.getChildren()) {
            Rectangle tile = ( (Rectangle) highlighted );
            tile.setFill((tile.getX() / TILE_SIZE + tile.getY() / TILE_SIZE) % 2 == 0 ? Color.BLACK : Color.WHITE);
            tiles.add(tile);
        }

        highlightedTileGroup.getChildren().clear();
        tileGroup.getChildren().addAll(tiles);
    }


    private void runGame() {
        tileGroup = new Group();
        pieceGroup = new Group();
        highlightedTileGroup = new Group();

        root.getChildren().addAll(tileGroup, pieceGroup, highlightedTileGroup);
        drawTiles();

        checkers = new CheckersGame(this);
        checkers.setup();

        root.setOnMouseClicked(e -> {
            int x = (int) e.getSceneX() / TILE_SIZE;
            int y = (int) e.getSceneY() / TILE_SIZE;

            checkers.onTileClick(x, y);
        });
    }

    @Override
    public void start(Stage primaryStage) {        //Запуск
        StartingDialog dialog = new StartingDialog();
        Optional<ButtonType> result = dialog.showAndWait();

        root = new Pane();
        scene = new Scene(root);

        if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            RulesWindow rules = new RulesWindow();
            Optional<ButtonType> agree = rules.showAndWait();
            if (agree.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                primaryStage.setTitle("CheckersApp");
                primaryStage.setScene(scene);
                runGame();
                primaryStage.show();
            }
        }
    }


    private void drawTiles() {
        for (int i = 0; i < CheckersGame.HEIGHT; i++) {
            for (int j = 0; j < CheckersGame.WIDTH; j++) {
                Rectangle thisTile = new Rectangle();
                thisTile.setHeight(TILE_SIZE);
                thisTile.setWidth(TILE_SIZE);
                thisTile.setFill((i + j) % 2 == 0 ? Color.WHITE : Color.BLACK);
                thisTile.relocate(i * TILE_SIZE, j * TILE_SIZE);
                tileGroup.getChildren().add(thisTile);
            }
        }

    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void highlightTile(Tile tile) {
        drawHighlightedTile(tile.getX(), tile.getY());
    }

    @Override
    public void pieceAdded(Tile tile) {
        assert tile.getPiece() != null;

        drawPiece(tile.getPiece().getType(), tile.getX(), tile.getY());
    }

    @Override
    public void queenAdded(Tile tile) {
        assert tile.getPiece() != null;

        drawQueen(tile.getPiece().getType(), tile.getX(), tile.getY());
    }

    @Override
    public void highlightClear() {
        clearHighlighting();
    }

    private Circle findPiece(int x, int y) {
        double xCoord = x * TILE_SIZE + TILE_SIZE / 2.0;
        double yCoord = y * TILE_SIZE + TILE_SIZE / 2.0;

        for (Node p: pieceGroup.getChildren()) {
            if (p.getTranslateX() == xCoord && p.getTranslateY() == yCoord)
                return (Circle) p;
        }
        return null;
    }

    @Override
    public void pieceMoved(Tile from, Tile to) {
        Circle piece = findPiece(from.getX(), from.getY());

        piece.setTranslateX(to.getX() * TILE_SIZE + TILE_SIZE / 2.0);
        piece.setTranslateY(to.getY() * TILE_SIZE + TILE_SIZE / 2.0);
    }

    @Override
    public void pieceRemoved(Tile tile) {
        pieceGroup.getChildren().remove(findPiece(tile.getX(), tile.getY()));
    }

    @Override
    public void onFinish(Piece.Type whoWin) {
        Optional<ButtonType> result = new FinishDialog(whoWin).showAndWait();

        if (result.isPresent()) {
            if (result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                runGame();
            }
        }
    }
}
