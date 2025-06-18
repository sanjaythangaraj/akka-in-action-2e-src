package routers

import akka.actor.testkit.typed.scaladsl.{
  LogCapturing,
  ScalaTestWithActorTestKit,
  TestProbe
}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class StateBasedRouterSpec
    extends ScalaTestWithActorTestKit
    with AnyWordSpecLike
    with Matchers
    with LogCapturing {

  "A State Based Router" should {
    val forwardToProbe = TestProbe[String]
    val alertToProbe = TestProbe[String]

    val switch =
      spawn(Switch(forwardToProbe.ref, alertToProbe.ref), "switch")

    "route to forward to actor reference when on" in {
      switch ! Switch.Payload("hello", ":metadata")
      forwardToProbe.expectMessage("hello")
    }

    "route to alert to actor reference when off" in {
      switch ! Switch.SwitchOff
      switch ! Switch.Payload("hello", ":metadata")
      alertToProbe.expectMessage(":metadata")
    }
  }
}
