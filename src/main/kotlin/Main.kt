import com.n0n5ense.labindicator.bot.LabIndicatorBot
import com.n0n5ense.labindicator.common.Grade
import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.database.connectToDatabase
import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.database.repository.UserRepository
import com.n0n5ense.labindicator.server.startServer
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.multiple
import kotlinx.cli.required

fun main(args: Array<String>) {
    val parser = ArgParser("LabIndicator")

    val guildId by parser.option(ArgType.String, shortName = "gid").required()
    val dbPath by parser.option(ArgType.String, shortName = "db").required()
    val discordToken by parser.option(ArgType.String, shortName = "t").required()
    val secret by parser.option(ArgType.String, shortName = "s").required()
    val port by parser.option(ArgType.Int, shortName = "p").required()
    val adminUserDiscordId by parser.option(ArgType.String, shortName = "a").multiple()

    parser.parse(args)

    connectToDatabase(dbPath)
    addAdminPermission(adminUserDiscordId)
    LabIndicatorBot(discordToken, guildId.toLong()).start()
    startServer(port, secret)

}

private fun addAdminPermission(discordIds: List<String>) {
    discordIds.forEach {
        UserRepository.add(User(
            name = "adminUser",
            grade = Grade.B1.name,
            discordId = it,
            permissions = listOf(Permissions.Admin)
        ))
    }
}