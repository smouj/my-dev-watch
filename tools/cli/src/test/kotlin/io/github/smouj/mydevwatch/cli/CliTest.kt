package io.github.smouj.mydevwatch.cli

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CliTest {

    private fun run(vararg args: String): Pair<Int, String> {
        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        val code = Cli.run(arrayOf(*args), PrintStream(out), PrintStream(err))
        return code to out.toString()
    }

    @Test
    fun helpIsAlwaysAvailable() {
        val (code, output) = run("help")
        assertEquals(0, code)
        assertTrue(output.contains("my-dev-watch"))
        assertTrue(output.contains("Nothing in this tool writes to a watch"))
    }

    @Test
    fun batteryDecodingMatchesTheLibrary() {
        val (code, output) = run("battery", "A2", "56")
        assertEquals(0, code)
        assertTrue(output.contains("86%"))
        assertEquals(1, run("battery", "E5", "54").first)
    }

    @Test
    fun unknownCommandsFailWithUsage() {
        assertEquals(2, run("nope").first)
    }

    @Test
    fun identificationExplainsItself() {
        val (code, output) = run("identify", "--name", "S10", "--expose", "55ff")
        assertEquals(0, code)
        assertTrue(output.contains("GloryFit"))
        assertTrue(output.contains("Evidence"))
        assertEquals(3, run("identify", "--name", "S10").first)
    }

    @Test
    fun catalogueExposesImplementationGaps() {
        val (code, output) = run("commands")
        assertEquals(0, code)
        assertTrue(output.contains("0xA2"))
        assertTrue(output.contains("no"))
    }
}
