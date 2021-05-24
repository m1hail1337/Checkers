package com.app.checkers.logic

import com.app.checkers.models.Piece
import com.app.checkers.models.Piece.Type.Companion.next
import com.app.checkers.models.Tile


class CheckersGame(private var listener: BoardListener) {

    private var accentTile: Tile? = null
    private var possiblePairsForMove: List<Map<Tile?, Tile?>>? = null
    private var eatingTile: Tile? = null

    companion object {
        const val WIDTH = 8
        const val HEIGHT = 8
        const val INITIAL_CHECKERS_LINES = 3
    }

    private val board = Array(HEIGHT) { y ->
        Array(WIDTH) { x ->
            Tile(x, y, null)
        }
    }

    var currentMove = Piece.Type.WHITE
        private set

    private var clickCount = 0

    private fun getTile(y: Int, x: Int): Tile? {
        val tile = Tile(x, y, null)
        return if (tile.exists()) board[y][x] else null
    }

    private fun Tile.exists(): Boolean =
        x in 0 until WIDTH && y in 0 until HEIGHT  //Проверка на существование клетки


    fun setup() {
        for (i in 0 until INITIAL_CHECKERS_LINES) {
            val offsetBottom = if (i % 2 == 0) 0 else 1
            val offsetTop = if ((HEIGHT - i - 1) % 2 == 0) 0 else 1
            for (j in 0 until WIDTH step 2) {
                board[i][j + offsetTop].piece = Piece(Piece.Type.BLACK, Piece.Status.CHECKER)
                board[HEIGHT - i - 1][j + offsetBottom].piece = Piece(
                    Piece.Type.WHITE,
                    Piece.Status.CHECKER
                )

                listener.pieceAdded(board[i][j + offsetTop])
                listener.pieceAdded(board[HEIGHT - i - 1][j + offsetBottom])
            }
        }
    }


    private fun correctColorEat(currentMove: Piece.Type, tile: Tile): Boolean {
        return tile.piece!!.type == Piece.Type.BLACK && currentMove == Piece.Type.BLACK || currentMove == Piece.Type.WHITE && tile.piece!!.type == Piece.Type.WHITE
    }

    //Можно ли так ходить данному цвету
    private fun correctColorMove(currentMove: Piece.Type, tile: Tile, value: Int): Boolean {
        return tile.piece!!.type == Piece.Type.BLACK && value == 1 && currentMove == Piece.Type.BLACK ||
                tile.piece!!.type == Piece.Type.WHITE && value == -1 && currentMove == Piece.Type.WHITE
    }

    // есть ли возможность стать дамкой
    private fun becomingQueen(tile: Tile): Boolean {
        if (tile.hasPiece()) if (tile.piece!!.type == Piece.Type.BLACK && tile.y == 7 || tile.piece!!.type == Piece.Type.WHITE && tile.y == 0) {
            tile.piece!!.toQueen()
            return true
        }
        return false
    }

    // Начальные координаты в стороны
    private fun initCoordinates(): Map<Int, Pair<Int, Int>> {
        val coordinates = mutableMapOf<Int, Pair<Int, Int>>()
        coordinates[0] = -1 to -1
        coordinates[1] = -1 to 1
        coordinates[2] = 1 to -1
        coordinates[3] = 1 to 1
        return coordinates
    }

