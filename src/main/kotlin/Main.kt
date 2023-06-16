import com.n0n5ense.labindicator.bot.LabIndicatorBot
import com.n0n5ense.labindicator.database.connectToDatabase
import com.n0n5ense.labindicator.server.startServer

fun main(args: Array<String>) {
    connectToDatabase(args[1])
    LabIndicatorBot(args[0], 811862552652677161).start()
    startServer(8090, "db790c79dae3153ac3f310148d970588c00078b29f788688c4866cda3796f988d93f9b3bcb95d9a3249682a3f6a10228a1172918dfc4647cea6c12677740b4d6")

}