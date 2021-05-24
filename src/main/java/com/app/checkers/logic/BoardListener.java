package com.app.checkers.logic;

import com.app.checkers.models.Piece;
import com.app.checkers.models.Tile;

public interface BoardListener {
    void pieceAdded(Tile tile);

    void highlightTile(Tile tile);
    void highlightClear();

    void queenAdded(Tile tile);

    void onFinish(Piece.Type whoWin);

    void pieceMoved(Tile from, Tile to);

    void pieceRemoved(Tile tile);
}
