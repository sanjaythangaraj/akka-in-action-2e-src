package faulttolerance2

import akka.actor.typed.{ ActorRef, Behavior, Terminated }
import akka.actor.typed.scaladsl.Behaviors

object LogProcessingGuardian {
  def apply(directories: Vector[String]): Behavior[Nothing] =
    Behaviors.setup[Nothing] { context =>
      val fileWatchers = directories.map { directory =>
        val fileWatcher =
          context.spawnAnonymous(FileWatcher(directory))
        context.watch(fileWatcher)
        fileWatcher
      }.toSet
      monitorFileWatchers(fileWatchers)
    }

  private def monitorFileWatchers(
      fileWatchers: Set[ActorRef[FileWatcher.Command]])
      : Behavior[Nothing] =
    Behaviors.receiveSignal[Nothing] {
      case (context, Terminated(actorRef)) =>
        val remaining = fileWatchers - actorRef
            .asInstanceOf[ActorRef[FileWatcher.Command]]
        if (remaining.isEmpty) {
          context.log.info(
            "All FileWatchers have terminated, shutting down the system")
          Behaviors.stopped
        } else {
          monitorFileWatchers(remaining)
        }
    }
}
