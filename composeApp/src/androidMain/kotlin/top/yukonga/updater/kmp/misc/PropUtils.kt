package top.yukonga.updater.kmp.misc

import android.os.Environment
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.Properties

object PropUtils {

    fun getProps(names: List<String>): Map<String, String> {
        return names.associateWith { getProp(it) }
    }

    fun getProp(name: String): String {
        var prop = getPropByShell(name)
        if (prop.isEmpty()) prop = getPropByStream(name)
        return prop
    }

    private fun getPropByShell(propName: String): String {
        return try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            BufferedReader(InputStreamReader(p.inputStream), 1024).use { it.readLine() ?: "" }
        } catch (ignore: IOException) {
            ""
        }
    }

    private fun getPropByStream(key: String): String {
        return try {
            val prop = Properties()
            FileInputStream(File(Environment.getRootDirectory(), "build.prop")).use { prop.load(it) }
            prop.getProperty(key, "")
        } catch (_: Exception) {
            ""
        }
    }
}