package routers

import akka.actor.testkit.typed.scaladsl.{LogCapturing, ScalaTestWithActorTestKit, TestProbe}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PoolRoutersSpec
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers
    with LogCapturing {

  "a pool router" should {
    "send messages in round-robin fashion" in {

      val probe = TestProbe[String]
      val worker = Worker(probe.ref)

      val router = spawn(Manager(worker), "round-robin")

      probe.expectMessage("hi")
      probe.receiveMessages(10)
    }
  }
}