    fun requiredMoves(): List<Map<Tile?, Tile?>> {         //Список из обязательных ходов
        val coordinates = initCoordinates()

        var coefficient = 1
        val requiredMoves: MutableList<Map<Tile?, Tile?>> = ArrayList()
        for (i in 0 until HEIGHT) {
            for (j in 0 until WIDTH) {
                val tile: Tile = board[i][j]
                if (tile.hasPiece())
                    if (correctColorEat(currentMove, tile))
                        if (tile.piece!!.status != Piece.Status.QUEEN) {
                    for ((_, value) in coordinates) {
                        val potentialTile: Tile? =
                            getTile(tile.y + 2 * value.second, tile.x + 2 * value.first)
                        val tileBetweenPotentialTileAndTile: Tile? =
                            getTile(tile.y + value.second, tile.x + value.first)
                        potentialTile?.let {
                            if (tileBetweenPotentialTileAndTile!!.hasPiece() && tileBetweenPotentialTileAndTile.piece!!.type != tile.piece!!.type && !potentialTile.hasPiece()) {
                                val map: MutableMap<Tile?, Tile?> = HashMap()
                                map[potentialTile] = tile
                                requiredMoves.add(map)
                            }

                        }
                    }
                } else {
                    for ((_, value) in coordinates) {
                        while (getTile(
                                tile.y + (coefficient + 1) * value.second,
                                tile.x + (coefficient + 1) * value.first
                            ) != null
                        ) {
                            var potentialTile: Tile? = getTile(
                                tile.y + coefficient * value.second,
                                tile.x + coefficient * value.first
                            )
                            if (potentialTile!!.hasPiece()) {
                                if (potentialTile.piece!!.type != tile.piece!!.type) {
                                    while (getTile(
                                            tile.y + (coefficient + 1) * value.second,
                                            tile.x + (coefficient + 1) * value.first
                                        ) != null
                                    ) {
                                        potentialTile = getTile(
                                            tile.y + (coefficient + 1) * value.second,
                                            tile.x + (coefficient + 1) * value.first
                                        )
                                        if (!potentialTile!!.hasPiece()) {
                                            val map: MutableMap<Tile?, Tile?> = HashMap()
                                            map[potentialTile] = tile
                                            requiredMoves.add(map)
                                            coefficient++
                                        } else break
                                    }
                                }
                                break
                            } else coefficient++
                        }
                        coefficient = 1
                    }
                }
            }
        }
        return requiredMoves
    }

    // есть ли позиции для перемещения
    private fun possibleMoves(tile: Tile, currentMove: Piece.Type): List<Map<Tile?, Tile?>> {
        val possibleMoves: MutableList<Map<Tile?, Tile?>> = ArrayList()
        val coordinates = initCoordinates()
        var coefficient = 1
        if (tile.piece!!.status != Piece.Status.QUEEN) {
            for ((_, value) in coordinates) {
                val potentialTile = getTile(tile.y + value.second, tile.x + value.first)
                if (potentialTile != null)
                    if (!potentialTile.hasPiece())
                        if (correctColorMove(currentMove, tile, value.second)) {
                    val map: MutableMap<Tile?, Tile?> = HashMap()
                    map[potentialTile] = null
                    possibleMoves.add(map)
                }
            }
        } else {
            for ((_, value) in coordinates) {
                while (getTile(tile.y + coefficient * value.second, tile.x + coefficient * value.first) != null) {
                    val potential = getTile(tile.y + coefficient * value.second, tile.x + coefficient * value.first)
                    if (!potential!!.hasPiece()) {
                        if (correctColorEat(currentMove, tile)) {
                            val map: MutableMap<Tile?, Tile?> = HashMap()
                            map[potential] = null
                            possibleMoves.add(map)
                        }
                        coefficient++
                    } else break
                }
                coefficient = 1
            }
        }
        return possibleMoves
    }

