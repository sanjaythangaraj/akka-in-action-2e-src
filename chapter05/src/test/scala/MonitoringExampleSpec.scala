import akka.actor.testkit.typed.scaladsl.{LogCapturing, LoggingTestKit, ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class MonitoringExampleSpec
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers
    with LogCapturing {

  "Among two actors, No parent/child related, the watcher" must {

    "be able to notified with Terminated when watched actor stops" in {
      val watcher: ActorRef[SimplifiedFileWatcher.Command] =
        spawn(SimplifiedFileWatcher())
      val logProcessor =
        spawn(Behaviors.receiveMessagePartial[String] {
          case "stop" =>
            Behaviors.stopped
        })

      watcher ! SimplifiedFileWatcher.Watch(logProcessor)

      LoggingTestKit.info("terminated").expect {
        logProcessor ! "stop"
      }
    }

  }

  "Among two actors, parent/child related, the watcher" must {

    "get notified by ChildFailed if it is a child that failed" in {
      val probe = TestProbe[String]
      val watcher = spawn(ParentWatcher(probe.ref))

      watcher ! ParentWatcher.Spawn(ParentWatcher.childBehavior)

      watcher ! ParentWatcher.FailChildren

      probe.expectMessage("childFailed")
    }

    "get notified by Termination if it is a child that only stopped" in {
      val probe = TestProbe[String]
      val watcher = spawn(ParentWatcher(probe.ref))


      watcher ! ParentWatcher.Spawn(ParentWatcher.childBehavior)
      watcher ! ParentWatcher.StopChildren
      probe.expectMessage("terminated")
    }

    val restartingChildBehavior = Behaviors
      .supervise(ParentWatcher.childBehavior)
      .onFailure(SupervisorStrategy.restart)

    "is not being notified if the watched child throws an Non-Fatal Exception while having a restart strategy" in {
      val probe = TestProbe[String]
      val watcher = spawn(ParentWatcher(probe.ref))

      watcher ! ParentWatcher.Spawn(restartingChildBehavior)
      watcher ! ParentWatcher.FailChildren
      probe.expectNoMessage()
    }

    "being notified if child with restart strategy gets stopped" in {
      val probe = TestProbe[String]
      val watcher = spawn(ParentWatcher(probe.ref))

      watcher ! ParentWatcher.Spawn(restartingChildBehavior)
      watcher ! ParentWatcher.StopChildren
      probe.expectMessage("terminated")
    }
  }
}
