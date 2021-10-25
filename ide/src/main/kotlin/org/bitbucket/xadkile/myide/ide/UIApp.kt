package org.bitbucket.xadkile.myide.ide

import javafx.scene.text.FontWeight
import tornadofx.App
import tornadofx.Stylesheet
import tornadofx.View
import tornadofx.*

class HelloWorld : View() {
    override val root = hbox {
        label("Hello world")
    }
}

class UIApp : App(HelloWorld::class, Styles::class){
}

class Styles : Stylesheet() {
    init {
        label {
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
            backgroundColor += c("#cecece")
        }
    }
}
