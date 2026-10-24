package io.github.smouj.mydevwatch.cli

import io.github.smouj.mydevwatch.capture.CaptureCodec
import io.github.smouj.mydevwatch.capture.CaptureParse
import io.github.smouj.mydevwatch.capture.HexFormat
import io.github.smouj.mydevwatch.firmware.FirmwareAnalyzer
import io.github.smouj.mydevwatch.model.DeviceFingerprint
import io.github.smouj.mydevwatch.model.SupportLevel
import io.github.smouj.mydevwatch.protocol.ProtocolRegistry
import io.github.smouj.mydevwatch.protocol.gloryfit.GloryFitBattery
import io.github.smouj.mydevwatch.protocol.gloryfit.GloryFitCommand
import io.github.smouj.mydevwatch.watchface.WatchFaceCodec
import io.github.smouj.mydevwatch.watchface.WatchFaceRead
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * The CLI is the project's laboratory bench: everything the Android app needs to
 * prove can first be proved here, on a file, without a watch.
 */
object Cli {

    fun run(args: Array<String>, out: PrintStream, err: PrintStream): Int {
        if (args.isEmpty() || args[0] == "help" || args[0] == "--help") {
            printHelp(out)
            return 0
        }
        return try {
            when (args[0]) {
                "hex" -> hex(args, out)
                "battery" -> battery(args, out)
                "commands" -> commands(out)
                "identify" -> identify(args, out)
                "watchface" -> watchFace(args, out, err)
                "firmware" -> firmware(args, out, err)
                "capture" -> capture(args, out, err)
                "levels" -> levels(out)
                else -> {
                    err.println("Unknown command: " + args[0])
                    printHelp(err)
                    2
                }
            }
        } catch (error: Exception) {
            err.println("Error: " + (error.message ?: error::class.simpleName))
            1
        }
    }

    private fun hex(args: Array<String>, out: PrintStream): Int {
        if (args.size < 2) return usage(out, "hex <hex bytes>")
        val bytes = HexFormat.decode(args.drop(1).joinToString(" "))
        out.println(HexFormat.encode(bytes))
        out.println(bytes.size.toString() + " bytes")
        return 0
    }

    private fun battery(args: Array<String>, out: PrintStream): Int {
        if (args.size < 2) return usage(out, "battery <A2 xx>")
        val payload = HexFormat.decode(args.drop(1).joinToString(" "))
        val percent = GloryFitBattery.decode(payload)
        if (percent == null) {
            out.println("Not a valid GloryFit battery frame")
            return 1
        }
        out.println("Battery: " + percent + "%")
        return 0
    }

    private fun commands(out: PrintStream): Int {
        out.println("GloryFit command catalogue")
        out.println("opcode  implemented  confidence  title")
        GloryFitCommand.entries.forEach { command ->
            out.println(
                "%6s  %-11s  %-10s  %s".format(
                    "0x%02X".format(command.opcode),
                    if (command.implemented) "yes" else "no",
                    command.confidence.name.lowercase(),
                    command.title
                )
            )
        }
        return 0
    }

    private fun identify(args: Array<String>, out: PrintStream): Int {
        var name: String? = null
        val exposed = mutableSetOf<String>()
        val advertised = mutableSetOf<String>()
        var index = 1
        while (index < args.size) {
            when (args[index]) {
                "--name" -> { name = args.getOrNull(++index) }
                "--expose" -> args.getOrNull(++index)?.let { exposed += it }
                "--advertise" -> args.getOrNull(++index)?.let { advertised += it }
                else -> return usage(out, "identify [--name NAME] [--expose UUID] [--advertise UUID]")
            }
            index++
        }
        val fingerprint = DeviceFingerprint(name, advertised, exposed)
        val identification = ProtocolRegistry().identify(fingerprint)
        out.println("Protocol:   " + identification.protocol.displayName)
        out.println("Confidence: " + (identification.confidence * 100).toInt() + "%")
        if (identification.matched.isEmpty()) {
            out.println("Evidence:   none")
        } else {
            out.println("Evidence:   " + identification.matched.joinToString(", "))
        }
        identification.alternatives.forEach { (protocol, score) ->
            out.println("Alternative: " + protocol.displayName + " " + (score * 100).toInt() + "%")
        }
        return if (identification.isConclusive) 0 else 3
    }

