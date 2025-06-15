package faulttolerance2

import akka.actor.typed.{Behavior, PostStop, PreRestart, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import faulttolerance1.exception.DbBrokenConnectionException
import faulttolerance2.exception.UnexpectedColumnsException

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object DbWriter {
  sealed trait Command
  final case class Line(
      time: Long,
      message: String,
      messageType: String)
      extends Command

  class DatabaseConnection(url: String) {
    def save(line: DbWriter.Line): Unit =
      println(s"Saving line: $line to database at $url")

    def close(): Unit =
      println(s"Closing database connection to $url")
  }

  def apply(databaseUrl: String): Behavior[Command] =
    supervisorStrategy {
      Behaviors.setup[Command] { context =>
        val connection = new DatabaseConnection(databaseUrl)
        Behaviors.receiveMessage[Command] {
          case line: Line =>
            connection.save(line)
            Behaviors.same
        }
          .receiveSignal {
            case (_, PostStop) =>
              connection.close()
              Behaviors.same
            case (_, PreRestart) =>
              connection.close()
              Behaviors.same
          }
      }
    }

  def supervisorStrategy(
      behavior: Behavior[Command]): Behavior[Command] =
    Behaviors
      .supervise {
        Behaviors
          .supervise(behavior)
          .onFailure[UnexpectedColumnsException](
            SupervisorStrategy.resume)
      }
      .onFailure[DbBrokenConnectionException](
        SupervisorStrategy
          .restartWithBackoff(
            minBackoff = 3 seconds,
            maxBackoff = 30 seconds,
            randomFactor = 0.1)
          .withResetBackoffAfter(15 seconds))

}
