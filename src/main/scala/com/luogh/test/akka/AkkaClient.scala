package com.luogh.test.akka

import akka.actor.{ActorRef, ActorSystem}
import com.luogh.test.akka.Messages._

/**
  * @author luogh
  */
object AkkaClient {

  private var system: Option[ActorSystem] = None

  private val help =
    """ Usage: AkkaClient [ -h | --help]
        | Then, enter one of the following commands,one per line:
        | h | help      Print the help message
        | c n string    Create record for key n for value string.
        | r n           Read record for key n . It's an error if n isn't found.
        | u n string    Update (or create) record for key n for value string.
        | d n           Delete record for key n . It's an error if n isn't found.
        | crash n       "Crash" worker n ( to test recovery).
        | dump [n]      "Dump" the state of all workers(default) or worker n
        | ^q | quit     quit.
    """.stripMargin

  def main(args: Array[String]): Unit = {
    processArgs(args)
    val sys = ActorSystem("AkkaClient")
    system = Some(sys)
    val server = ServerActor.make(sys)
    val numOfWorkers = sys.settings.config.getInt("server.number-workers")
    server ! Start(numOfWorkers)
    processInput(server)
  }

  private def processArgs(args: Seq[String]): Unit = args match {
    case Nil =>
    case ("-h" | "--help") +: tail => exit(help, 0)
    case head +: tail => exit(s"Unknown input $head! \n" + help, 1)
  }

  private def exit(msg: String, code: Int): Nothing = {
    for (sys <- system) sys.terminate()
    println(msg)
    sys.exit(code)
  }

  private def processInput(server: ActorRef): Unit = {
    val blankRE = """^\s*#?\s*$""".r
    val badCrashRE = """^\s*[Cc][Rr][Aa][Ss][Hh]\s*$""".r
    val crashRE = """^\s*[Cc][Rr][Aa][Ss][Hh]\s+(\d+)\s*$""".r
    val dumpRE = """^\s*[Dd][Uu][Mm][Pp](\s+\d+)?\s*$""".r
    val charNumberRE = """^\s*(\w)\s+(\d+)\s*$""".r
    val charNumberStringRE = """^\s*(\w)\s+(\d+)\s+(.*)$""".r

    def prompt() = print(">> ")
    def missingActorNumber() = println("Crash command requires an actor numbler")
    def invalidInput(s: String) = println(s"Unrecognized command: $s")
    def invalidCommand(s: String) = println(s"Expected 'c','r','u','d'. Got $s")
    def invalidNumber(s: String) = println(s"Expected a number,Got $s")
    def invalidString() = println(s"Expected a string after the command and number")
    def unexpectedString(s: String, n: Int) = println(s"Extra arguments after command and number '$s $n'")
    def finnished(): Nothing = exit("GoodBye", 0)

    val handleLine: PartialFunction[String, Unit] = {
      case blankRE() =>
      case "h" | "help" => println(help)
      case dumpRE(n) =>
        server ! (if (n == null) DumpAll else Dump(n.trim.toInt) )
      case badCrashRE() => missingActorNumber()
      case crashRE(n) =>
        server ! Crash(n.trim.toInt)
      case charNumberStringRE(c, n ,s) => c match {
        case "c" | "C" => server ! Create(n.toInt, s)
        case "u" | "U" => server ! Update(n.toInt, s)
        case "r" | "R" => unexpectedString(c, n.toInt)
        case "d" | "D" => unexpectedString(c, n.toInt)
        case _ => invalidCommand(c)
      }
      case charNumberRE(c, n ) => c match {
        case "r" | "R" => server ! Read(n.toInt)
        case "d" | "D" => server ! Delete(n.toInt)
        case "c" | "C" => invalidString
        case "u" | "U" => invalidString
        case _ => invalidCommand(c)
      }
      case "q" | "quit" | "exit" => finnished()
      case string => invalidInput(string)
    }

    while (true) {
      prompt()
      Console.in.readLine() match {
        case null => finnished()
        case line => handleLine(line)
      }
    }
  }
}
