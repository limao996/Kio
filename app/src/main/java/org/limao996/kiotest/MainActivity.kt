package org.limao996.kiotest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.limao996.kio.Kio

class MainActivity : AppCompatActivity() {
    private val kio = Kio(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getDatabasePath("test.txt").createNewFile()

        val a = kio.open(
            getDatabasePath("test.txt").absolutePath
        )

        if (!a.checkPermission()) a.requestPermission()
        a.openOutputStream("t")
            .writer()
            .apply {
                write("测试1")
                close()
            }
        log(
            "A",
            a.openInputStream()
                .reader()
                .readText()
        )

        val b = kio.open("/sdcard/Android/data/bin.mt.plus/a.txt/../a.txt")
        if (!b.checkPermission()) b.requestPermission()
        else {
            b.openOutputStream("a")
                .writer()
                .apply {
                    write("测试2")
                    close()
                }
            log(
                "B",
                b.openInputStream()
                    .reader()
                    .readText()
            )

            log(a.absolutePath)
            log(b.absolutePath)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        kio.onActivityResult(requestCode, resultCode, data)
    }
}

fun log(vararg msg: Any?) {
    Log.i("KioLog", msg.joinToString("\t"))
}
