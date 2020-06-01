package com.github.notizklotz.rawandjpegsync

import java.awt.Desktop
import java.nio.file.Files.isRegularFile
import java.nio.file.Files.walk
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors.toSet

fun main(args: Array<String>) {
    val basedir = (if (args.isEmpty()) "./" else args[0]).let { Paths.get(it) }

    val rawFiles = findFilesByFilenameRegex(basedir, Regex("DSCF.*\\.RAF\$"))
    val jpegFiles = findFilesByFilenameRegex(basedir, Regex("DSCF.*\\.jpg\$", RegexOption.IGNORE_CASE))

    deleteDispensableJpegs(jpegFiles, rawFiles)
    findUndevelopedRaws(rawFiles, jpegFiles)
}

private fun deleteDispensableJpegs(jpegFiles: Set<Path>, rawFiles: Set<Path>) {
    val dispensableJpegs = jpegFiles.filterNot { rawFiles.contains(createRawFilePathFromJpegPath(it)) }
    val desktop = Desktop.getDesktop()
    dispensableJpegs.forEach { desktop.moveToTrash(it.toFile()) }
    println("Moved ${dispensableJpegs.size} JPG files to trash")
}

private fun findUndevelopedRaws(rawFiles: Set<Path>, jpegFiles: Set<Path>) {
    val undevelopedRaw = rawFiles.filterNot { jpegFiles.contains(createJpegFilePathFromRawFilePath(it)) }

    if (undevelopedRaw.isNotEmpty()) {
        println("Undeveloped RAW files found: ${undevelopedRaw.joinToString()}")
    }
}

private fun findFilesByFilenameRegex(dir: Path, regex: Regex): Set<Path> = walk(dir).filter { isRegularFile(it) && it.fileName.toString().matches(regex) }.collect(toSet())

private fun createRawFilePathFromJpegPath(jpeg: Path): Path = jpeg.resolveSibling(jpeg.fileName.toString().replace(".jpg", ".RAF"))

private fun createJpegFilePathFromRawFilePath(raw: Path): Path = raw.resolveSibling(raw.fileName.toString().replace(".RAF", ".jpg"))
