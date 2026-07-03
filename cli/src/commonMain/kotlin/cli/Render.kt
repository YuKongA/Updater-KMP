package cli

import com.github.ajalt.mordant.rendering.TextColors.brightCyan
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.terminal.Terminal
import data.DataHelper

// Mordant strips ANSI styling automatically when output is piped/redirected;
// padEnd aligns on the plain text, so the piped form stays columnar too.
private val section = bold + brightCyan

private fun Terminal.kv(label: String, value: String) {
    if (value.isNotEmpty()) println(dim(label.padEnd(16)) + value)
}

private fun Terminal.heading(text: String) = println(bold(text))

fun Terminal.renderRom(title: String, rom: DataHelper.RomInfoData) {
    if (rom.version.isEmpty()) return
    println(section(title))
    kv("Type", rom.type)
    kv("Device", rom.device)
    kv("Version", "${rom.version} (${rom.bigVersion})")
    kv("Branch", rom.branch)
    kv("Codebase", "Android ${rom.codebase}")
    kv("Filename", rom.fileName)
    kv("Filesize", rom.fileSize)
    kv("MD5", rom.md5)
    kv("Fingerprint", rom.fingerprint)
    kv("Security patch", rom.securityPatchLevel)
    kv("Build time", rom.timestamp)
    kv("Official", rom.official1Download)
    kv("Official 2", rom.official2Download)
    kv("CDN", rom.cdn1Download)
    kv("CDN 2", rom.cdn2Download)
    if (rom.gentleNotice.isNotEmpty()) {
        heading("Notice")
        println(rom.gentleNotice.trimEnd())
    }
    if (rom.changelog.isNotEmpty()) {
        heading("Changelog")
        println(rom.changelog.trimEnd())
    }
    println()
}

fun Terminal.renderXms(xms: DataHelper.XmsInfoData) {
    if (!xms.hasUpdate) return
    println(section("XMS Update"))
    kv("Current", xms.curVer.ifEmpty { "-" })
    kv("Latest", xms.lstVer)
    kv("Packages", "${xms.apps.size} / ${xms.pkgCnt}")
    if (xms.changelogText.isNotEmpty()) {
        heading("Changelog")
        println(xms.changelogText.trimEnd())
    }
    if (xms.apps.isNotEmpty()) {
        heading("Applications")
        xms.apps.forEach { app ->
            println("- ${app.name} ${app.versionCode} (${app.fileSize})")
            app.downloadUrls.firstOrNull()?.let { println("  " + dim(it)) }
        }
    }
    println()
}
