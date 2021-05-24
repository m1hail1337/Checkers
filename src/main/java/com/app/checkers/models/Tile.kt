package com.app.checkers.models

class Tile(val x: Int, val y: Int, var piece: Piece? = null) {

    fun hasPiece(): Boolean = piece != null
}