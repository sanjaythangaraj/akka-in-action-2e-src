package subcriptions

import akka.actor.typed.PostStop
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.{
  ClusterDomainEvent,
  MemberExited,
  MemberRemoved,
  MemberUp,
  ReachableMember,
  UnreachableMember
}
import akka.cluster.MemberStatus.Exiting
import akka.cluster.typed.{ Cluster, Subscribe, Unsubscribe }

object ClusterDomainEventListener {
  def apply() = Behaviors.setup[ClusterDomainEvent] { context =>
    Cluster(context.system).subscriptions ! Subscribe(
      context.self,
      classOf[ClusterDomainEvent])

    Behaviors
      .receiveMessage[ClusterDomainEvent] {
        case MemberUp(member) =>
          context.log.info(s"$member UP.")
          Behaviors.same
        case MemberExited(member) =>
          context.log.info(s"$member EXITED.")
          Behaviors.same
        case MemberRemoved(m, previousStatus) =>
          if (previousStatus == Exiting) {
            context.log.info(s"Member $m gracefully exited, REMOVED.")
          } else {
            context.log.info(s"$m downed after unreachable, REMOVED.")
          }
          Behaviors.same
        case UnreachableMember(m) =>
          context.log.info(s"$m UNREACHABLE")
          Behaviors.same
        case ReachableMember(m) =>
          context.log.info(s"$m REACHABLE")
          Behaviors.same
        case event =>
          context.log.info(s"not handling ${event.toString}")
          Behaviors.same
      }
      .receiveSignal {
        case (context, PostStop) =>
          Cluster(context.system).subscriptions ! Unsubscribe(
            context.self)
          Behaviors.stopped
      }
  }
}
