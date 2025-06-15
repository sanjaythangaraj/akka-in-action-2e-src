package faulttolerance2

import akka.actor.typed.{ Behavior, SupervisorStrategy, Terminated }
import akka.actor.typed.scaladsl.Behaviors
import faulttolerance2.exception.ClosedWatchServiceException

import java.io.File
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object FileWatcher extends FileListeningAbilities {

  sealed trait Command
  final case class NewFile(file: File, timeAdded: Long)
      extends Command
  final case class FileModified(file: File, timeAdded: Long)
      extends Command

  private case object TimerKey
  private case object SendSimulatedMessages extends Command

  def apply(directory: String): Behavior[Command] =
    Behaviors
      .supervise {
        Behaviors.withTimers[Command] { factory =>
          Behaviors.setup[Command] { context =>
            val logProcessor =
              context.spawn(LogProcessor(), "log-processor")
            context.watch(logProcessor)

            register(directory)

            factory.startTimerAtFixedRate(
              TimerKey,
              SendSimulatedMessages,
              5 seconds)

            Behaviors
              .receiveMessage[Command] {
                case SendSimulatedMessages =>
                  // Simulate a new file
                  val newFile = new File(
                    directory,
                    s"new-log-${System.currentTimeMillis()}.log")
                  context.self ! NewFile(
                    newFile,
                    System.currentTimeMillis())

                  // Simulate a modified file
                  val modifiedFile = new File(
                    directory,
                    s"existing-log-${System.currentTimeMillis()}.log")
                  context.self ! FileModified(
                    modifiedFile,
                    System.currentTimeMillis())

                  Behaviors.same

                case NewFile(file, _) =>
                  logProcessor ! LogProcessor.LogFile(file)
                  context.log.info(
                    s"Processed NewFile: ${file.getName}")
                  Behaviors.same

                case FileModified(file, _) =>
                  logProcessor ! LogProcessor.LogFile(file)
                  context.log.info(
                    s"Processed FileModified: ${file.getName}")
                  Behaviors.same
              }
              .receiveSignal {
                case (_, Terminated(_)) =>
                  context.log.info(
                    "LogProcessor terminated, stopping FileWatcher")
                  Behaviors.stopped
              }
          }
        }
      }
      .onFailure[ClosedWatchServiceException](
        SupervisorStrategy.restart)

}
