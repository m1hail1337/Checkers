@file:Suppress("JAVA_MODULE_DOES_NOT_DEPEND_ON_MODULE")

import com.app.checkers.logic.BoardListener
import com.app.checkers.logic.CheckersGame
import com.app.checkers.models.Piece
import com.app.checkers.models.Tile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class CheckersGameTest {

   private lateinit var checkers: CheckersGame

   @Test
   fun boardCreation() {

       val expectPieces = setOf(
           7 to 0, 5 to 0, 3 to 0, 1 to 0,
           6 to 1, 4 to 1, 2 to 1, 0 to 1,
           7 to 2, 5 to 2, 3 to 2, 1 to 2,

           6 to 5, 4 to 5, 2 to 5, 0 to 5,
           7 to 6, 5 to 6, 3 to 6, 1 to 6,
           6 to 7, 4 to 7, 2 to 7, 0 to 7,
       )

       val actualPieces = mutableSetOf<Pair<Int, Int>>()

       checkers = CheckersGame(object : TestBoardListener {
           override fun pieceAdded(tile: Tile) {
               actualPieces.add(tile.x to tile.y)
           }
       })

       checkers.setup()
       assertEquals(Piece.Type.WHITE, checkers.currentMove)

       assertSetsEqual(expectPieces, actualPieces)
   }

    @Test
    fun testHighlight() {
        val actual = 1 to 4
        var highlighted = Tile(-1, -1, null)
        checkers = CheckersGame(object : TestBoardListener {
            override fun highlightTile(tile: Tile) {
                highlighted = tile
                assertEquals(actual, tile.x to tile.y)
            }
        })

        checkers.setup()

        checkers.onTileClick(0, 5)

        assertEquals(actual, highlighted.x to highlighted.y)
    }

    @Test
    fun testDoubleHighlighting() {
        val expectHighlight = setOf(3 to 4, 5 to 4)
        val actualHighlight = mutableSetOf<Pair<Int, Int>>()

        checkers = CheckersGame(object : TestBoardListener {
            override fun highlightTile(tile: Tile) {
                super.highlightTile(tile)

                actualHighlight.add(tile.x to tile.y)
            }
        })

        checkers.setup()

        checkers.onTileClick(4, 5)

        assertSetsEqual(actualHighlight, expectHighlight)
    }

    @Test
    fun testRequireEat() {
        checkers = CheckersGame(object : TestBoardListener {})

        checkers.setup()


        assertEquals(Piece.Type.WHITE, checkers.currentMove)

        checkers.onTileClick(0, 5)
        checkers.onTileClick(1, 4)

        assertEquals(Piece.Type.BLACK, checkers.currentMove)

        checkers.onTileClick(1, 2)
        checkers.onTileClick(0, 3)

        assertEquals(Piece.Type.WHITE, checkers.currentMove)

        checkers.onTileClick(2, 5)
        checkers.onTileClick(3, 4)

        assertEquals(Piece.Type.BLACK, checkers.currentMove)

        checkers.onTileClick(0, 3)

        val requiredTile = checkers.requiredMoves().first().entries.first()

        assertEquals(2 to 5, requiredTile.key!!.x to requiredTile.key!!.y)

    }


    private fun assertSetsEqual(expect: Set<Any>, actual: Set<Any>) {
        assertEquals(expect.size, actual.size)

        for (x in expect) {
            assertTrue(x in actual, "Expected $x in $actual")
        }
    }

    @Test
    fun testEat() {
        val actual = 1 to 4
        var killedPieceAtTile = Tile(-1, -1, null)

        checkers = CheckersGame(object : TestBoardListener {
            override fun pieceRemoved(tile: Tile) {
                killedPieceAtTile = tile
                assertEquals(actual, tile.x to tile.y)
            }
        })

        checkers.setup()

        assertEquals(Piece.Type.WHITE, checkers.currentMove)

        checkers.onTileClick(0, 5)
        checkers.onTileClick(1, 4)

        assertEquals(Piece.Type.BLACK, checkers.currentMove)

        checkers.onTileClick(1, 2)
        checkers.onTileClick(0, 3)

        assertEquals(Piece.Type.WHITE, checkers.currentMove)

        checkers.onTileClick(2, 5)
        checkers.onTileClick(3, 4)

        assertEquals(Piece.Type.BLACK, checkers.currentMove)

        checkers.onTileClick(0, 3)
        checkers.onTileClick(2, 5) // съел


        assertEquals(actual, killedPieceAtTile.x to killedPieceAtTile.y)
    }

    interface TestBoardListener : BoardListener {
        override fun pieceAdded(tile: Tile) = Unit

        override fun highlightTile(tile: Tile) = Unit

        override fun highlightClear() = Unit

        override fun queenAdded(tile: Tile) = Unit

        override fun onFinish(whoWin: Piece.Type) = Unit

        override fun pieceMoved(from: Tile, to: Tile) = Unit

        override fun pieceRemoved(tile: Tile) = Unit
    }
}