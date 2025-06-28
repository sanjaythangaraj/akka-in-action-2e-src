package countwords

import akka.actor.typed.ActorRef
import akka.actor.typed.receptionist.{ Receptionist, ServiceKey }
import akka.actor.typed.scaladsl.Behaviors

object Worker {

  val RegistrationKey = ServiceKey[Worker.Command]("Worker")

  sealed trait Command
  final case class Process(text: String, replyTo: ActorRef[Response])
      extends Command
      with CborSerializable

  sealed trait Response
  final case class Result(aggregation: Map[String, Int])
      extends Response
      with CborSerializable

  def apply() = Behaviors.setup[Command] { context =>
    context.log.debug(
      s"${context.self} subscribing to $RegistrationKey")

    context.system.receptionist ! Receptionist
      .Register(RegistrationKey, context.self)

    Behaviors.receiveMessage[Command] {
      case Process(text, replyTo) =>
        context.log.debug(s"processing $text")
        replyTo ! Result(processTask(text))
        Behaviors.same
    }

  }

  def processTask(text: String): Map[String, Int] =
    text
      .split("\\W+")
      .foldLeft(Map.empty[String, Int]) { (mapAccumulator, word) =>
        mapAccumulator + (word -> (mapAccumulator.getOrElse(word, 0) + 1))
      }
}
