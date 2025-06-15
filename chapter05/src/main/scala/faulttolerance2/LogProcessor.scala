package faulttolerance2

import akka.actor.typed.{ Behavior, SupervisorStrategy }
import akka.actor.typed.scaladsl.Behaviors
import faulttolerance2.exception.ParseException

import java.io.File

object LogProcessor {
  sealed trait Command
  final case class LogFile(file: File) extends Command

  def apply(): Behavior[Command] =
    Behaviors
      .supervise {
        Behaviors.setup[Command] { context =>
          val dbUrl = "jdbc:some-database-url"
          val dbWriter = context.spawn(DbWriter(dbUrl), "db-writer")
          context.watch(dbWriter)
          Behaviors.receiveMessage[Command] {
            case LogFile(file) =>
              readFile(file).foreach { line =>
                val (time, message, messageType) = parseLine(line)
                dbWriter ! DbWriter.Line(time, message, messageType)
              }
              Behaviors.same
          }
        }

      }
      .onFailure[ParseException](SupervisorStrategy.resume)

  private def readFile(file: File): List[String] =
    List("12345 info message", "67890 error something")

  private def parseLine(line: String): (Long, String, String) = {
    val parts = line.split(" ", 3)
    if (parts.length < 3)
      throw new ParseException("Invalid line format", new File(""))
    (parts(0).toLong, parts(2), parts(1))
  }
}
