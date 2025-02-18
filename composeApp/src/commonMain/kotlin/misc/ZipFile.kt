package misc

import data.FileInfoHelper
import okio.ByteString
import okio.ByteString.Companion.toByteString

object ZipFileUtil {
    private const val CENSIG = 0x02014b50L        // "PK\001\002"
    private const val LOCSIG = 0x04034b50L        // "PK\003\004"
    private const val ENDSIG = 0x06054b50L        // "PK\005\006"
    private const val ENDHDR = 22
    private const val ZIP64_ENDSIG = 0x06064b50L  // "PK\006\006"
    private const val ZIP64_LOCSIG = 0x07064b50L  // "PK\006\007"
    private const val ZIP64_LOCHDR = 20
    private const val ZIP64_MAGICVAL = 0xFFFFFFFFL

    fun locateCentralDirectory(bytes: ByteArray, fileLength: Long): FileInfoHelper.FileInfo {
        val byteString = bytes.toByteString()
        val startPos = bytes.size - ENDHDR
        var cenSize = -1L
        var cenOffset = -1L

        for (i in 0..startPos) {
            val position = startPos - i
            if (byteString.getIntLe(position).toLong() == ENDSIG) {
                val endSigOffset = position + 4
                val sizeMagic = byteString.getIntLe(endSigOffset + 12)

                if (sizeMagic.toUInt().toLong() == ZIP64_MAGICVAL) {
                    val zip64LocPos = endSigOffset - ZIP64_LOCHDR
                    if (byteString.getIntLe(zip64LocPos - 4).toLong() == ZIP64_LOCSIG) {
                        val zip64EndOffset = byteString.getLongLe(zip64LocPos + 4)
                        val zip64Pos = 4096 - (fileLength - zip64EndOffset).toInt()

                        if (byteString.getIntLe(zip64Pos).toLong() == ZIP64_ENDSIG) {
                            cenSize = byteString.getLongLe(zip64Pos + 40).toULong().toLong()
                            cenOffset = byteString.getLongLe(zip64Pos + 48).toULong().toLong()
                        }
                    }
                } else {
                    cenSize = byteString.getIntLe(endSigOffset + 8).toUInt().toLong()
                    cenOffset = byteString.getIntLe(endSigOffset + 12).toUInt().toLong()
                    break
                }
            }
        }

        return FileInfoHelper.FileInfo(cenOffset, cenSize)
    }

    fun locateLocalFileHeader(bytes: ByteArray, fileName: String): Long {
        val byteString = bytes.toByteString()
        var pos = 0
        var localHeaderOffset = -1L

        while (pos < bytes.size) {
            if (byteString.getIntLe(pos).toLong() == CENSIG) {
                pos += 28
                val fileNameLength = byteString.getShortLe(pos).toUInt().toInt()
                pos += 2
                val extraFieldLength = byteString.getShortLe(pos).toUInt().toInt()
                pos += 2
                val fileCommentLength = byteString.getShortLe(pos).toUInt().toInt()
                pos += 10
                val localHeaderOffsetTemp = byteString.getIntLe(pos).toUInt().toLong()
                pos += 4

                val currentFileName = byteString.substring(pos, pos + fileNameLength).utf8()
                if (fileName == currentFileName) {
                    localHeaderOffset = localHeaderOffsetTemp
                    break
                }
                pos += fileNameLength + extraFieldLength + fileCommentLength
            } else {
                break
            }
        }

        return localHeaderOffset
    }

    fun locateLocalFileOffset(bytes: ByteArray): Long {
        val byteString = bytes.toByteString()
        var localFileOffset = -1L

        if (byteString.getIntLe(0).toLong() == LOCSIG) {
            var pos = 26
            val fileNameLength = byteString.getShortLe(pos).toUInt().toInt()
            pos += 2
            val extraFieldLength = byteString.getShortLe(pos).toUInt().toInt()
            localFileOffset = (pos + 2 + fileNameLength + extraFieldLength).toLong()
        }

        return localFileOffset
    }

    private fun ByteString.getIntLe(pos: Int): Int {
        return (get(pos).toInt() and 0xFF) or
                ((get(pos + 1).toInt() and 0xFF) shl 8) or
                ((get(pos + 2).toInt() and 0xFF) shl 16) or
                ((get(pos + 3).toInt() and 0xFF) shl 24)
    }

    private fun ByteString.getShortLe(pos: Int): Short {
        return ((get(pos).toInt() and 0xFF) or
                ((get(pos + 1).toInt() and 0xFF) shl 8)).toShort()
    }

    private fun ByteString.getLongLe(pos: Int): Long {
        return (getIntLe(pos).toLong() and ZIP64_MAGICVAL) or
                (getIntLe(pos + 4).toLong() shl 32)
    }
}