    private fun canKillMore(tile: Tile): Boolean {
        val coordinates = initCoordinates()


        var coefficient = 1
        if (tile.piece!!.status != Piece.Status.QUEEN) {
            for ((_, value) in coordinates) {
                val potentialTile: Tile? = getTile(tile.y + 2 * value.second, tile.x + 2 * value.first)
                val tileBetweenPotentialTileAndTile: Tile? =
                    getTile(tile.y + value.second, tile.x + value.first)
                if (potentialTile != null) if (tileBetweenPotentialTileAndTile!!.hasPiece())
                    if (tileBetweenPotentialTileAndTile.piece!!.type != tile.piece!!.type) if (!potentialTile.hasPiece()) return true
            }
        } else {
            for ((_, value) in coordinates) {
                while (getTile(
                        tile.y + (coefficient + 1) * value.second,
                        tile.x + (coefficient + 1) * value.first
                    ) != null
                ) {
                    val tileBetweenPotentialTileAndTile: Tile? =
                        getTile(tile.y + coefficient * value.second, tile.x + coefficient * value.first)
                    val potentialTile: Tile? = getTile(
                        tile.y + (coefficient + 1) * value.second,
                        tile.x + (coefficient + 1) * value.first
                    )
                    if (tileBetweenPotentialTileAndTile!!.hasPiece()) {
                        if (tileBetweenPotentialTileAndTile.piece!!.type != tile.piece!!.type) {
                            return if (!potentialTile!!.hasPiece()) {
                                true
                            } else break
                        }
                        break
                    } else coefficient++
                }
                coefficient = 1
            }
        }
        return false
    }

    // существуют ли ходы для данного игрока
    private fun checkForMove(): Boolean {
        for (i in 0 until HEIGHT) for (j in 0 until WIDTH) {
            val tile = board[i][j]
            if (tile.hasPiece() && possibleMoves(tile, currentMove).isNotEmpty()) return false
        }

        return true
    }


    private fun finishGame(): Boolean {   // Условия для окончания игры для данного игрока
        if (requiredMoves().isEmpty() && checkForMove()) return true
        for (i in 0 until HEIGHT) for (j in 0 until WIDTH) {
            val tile = board[i][j]
            if (tile.hasPiece()) {
                if (tile.piece!!.type == Piece.Type.WHITE) return false
                if (tile.piece!!.type == Piece.Type.BLACK) return false
            }
        }
        return true
    }


    //Находит клетку, шашка на которой будет удалена
    private fun deletedChecker(pick: Tile, variant: Tile): Tile {
        var coefficient = 1
        var posX = -1
        var posY = -1
        if (pick.y > variant.y) {
            if (pick.x > variant.x) {
                for (i in pick.y - 1 downTo variant.y + 1) {
                    if (board[i][pick.x - coefficient].hasPiece()) {
                        posX = pick.x - coefficient
                        posY = i
                        break
                    }
                    coefficient++
                }
            } else for (i in pick.y - 1 downTo variant.y + 1) {
                if (board[i][pick.x + coefficient].hasPiece()) {
                    posX = pick.x + coefficient
                    posY = i
                    break
                }
                coefficient++
            }
        }
        if (pick.y < variant.y) {
            if (pick.x > variant.x) {
                for (i in pick.y + 1 until variant.y) {
                    if (board[i][pick.x - coefficient].hasPiece()) {
                        posX = pick.x - coefficient
                        posY = i
                        break
                    }
                    coefficient++
                }
            } else for (i in pick.y + 1 until variant.y) {
                if (board[i][pick.x + coefficient].hasPiece()) {
                    posX = pick.x + coefficient
                    posY = i
                    break
                }
                coefficient++
            }
        }
        return board[posY][posX]
    }

    //Подсвечивает ходы только твоих шашек
    private fun canHighlight(currentMove: Piece.Type, tile: Tile): Boolean {
        return if (currentMove == Piece.Type.BLACK) tile.piece!!.type == Piece.Type.BLACK else tile.piece!!.type == Piece.Type.WHITE
    }

    //Не позволяет перемещаться шашкой на ту клетку, которой нет в списке возможных вариантов
    private fun canMove(pairs: List<Map<Tile?, Tile?>>?, tile: Tile?): Boolean {
        for (element in pairs!!) {
            if (element.containsKey(tile)) return true
        }
        return false
    }

