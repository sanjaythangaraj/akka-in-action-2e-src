package faulttolerance1

import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors

object LogProcessingGuardian {

  def apply(sources: Vector[String], databaseUrl: String): Behavior[Nothing] =
    Behaviors.setup[Nothing] { context =>
      var fileWatchers = Set.empty[ActorRef[FileWatcher.Command]]
      sources.foreach { source =>
        val dbWriter: ActorRef[DbWriter.Command] =
          context.spawnAnonymous(DbWriter(databaseUrl))

        val logProcessor: ActorRef[LogProcessor.Command] =
          context.spawnAnonymous(LogProcessor(dbWriter))

        val fileWatcher: ActorRef[FileWatcher.Command] =
          context.spawnAnonymous(FileWatcher(source, logProcessor))

        context.watch(fileWatcher)
        fileWatchers += fileWatcher
      }

      def checkAndStop(): Behavior[Nothing] = {
        if (fileWatchers.isEmpty) {
          Behaviors.stopped[Nothing]
        } else {
          Behaviors.same
        }
      }


      Behaviors.
        receiveMessage[Nothing] {
          _: Any =>
            Behaviors.ignore
        }
        .receiveSignal {
          case (context, Terminated(actorRef)) =>
            fileWatchers -= actorRef.asInstanceOf[ActorRef[FileWatcher.Command]]
            checkAndStop()
        }
    }

}
