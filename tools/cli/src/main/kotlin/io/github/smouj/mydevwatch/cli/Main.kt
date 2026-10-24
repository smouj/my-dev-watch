package io.github.smouj.mydevwatch.cli

fun main(args: Array<String>) {
    kotlin.system.exitProcess(Cli.run(args, System.out, System.err))
}
