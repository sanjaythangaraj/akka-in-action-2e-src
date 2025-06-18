package routers

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object PostalOffice {
  sealed trait Command
  final case class Standard(msg: String) extends Command
  final case class Tracked(msg: String) extends Command
  final case class Guaranteed(msg: String) extends Command

  def apply(): Behavior[Command] = Behaviors.empty
}
