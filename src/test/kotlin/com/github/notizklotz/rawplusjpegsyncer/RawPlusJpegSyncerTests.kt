package com.github.notizklotz.rawplusjpegsyncer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

class RawPlusJpegSyncerTests {

    private lateinit var directory: Path

    private lateinit var rawsyncer: RawPlusJpegSyncer

    @BeforeEach
    fun prepareTestFiles() {
        directory = createTempDirectory(Paths.get("target"), "testing")
        copyFolder(Paths.get("src/test/resources/testdir"), directory)

        rawsyncer = RawPlusJpegSyncer(directory)
    }

    @Test
    fun `no obsolete files`() {
        rawsyncer.calcObsoleteFiles()

        assertEquals(0, list(rawsyncer.obsoleteDir).count())
        assertEquals(2, list(rawsyncer.slot1.resolve("134_FUJI")).count())
        assertEquals(2, list(rawsyncer.slot1.resolve("135_FUJI")).count())
        assertEquals(2, list(rawsyncer.slot2.resolve("134_FUJI")).count())
        assertEquals(2, list(rawsyncer.slot2.resolve("135_FUJI")).count())
    }

    @Test
    fun `one obsolete RAW file`() {
        delete(rawsyncer.slot2.resolve("134_FUJI/DSCF4078.JPG"))

        rawsyncer.calcObsoleteFiles()

        val obsoleteFiles = list(rawsyncer.obsoleteDir).toList()

        assertEquals(1, obsoleteFiles.count())
        assertEquals(1, list(rawsyncer.slot1.resolve("134_FUJI")).count())
        assertEquals(2, list(rawsyncer.slot1.resolve("135_FUJI")).count())
        assertEquals(1, list(rawsyncer.slot2.resolve("134_FUJI")).count())
        assertEquals(2, list(rawsyncer.slot2.resolve("135_FUJI")).count())
        assertFalse(exists(rawsyncer.slot1.resolve("134_FUJI/DSCF4078.RAF")))
        assertTrue(exists(rawsyncer.obsoleteDir.resolve("134_FUJI/DSCF4078.RAF")))
    }

    @Test
    fun `one obsolete JPG file`() {
        delete(rawsyncer.slot1.resolve("134_FUJI/DSCF4078.RAF"))

        rawsyncer.calcObsoleteFiles()

        assertEquals(1, list(rawsyncer.obsoleteDir).toList().count())
        assertEquals(1, list(rawsyncer.slot1.resolve("134_FUJI")).count())
        assertEquals(2, list(rawsyncer.slot1.resolve("135_FUJI")).count())
        assertEquals(1, list(rawsyncer.slot2.resolve("134_FUJI")).count())
        assertEquals(2, list(rawsyncer.slot2.resolve("135_FUJI")).count())
        assertFalse(exists(rawsyncer.slot2.resolve("134_FUJI/DSCF4078.JPG")))
        assertTrue(exists(rawsyncer.obsoleteDir.resolve("134_FUJI/DSCF4078.JPG")))
    }

    @Test
    fun `multiple obsolete files`() {
        delete(rawsyncer.slot1.resolve("134_FUJI/DSCF4078.RAF"))
        delete(rawsyncer.slot1.resolve("134_FUJI/DSCF4079.RAF"))
        delete(rawsyncer.slot2.resolve("135_FUJI/DSCF1000.JPG"))
        delete(rawsyncer.slot2.resolve("135_FUJI/DSCF4078.JPG"))

        rawsyncer.calcObsoleteFiles()

        assertEquals(2, list(rawsyncer.obsoleteDir.resolve("134_FUJI")).toList().count())
        assertEquals(2, list(rawsyncer.obsoleteDir.resolve("135_FUJI")).toList().count())
        assertEquals(0, list(rawsyncer.slot1.resolve("134_FUJI")).count())
        assertEquals(0, list(rawsyncer.slot1.resolve("135_FUJI")).count())
        assertEquals(0, list(rawsyncer.slot2.resolve("134_FUJI")).count())
        assertEquals(0, list(rawsyncer.slot2.resolve("135_FUJI")).count())
        assertTrue(exists(rawsyncer.obsoleteDir.resolve("134_FUJI/DSCF4078.JPG")))
        assertTrue(exists(rawsyncer.obsoleteDir.resolve("134_FUJI/DSCF4078.JPG")))
        assertTrue(exists(rawsyncer.obsoleteDir.resolve("135_FUJI/DSCF4078.RAF")))
        assertTrue(exists(rawsyncer.obsoleteDir.resolve("135_FUJI/DSCF4078.RAF")))
    }

    private fun copyFolder(src: Path, dest: Path) {
        try {
            walk(src).forEach { s ->
                try {
                    val d = dest.resolve(src.relativize(s))
                    if (isDirectory(s)) {
                        if (!exists(d))
                            createDirectory(d)
                        return@forEach
                    }
                    copy(s, d)// use flag to override existing
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }
}
