package utils

import data.FileInfoHelper
import okio.ByteString
import okio.ByteString.Companion.toByteString

object ZipFileUtils {
    private const val CENSIG = 0x02014b50L         // "PK\001\002" - Central directory file header signature
    private const val LOCSIG = 0x04034b50L         // "PK\003\004" - Local file header signature
    private const val ENDSIG = 0x06054b50L         // "PK\005\006" - End of central directory record signature
    private const val ENDHDR = 22                  // Minimum size of end of central directory record
    private const val ZIP64_ENDSIG = 0x06064b50L   // "PK\006\006" - Zip64 end of central directory record signature
    private const val ZIP64_LOCSIG = 0x07064b50L   // "PK\006\007" - Zip64 end of central directory locator signature
    private const val ZIP64_LOCHDR = 20            // Size of Zip64 end of central directory locator
    private const val ZIP64_MAGICVAL = 0xFFFFFFFFL // Marker for Zip64 fields

    fun locateCentralDirectory(bytes: ByteArray, fileLength: Long): FileInfoHelper.FileInfo {
        val byteString = bytes.toByteString()
        val searchStartPos = bytes.size - ENDHDR
        var cenSize = -1L
        var cenOffset = -1L

        for (currentScanPos in searchStartPos downTo 0) {
            if ((byteString.getIntLe(currentScanPos).toLong() and 0xFFFFFFFFL) == ENDSIG) {
                val cenDirOffsetFieldPos = currentScanPos + 16
                val cenDirSizeFieldPos = currentScanPos + 12

                val offsetOfCentralDir = byteString.getIntLe(cenDirOffsetFieldPos).toLong() and 0xFFFFFFFFL
                val sizeOfCentralDir = byteString.getIntLe(cenDirSizeFieldPos).toLong() and 0xFFFFFFFFL

                if (offsetOfCentralDir == ZIP64_MAGICVAL || sizeOfCentralDir == ZIP64_MAGICVAL) {
                    val zip64LocatorPos = currentScanPos - ZIP64_LOCHDR
                    if (zip64LocatorPos >= 0 && (byteString.getIntLe(zip64LocatorPos).toLong() and 0xFFFFFFFFL) == ZIP64_LOCSIG) {
                        val zip64EocdRecordOffsetInFile = byteString.getLongLe(zip64LocatorPos + 8)
                        val zip64EocdRecordOffsetInBuffer = bytes.size - (fileLength - zip64EocdRecordOffsetInFile).toInt()
                        if (zip64EocdRecordOffsetInBuffer >= 0
                            && (zip64EocdRecordOffsetInBuffer + 56) <= bytes.size
                            && (byteString.getIntLe(zip64EocdRecordOffsetInBuffer).toLong() and 0xFFFFFFFFL) == ZIP64_ENDSIG
                        ) {
                            cenSize = byteString.getLongLe(zip64EocdRecordOffsetInBuffer + 40)
                            cenOffset = byteString.getLongLe(zip64EocdRecordOffsetInBuffer + 48)
                            break
                        }
                    }
                } else {
                    cenSize = sizeOfCentralDir
                    cenOffset = offsetOfCentralDir
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

        while (pos + 46 <= bytes.size) {
            if ((byteString.getIntLe(pos).toLong() and 0xFFFFFFFFL) == CENSIG) {
                val fileNameLength = byteString.getShortLe(pos + 28).toInt() and 0xFFFF
                val extraFieldLength = byteString.getShortLe(pos + 30).toInt() and 0xFFFF
                val fileCommentLength = byteString.getShortLe(pos + 32).toInt() and 0xFFFF
                val relativeOffsetOfLocalHeader = byteString.getIntLe(pos + 42).toLong() and 0xFFFFFFFFL

                val fileNameStartPos = pos + 46
                if (fileNameStartPos + fileNameLength > bytes.size) break

                val currentFileName = byteString.substring(fileNameStartPos, fileNameStartPos + fileNameLength).utf8()
                if (fileName == currentFileName) {
                    localHeaderOffset = relativeOffsetOfLocalHeader
                    break
                }
                pos = fileNameStartPos + fileNameLength + extraFieldLength + fileCommentLength
            } else {
                break
            }
        }
        return localHeaderOffset
    }

    fun locateLocalFileOffset(bytes: ByteArray): Long {
        val byteString = bytes.toByteString()
        if ((byteString.getIntLe(0).toLong() and 0xFFFFFFFFL) == LOCSIG) {
            val fileNameLength = byteString.getShortLe(26).toInt() and 0xFFFF
            val extraFieldLength = byteString.getShortLe(28).toInt() and 0xFFFF
            return (30L + fileNameLength + extraFieldLength)
        }
        return -1L
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