    private fun watchFace(args: Array<String>, out: PrintStream, err: PrintStream): Int {
        if (args.size < 3 || args[1] != "inspect") return usage(out, "watchface inspect <file>")
        val bytes = Files.readAllBytes(Path.of(args[2]))
        return when (val result = WatchFaceCodec.read(bytes)) {
            is WatchFaceRead.Invalid -> {
                err.println("Invalid container: " + result.reason.name)
                1
            }
            is WatchFaceRead.Success -> {
                val container = result.container
                out.println("Container:      " + HexFormat.encode(container.magic))
                out.println("Width:          " + container.width + " px")
                out.println("Payload:        " + container.payload.size + " bytes")
                out.println("Total size:     " + container.totalSize + " bytes")
                out.println("Declared CRC:   " + java.lang.Long.toHexString(result.declaredCrc))
                out.println("Computed CRC:   " + java.lang.Long.toHexString(container.checksum))
                out.println("Checksum:       " + if (result.checksumValid) "valid" else "MISMATCH")
                out.println("Compressed:     " + if (FirmwareAnalyzer.entropy(container.payload) >= 7.2) "likely" else "no")
                0
            }
        }
    }

    private fun firmware(args: Array<String>, out: PrintStream, err: PrintStream): Int {
        if (args.size < 3 || args[1] != "inspect") return usage(out, "firmware inspect <file>")
        val path = Path.of(args[2])
        if (!Files.isRegularFile(path)) {
            err.println("File not found: " + path)
            return 1
        }
        val report = FirmwareAnalyzer.analyze(Files.readAllBytes(path))
        out.println("SHA-256:        " + report.sha256)
        out.println("Size:           " + report.size + " bytes")
        out.println("Entropy:        " + report.entropy + " bits/byte")
        out.println("Compressed:     " + if (report.looksCompressed) "likely" else "no")
        out.println("Printable:      " + (report.printableRatio * 100).toInt() + "%")
        out.println("Signatures:     " + if (report.signatures.isEmpty()) "none recognised" else report.signatures.joinToString(", "))
        out.println("Strings:        " + report.stringCount + " (first " + report.strings.size + " shown)")
        report.strings.take(10).forEach { out.println("  " + it) }
        out.println()
        out.println("Read-only analysis. Nothing was written to any device.")
        return 0
    }

    private fun capture(args: Array<String>, out: PrintStream, err: PrintStream): Int {
        if (args.size < 3 || args[1] != "inspect") return usage(out, "capture inspect <file>")
        val text = Files.readString(Path.of(args[2]))
        return when (val parsed = CaptureCodec.parse(text)) {
            is CaptureParse.Invalid -> {
                err.println("Line " + parsed.line + ": " + parsed.reason)
                1
            }
            is CaptureParse.Success -> {
                val file = parsed.file
                out.println("Device:    " + (file.manifest.device ?: "unknown"))
                out.println("Protocol:  " + file.manifest.protocol.displayName)
                out.println("Created:   " + file.manifest.createdAt)
                out.println("Packets:   " + file.packets.size)
                out.println("TX / RX:   " + file.packets.count { it.direction.name == "TX" } + " / " + file.packets.count { it.direction.name == "RX" })
                0
            }
        }
    }

    private fun levels(out: PrintStream): Int {
        SupportLevel.entries.forEach { level ->
            out.println(level.level.toString().padStart(2) + "  " + level.label.padEnd(16) + level.description)
        }
        return 0
    }

    private fun usage(out: PrintStream, text: String): Int {
        out.println("Usage: my-dev-watch " + text)
        return 2
    }

    private fun printHelp(out: PrintStream) {
        out.println("""
            my-dev-watch — local-first toolkit for GloryFit-family smartwatches

            Usage: my-dev-watch <command> [options]

            Commands:
              hex <bytes>                 Normalise a hex string
              battery <A2 xx>             Decode a battery frame
              commands                    List the GloryFit command catalogue
              identify --name --expose    Score protocol identification
              watchface inspect <file>    Read a watch-face container
              firmware inspect <file>     Analyse a firmware image (read-only)
              capture inspect <file>      Summarise a .mdcap session
              levels                      Explain compatibility levels 0-6

            Nothing in this tool writes to a watch. Flashing is not implemented.
        """.trimIndent())
    }
}
