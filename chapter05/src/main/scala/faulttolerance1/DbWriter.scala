package faulttolerance1

import akka.actor.typed.{
  Behavior,
  PostStop,
  PreRestart,
  SupervisorStrategy
}
import akka.actor.typed.scaladsl.Behaviors
import faulttolerance1.exception.{
  DbBrokenConnectionException,
  DbNodeDownException
}

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
    supervisionStrategy {
      Behaviors.setup[Command] { context =>
        val connection = new DatabaseConnection(databaseUrl)
        Behaviors
          .receiveMessage[Command] {
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

  def supervisionStrategy(beh: Behavior[Command]): Behavior[Command] =
    Behaviors
      .supervise {
        Behaviors
          .supervise {
            beh
          }
          .onFailure[DbBrokenConnectionException](
            SupervisorStrategy.restart)
      }
      .onFailure[DbNodeDownException](SupervisorStrategy.stop)
}
