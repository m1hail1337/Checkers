package com.app.checkers.view

import com.app.checkers.models.Piece
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import tornadofx.*

class FinishDialog(whoWin: Piece.Type) : Dialog<ButtonType>() {
    init {
        title = "Окончание игры"
        with(dialogPane) {
            content = hbox {
                text (if (whoWin == Piece.Type.WHITE) "Белые" else "Черные" + " победили!")
            }
            buttonTypes.add(ButtonType("Начать заново", ButtonBar.ButtonData.OK_DONE))
            buttonTypes.add(ButtonType("Выйти", ButtonBar.ButtonData.NO))
        }
    }
}