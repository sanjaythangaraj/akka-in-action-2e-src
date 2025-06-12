import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior, Terminated }

object SimplifiedFileWatcher {
  sealed trait Command
  final case class Watch(ref: ActorRef[String]) extends Command

  def apply(): Behavior[Command] =
    Behaviors
      .receive[Command] { (context, message) =>
        message match {
          case Watch(ref) =>
            context.watch(ref)
            Behaviors.same
        }
      }
      .receiveSignal {
        case (context, Terminated(ref)) =>
          context.log.info("terminated")
          Behaviors.same
      }
}
