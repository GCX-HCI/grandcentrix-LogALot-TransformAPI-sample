package net.grandcentrix.logalot

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import grandcentrix.net.logalot.R
import net.grandcentrix.gradle.logalot.annotations.LogALot

/**
 * Just a test.
 */
class MainActivity : AppCompatActivity() {

    @LogALot
    private var myField = 0

    @Suppress("MagicNumber")
    @LogALot
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.button).setOnClickListener {
            doSomething("withAParam", 42)
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("UnusedPrivateMember")
    @LogALot
    private fun doSomething(str: String, int: Int) {
        myField++
        findViewById<Button>(R.id.button).text = "Current $myField"
    }
}
