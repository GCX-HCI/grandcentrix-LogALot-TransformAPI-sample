package grandcentrix.net.logalot

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.button).setOnClickListener {
            doSomething()
        }
    }

    @LogALot
    fun doSomething() {
        myField++
    }
}
