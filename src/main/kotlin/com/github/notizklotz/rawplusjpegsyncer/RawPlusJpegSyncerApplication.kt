package com.github.notizklotz.rawplusjpegsyncer

import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.util.stream.Collectors.toMap

fun main(args: Array<String>) {
    val basedir = (if (args.isEmpty()) "./" else args[0]).let { Paths.get(it) }

    RawPlusJpegSyncer(basedir).calcObsoleteFiles()
}

class RawPlusJpegSyncer(basedir: Path) {

    val slot1 = basedir.resolve("SLOT 1")
    val slot2 = basedir.resolve("SLOT 2")
    val obsoleteDir = basedir.resolve("obsolete")

    fun calcObsoleteFiles() {
        val slot1Files: Map<String, Path> = mapPhotos(slot1)
        val slot2Files: Map<String, Path> = mapPhotos(slot2)

        val keepers = slot1Files.keys.intersect(slot2Files.keys)
        val obsoletePaths = moveObsoleteFiles(slot1Files, keepers).plus(moveObsoleteFiles(slot2Files, keepers))

        createDirectories(obsoleteDir)

        obsoletePaths.forEach {
            val obsDir = obsoleteDir.resolve(it.parent.fileName)

            createDirectories(obsDir)
            move(it, obsDir.resolve(it.fileName), ATOMIC_MOVE)
        }
    }

}

private fun moveObsoleteFiles(slotFiles: Map<String, Path>, keepers: Set<String>) =
        slotFiles.filterKeys { !keepers.contains(it) }.map { it.value }

private fun mapPhotos(slotdir: Path): Map<String, Path> = walk(slotdir)
        .filter { isRegularFile(it) }
        .collect(toMap({ slotdir.relativize(it).toString().removeSuffix(".RAF").removeSuffix(".JPG") }, { it }))

