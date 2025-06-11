package fishing

import akka.actor.testkit.typed.scaladsl.{
  FishingOutcomes,
  ScalaTestWithActorTestKit,
  TestDuration
}
import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ Behaviors, TimerScheduler }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._

class FishingSpec
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers {

  "A timing test" must {
    val interval = 100.milliseconds

    "be able to cancel timer" in {
      val probe = createTestProbe[Receiver.Command]()
      val timerKey = "key1234"

      val sender =
        Behaviors.withTimers[Sender.Command] { timer =>
          timer.startTimerAtFixedRate(timerKey, Sender.Tick, interval)
          Sender.apply(probe.ref, timer)
        }

      val ref = spawn(sender)
      probe.expectMessage(Receiver.Tock)
      probe.fishForMessage(3.seconds) {
        case Receiver.Tock =>
          if (scala.util.Random.nextInt(4) == 0)
            ref ! Sender.Cancel(timerKey)
          FishingOutcomes.continueAndIgnore

        case Receiver.Cancelled => FishingOutcomes.complete
        case message =>
          FishingOutcomes.fail(s"unexpected message: $message")
      }
      probe.expectNoMessage(interval + 100.millis.dilated)
    }
  }

  "a monitor" must {
    "intercept the messages" in {
      val probe = createTestProbe[String]()
      val behavior = Behaviors.receiveMessage[String] { _ =>
        Behaviors.ignore
      }

      val behaviorMonitored = Behaviors.monitor(probe.ref, behavior)
      val actor = spawn(behaviorMonitored)

      actor ! "checking"
      probe.expectMessage("checking")
    }
  }

  "An automated resuming counter" must {
    "receive a resume after a pause" in {
      val probe = createTestProbe[CounterTimer.Command]()
      val counterMonitored =
        Behaviors.monitor(probe.ref, CounterTimer())

      val counter = spawn(counterMonitored)

      counter ! CounterTimer.Pause(1)
      probe.fishForMessage(3.seconds) {
        case CounterTimer.Increase =>
          FishingOutcomes.continueAndIgnore
        case CounterTimer.Pause(_) =>
          FishingOutcomes.continueAndIgnore
        case CounterTimer.Resume =>
          FishingOutcomes.complete
      }
    }
  }

}

object Receiver {
  sealed trait Command

  final case object Tock extends Command

  final case object Cancelled extends Command

  def apply() = Behaviors.ignore
}

object Sender {
  sealed trait Command

  final case object Tick extends Command

  final case class Cancel(key: String) extends Command

  def apply(
      forwardTo: ActorRef[Receiver.Command],
      timer: TimerScheduler[Command]): Behavior[Command] =
    Behaviors.receiveMessage[Command] {
      case Tick =>
        forwardTo ! Receiver.Tock
        Behaviors.same
      case Cancel(key) =>
        timer.cancel(key)
        forwardTo ! Receiver.Cancelled
        Behaviors.same
    }
}
