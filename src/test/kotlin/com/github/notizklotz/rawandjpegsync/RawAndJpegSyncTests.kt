package com.github.notizklotz.rawandjpegsync

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors.*

class RawAndJpegSyncTests {

    private lateinit var tempDir: Path

    @BeforeEach
    fun prepareTestFiles() {
        tempDir = createTempDirectory(Paths.get("target"), "testing")
        File("src/test/resources/testdir").copyRecursively(tempDir.toFile())
    }

    @Test
    fun `all dispensable files were deleted`() {
        main(arrayOf(tempDir.toString()))

        val files = walk(tempDir).filter { isRegularFile(it) }.map { it.fileName.toString() }.collect(toSet())

        assertThat(files).containsExactlyInAnyOrderElementsOf(listOf("DSCF4078.RAF", "DSCF4078.jpg", "DSCF4079.RAF", "DSCF4079.jpg", "DSCF4080.RAF"))
    }

}
