package routers

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ Behaviors, PoolRouter, Routers }

object BroadcastingChecker {
  def apply(behavior: Behavior[HighWayPatrol.Command]) =
    Behaviors.setup[Unit] { context =>
      val poolSize = 4
      val dataCheckerRouter: PoolRouter[HighWayPatrol.Command] =
        Routers
          .pool(poolSize = poolSize)(behavior)
          .withBroadcastPredicate { msg =>
            msg.isInstanceOf[HighWayPatrol.Violation]
          }
      ???
    }
}
