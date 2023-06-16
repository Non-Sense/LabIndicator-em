import com.n0n5ense.labindicator.bot.LabIndicatorBot
import com.n0n5ense.labindicator.database.connectToDatabase
import com.n0n5ense.labindicator.server.startServer
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required

fun main(args: Array<String>) {
    val parser = ArgParser("LabIndicator")

    val guildId by parser.option(ArgType.String, shortName = "gid").required()
    val dbPath by parser.option(ArgType.String, shortName = "db").required()
    val discordToken by parser.option(ArgType.String, shortName = "t").required()
    val secret by parser.option(ArgType.String, shortName = "s").required()
    val port by parser.option(ArgType.Int, shortName = "p").required()

    parser.parse(args)

    connectToDatabase(dbPath)
    LabIndicatorBot(discordToken, guildId.toLong()).start()
    startServer(port, secret)

}