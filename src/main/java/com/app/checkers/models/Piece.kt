package com.app.checkers.models

class Piece(val type: Type, var status: Status) {

    enum class Type {
        BLACK, WHITE;

        companion object {
            fun Type.next(): Type = if (this == WHITE) BLACK else WHITE
        }
    }

    enum class Status {
        CHECKER, QUEEN
    }

    fun toQueen() {
        status = Status.QUEEN
    }
}