    //Не позволяет перемещаться шашкой на ту клетку, которой нет в списке возможных вариантов (для съедения)
    private fun canEatThis(pairs: List<Map<Tile?, Tile?>>?, tile: Tile): Boolean {
        for (element in pairs!!) {
            for ((key, value) in element) if (value == accentTile && key == tile) return true
        }
        return false
    }

    //Процесс перемещения
    private fun movePiece(selectedTile: Tile, variant: Tile) {
        variant.piece = selectedTile.piece
        selectedTile.piece = null
        listener.pieceMoved(selectedTile, variant)
        if (becomingQueen(variant)) {
            listener.pieceRemoved(variant)
            listener.queenAdded(variant)
        }
        clickCount = 0
    }

    // съедание шашки
    private fun kill(selectedTile: Tile, variant: Tile) {
        val deletedTile = deletedChecker(selectedTile, variant)
        movePiece(selectedTile, variant)
        listener.pieceRemoved(deletedTile)
        deletedTile.piece = null

        if (!canKillMore(variant)) {
            eatingTile = null
            currentMove = currentMove.next()
        } else eatingTile = variant
        clickCount = 0
    }

    private fun filterForRequiredMoves(pair: List<Map<Tile?, Tile?>>, tile: Tile): List<Map<Tile?, Tile?>> {
        val moves: MutableList<Map<Tile?, Tile?>> = ArrayList()
        for (i in pair.indices) for ((key, value) in pair[i]) if (eatingTile != null) {
            if (tile == eatingTile) if (tile == value) {
                val map = mutableMapOf<Tile?, Tile?>()
                map[key] = value
                moves.add(map)
            }
        } else {
            if (tile == value) {
                val map = mutableMapOf<Tile?, Tile?>()
                map[key] = value
                moves.add(map)
            }
        }
        return moves
    }


    // клик на поле
    fun onTileClick(x: Int, y: Int) {
        if (clickCount == 1) {
            val tileForSecondClick = board[y][x]
            //позволяет перевыбрать шашку
            if (tileForSecondClick.hasPiece()) {
                if (tileForSecondClick.piece!!.type == accentTile!!.piece!!.type
                ) clickCount = 0
            } else {
                if (canMove(possiblePairsForMove, tileForSecondClick)) {
                    //Если возможно съесть
                    if (!possiblePairsForMove!![0].containsValue(null)) {
                        if (eatingTile != null) {
                            accentTile = eatingTile
                            if (canEatThis(possiblePairsForMove, tileForSecondClick)) kill(
                                eatingTile!!,
                                tileForSecondClick
                            )
                        } else {
                            if (canEatThis(possiblePairsForMove, tileForSecondClick)) kill(
                                accentTile!!,
                                tileForSecondClick
                            )
                        }
                        clickCount = 0

                        // ход, когда нечего есть
                    } else {
                        movePiece(accentTile!!, tileForSecondClick)
                        currentMove = currentMove.next()
                    }
                }
            }
            listener.highlightClear()
        }
        if (clickCount == 0) {
            val tileForFirstClick = board[y][x]
            // запрет на выбор чужой шашки
            if (tileForFirstClick.hasPiece()) if (canHighlight(currentMove, tileForFirstClick)) {
                val requiredMoves = requiredMoves()
                val pair = possibleMoves(tileForFirstClick, currentMove)
                possiblePairsForMove = if (requiredMoves.isNotEmpty())
                    filterForRequiredMoves(requiredMoves, tileForFirstClick)
                else
                    pair

                for (i in possiblePairsForMove!!.indices) {
                    for (entry in possiblePairsForMove!![i].entries) {
                        listener.highlightTile(entry.key)
                    }
                }

                accentTile = tileForFirstClick
                clickCount = 1
            }
        }

        if (finishGame()) {
            listener.onFinish(if (currentMove == Piece.Type.BLACK) Piece.Type.WHITE else Piece.Type.BLACK)
            currentMove = currentMove.next()
        }
    }

}
