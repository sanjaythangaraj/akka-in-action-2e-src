package routers

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }

object Worker {
  def apply(monitor: ActorRef[String]): Behavior[String] =
    Behaviors.receiveMessage[String] { message =>
      monitor ! message
      Behaviors.same
    }
}
