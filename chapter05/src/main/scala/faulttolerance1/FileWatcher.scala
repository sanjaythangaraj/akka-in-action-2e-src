package faulttolerance1

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior, SupervisorStrategy }
import faulttolerance1.exception.CorruptedFileException

import java.io.File

object FileWatcher {
  sealed trait Command
  final case class NewFile(file: File, timeAdded: Long)
      extends Command
  private case object AllFilesSent extends Command

  def apply(
      directory: String,
      logProcessor: ActorRef[LogProcessor.Command])
      : Behavior[Command] =
    Behaviors
      .supervise {
        Behaviors.setup[Command] { context =>
          listFiles(directory).foreach { file =>
            context.self ! NewFile(file, System.currentTimeMillis())
          }
          context.self ! AllFilesSent

          Behaviors.receiveMessage[Command] {
            case NewFile(file, _) =>
              logProcessor ! LogProcessor.LogFile(file)
              Behaviors.same
            case AllFilesSent =>
              Behaviors.stopped
          }
        }
      }
      .onFailure[CorruptedFileException](SupervisorStrategy.resume)

  def listFiles(directory: String): List[File] =
    List(
      new File(directory + "file1.log"),
      new File(directory + "file2.log"))
}
