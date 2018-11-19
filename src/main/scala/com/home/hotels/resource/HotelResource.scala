package com.home.hotels.resource

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes.TooManyRequests
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.home.hotels.actors.ActorType.{RequestBlocked, RequestLimited}
import com.home.hotels.actors.ActorType.Sharding.EntityEnvelope
import com.home.hotels.jsonprotocol.HotelJsonProtocol._
import com.home.hotels.{Hotel, getHotelsByCityId, getOrdering, readAndParseCsv}
import spray.json._

import scala.util.{Failure, Success}

trait HotelResource {
  val actorShard: ActorRef
  implicit val timeout: Timeout

  val route: Route =
    get {
      path("hotel" / Segment / Segment) { (apiKey, cityId) =>
        parameters('sort.?) { sort =>

          // the direction in which to sort the results by price
          val ordering = getOrdering(sort)

          // load the hotels and group by city
          val groupedHotels = readAndParseCsv(path = "hoteldb.csv")
            .groupBy(h => h.city)

          // get the hotels by city and sort them by the ordering passed as a param
          val hotels = getHotelsByCityId(cityId, groupedHotels)
            .map(_.sortBy(_.price)(ordering))


          val result = (actorShard ? EntityEnvelope(apiKey, hotels))
            .mapTo[Option[Seq[Hotel]]]

          onComplete(result) {
            case Success(x) => complete(HttpEntity(ContentTypes.`application/json`, x.toJson.compactPrint))
            case Failure(RequestLimited) => complete(TooManyRequests, "The user has sent too many requests in a given amount of time.")
            case Failure(RequestBlocked) => complete(TooManyRequests, "The user has been blocked.")
            case _ => complete(StatusCodes.ServiceUnavailable, "The server is currently unavailable")
          }

        }
      }
    }
}