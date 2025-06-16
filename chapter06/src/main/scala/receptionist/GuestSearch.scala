package receptionist

import akka.actor.typed.ActorRef
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.Behaviors

object GuestSearch {

  sealed trait Command
  final case object Find extends Command
  private final case class ListingResponse(
      listings: Receptionist.Listing)
      extends Command

  def apply(
      actorName: String,
      replyTo: ActorRef[ActorRef[VIPGuest.Command]]) =
    Behaviors.setup[Command] { context =>

      val listingResponseAdapter: ActorRef[Receptionist.Listing] =
        context.messageAdapter[Receptionist.Listing](ListingResponse)

      Behaviors.receiveMessage[Command] {
        case Find =>
          context.system.receptionist ! Receptionist.Find(
            HotelConcierge.goldenKey,
            listingResponseAdapter)
          Behaviors.same

        case ListingResponse(
            HotelConcierge.goldenKey.Listing(listings)) =>
          listings
            .filter(_.path.name.contains(actorName))
            .foreach(actor => replyTo ! actor)
          Behaviors.stopped
      }
    }

}
