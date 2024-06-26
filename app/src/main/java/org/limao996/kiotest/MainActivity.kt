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

        val a = kio.open("/sdcard/test.txt")
        a.checkAndRequestPermission {
            a.createNewFile()
            a.openOutputStream("t").writer().use {
                it.write("测试1")
            }
            log(
                "A", a.openInputStream().reader().readText()
            )
            
            val b = kio.open("/sdcard/Android/data/bin.mt.plus/a.txt")
            b.checkAndRequestPermission {
                b.createNewFile()
                a.copyContentTo(b)
                b.openOutputStream("a").writer().use {
                    it.write("测试2")
                }
                log(
                    "B", b.openInputStream().reader().readText()
                )
            }

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
