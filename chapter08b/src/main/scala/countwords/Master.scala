package countwords

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.util.{ Failure, Success }

object Master {

  sealed trait Event
  final case object Tick extends Event

  final case class CountedWords(aggregation: Map[String, Int])
      extends Event
      with CborSerializable
  final case class FailedJob(text: String) extends Event

  def apply(workerRouter: ActorRef[Worker.Command]): Behavior[Event] =
    Behaviors.withTimers { timers =>
      timers.startTimerWithFixedDelay(Tick, Tick, 1.second)
      working(workerRouter)
    }

  def working(
      workerRouter: ActorRef[Worker.Command],
      countedWords: Map[String, Int] = Map(),
      lag: Vector[String] = Vector()): Behavior[Event] =
    Behaviors.setup[Event] { context =>
      implicit val timeout: Timeout = 3.seconds
      val parallelism =
        context.system.settings.config
          .getInt("example.countwords.delegation-parallelism")

      Behaviors.receiveMessage[Event] {
        case Tick =>
          context.log.debug(s"tick, current lag ${lag.size}")

          val text = "this simulates a stream, a very simple stream"
          val allTexts = lag :+ text
          val (firstPart, secondPart) =
            allTexts.splitAt(parallelism)

          firstPart.foreach { text =>
            context.ask(
              workerRouter,
              replyTo => Worker.Process(text, replyTo)) {
              case Success(Worker.Result(map)) =>
                CountedWords(map)
              case Failure(ex) =>
                FailedJob(text)
            }
          }
          working(workerRouter, countedWords, secondPart)

        case CountedWords(map) =>
          val merged = merge(countedWords, map)
          context.log.debug(s"current count ${merged.toString}")
          working(workerRouter, merged, lag)
        case FailedJob(text) =>
          context.log.debug(s"failed, adding text to lag ${lag.size}")
          working(workerRouter, countedWords, lag :+ text)

      }
    }

  def merge(
      currentCount: Map[String, Int],
      newCount: Map[String, Int]): Map[String, Int] =
    (currentCount.toSeq ++ newCount)
      .groupMapReduce(_._1)(_._2)(_ + _)

}

//      val adapter: ActorRef[Worker.Response] =
//        context.messageAdapter(response => CountedWords(response))
