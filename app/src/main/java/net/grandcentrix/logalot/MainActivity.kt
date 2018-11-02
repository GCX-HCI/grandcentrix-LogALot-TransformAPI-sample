package net.grandcentrix.logalot

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import net.grandcentrix.gradle.logalot.annotations.LogALot

/**
 * Just a test.
 */
class MainActivity : AppCompatActivity() {

    @LogALot
    private var myField = 0

    @LogALot
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noArgMethod()
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.button).setOnClickListener {
            doSomething("withAParam", 42)
        }
    }

    @LogALot
    private fun noArgMethod(): String = "return something from here"

    @SuppressLint("SetTextI18n")
    @LogALot
    private fun doSomething(str: String, int: Int) {
        myField++
        findViewById<Button>(R.id.button).text = "Current $myField"
        globalFun(myField)
    }
}

@LogALot
private var globalString = "Hello"

@LogALot
private fun globalFun(i: Int) {
    globalString = "Global $i"
}