package faulttolerance1

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior, SupervisorStrategy }
import faulttolerance1.exception.CorruptedFileException

import java.io.File

object LogProcessor {

  sealed trait Command
  final case class LogFile(file: File) extends Command

  def apply(dbWriter: ActorRef[DbWriter.Command]): Behavior[Command] =
    Behaviors
      .supervise {
        Behaviors.receiveMessage[Command] {
          case LogFile(file) =>
            parseFile(file).foreach(dbWriter ! _)
            Behaviors.same
        }
      }
      .onFailure[CorruptedFileException](SupervisorStrategy.resume)

  def parseFile(file: File): List[DbWriter.Line] =
    List(
      DbWriter.Line(System.currentTimeMillis(), "Message1", "INFO"),
      DbWriter.Line(System.currentTimeMillis(), "Message2", "ERROR"))
}
