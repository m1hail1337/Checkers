package com.app.checkers.view


import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog


class StartingDialog : Dialog<ButtonType>() {
    init {
        title = "Русские Шашки"
        with(dialogPane) {
            buttonTypes.add(ButtonType("Начать игру", ButtonBar.ButtonData.OK_DONE))
            buttonTypes.add(ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE))
        }
    }
}


