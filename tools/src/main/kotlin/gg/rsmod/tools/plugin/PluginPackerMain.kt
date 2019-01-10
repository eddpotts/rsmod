package gg.rsmod.tools.plugin

import gg.rsmod.ui.plugin.PluginPackerUI
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList

/**
 * @author Tom <rspsmods@gmail.com>
 */
object PluginPackerMain {

    private enum class PackType {
        ZIP,
        JAR
    }

    @JvmStatic fun main(vararg args: String) {
        val packer = PluginPacker()

        if (args.isNotEmpty() && args.first().toLowerCase() == "gui") {
            PluginPackerUI().start()
        } else {
            val options = Options()

            options.addOption("t", true, "The type of packing: <zip> or <jar>")
            options.addOption("n", true, "The name you would like to give your packed plugin")
            options.addOption("s", true, "The path to the plugin files that you wish to pack")
            options.addOption("c", true, "The path to your Kotlin compiler [only for <jar> packing]")
            options.addOption("g", true, "The path to your Game jar [only for <jar> packing]")
            options.addOption("p", true, "The path to your Plugins jar [only for <jar> packing]")

            val parser = DefaultParser()
            val commands = parser.parse(options, args)

            if (commands.args.contains("help")) {
                HelpFormatter().printHelp("<t> <c> <g> <p> <s> <n>", options)
                return
            } else if (!commands.hasOption('t')) {
                HelpFormatter().printHelp("<t> <c> <g> <p> <s> <n>", options)
                return
            }

            val packType = when {
                commands.getOptionValue('t').toLowerCase() == "zip" -> PackType.ZIP
                commands.getOptionValue('t').toLowerCase() == "jar" -> PackType.JAR
                else -> null
            }

            if (packType == null) {
                HelpFormatter().printHelp("<t> <c> <g> <s> <n>", options)
                return
            }
            val requiredOptions = when (packType) {
                PackType.ZIP -> arrayOf(options.getOption("n"), options.getOption("s"))
                PackType.JAR -> arrayOf(options.getOption("n"), options.getOption("s"), options.getOption("c"), options.getOption("g"), options.getOption("p"))
            }
            if (requiredOptions.any { !commands.hasOption(it.opt) }) {
                HelpFormatter().printHelp("<t> <c> <g> <p> <s> <n>", options)
                return
            }

            try {
                val pluginName = commands.getOptionValue('n')
                val source = Paths.get(commands.getOptionValue('s'))
                val output = Paths.get(".", "plugins")

                if (!Files.exists(source)) {
                    error("Source file path does not exist: $source")
                }

                if (packType == PackType.JAR) {
                    val compilerPath = commands.getOptionValue('c')
                    val gameJarPath = commands.getOptionValue('g')
                    val pluginJarPath = commands.getOptionValue('p')

                    val compiler = Paths.get(compilerPath).resolve("kotlinc")
                    val gameJar = Paths.get(gameJarPath)
                    val pluginJar = Paths.get(pluginJarPath)

                    if (!Files.exists(compiler)) {
                        error("Kotlin compiler could not be found in: $compiler")
                    } else if (!Files.exists(gameJar)) {
                        error("Game distribution jar could not be found in: $gameJar")
                    } else if (!Files.exists(pluginJar)) {
                        error("Plugin distribution jar could not be found in: $gameJar")
                    }

                    if (packer.compileBinary(compilerPath, gameJarPath, pluginName, output, Files.walk(source).toList())) {
                        println("Plugin has been compiled and created as: ${output.resolve("$pluginName.jar")}")
                    } else {
                        println("Could not pack plugin! Make sure your source files do not have any errors, that you have Kotlin compiler installed and that you have write-access to $output")
                    }
                } else {
                    if (packer.compileSource(pluginName, output, Files.walk(source).toList())) {
                        println("Plugin has been compiled and created as: ${output.resolve("$pluginName.zip")}")
                    } else {
                        println("Could not pack plugin! Make sure you have write-access to $output")
                    }
                }
            } catch (e: ParseException) {
                println("Invalid arguments! Use -help for arg explanation.")
            }
        }
    }
}