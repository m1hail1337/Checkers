package com.app.checkers.view

import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import tornadofx.*

class RulesWindow : Dialog<ButtonType>() {
    init {
        title = "Правила"
        with(dialogPane) {
            content = hbox {
                text ("1.Шашки ходят только по клеткам тёмного цвета.\n" +
                        "2.Доска расположена так, чтобы угловое поле внизу слева со стороны игрока было тёмным.\n" +
                        "3.Простая шашка бьёт вперёд и назад, дамка ходит и бьёт на любое поле диагонали.\n" +
                        "4.Во время боя простая шашка может превратиться в дамку и сразу продолжить бой по правилам дамки.\n" +
                        "5.При наличии нескольких вариантов боя можно выбрать любой из них.")
            }
            buttonTypes.add(ButtonType("Играть!", ButtonBar.ButtonData.OK_DONE))
        }
    }
}