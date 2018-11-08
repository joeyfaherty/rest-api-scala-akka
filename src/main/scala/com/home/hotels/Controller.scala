package com.home.hotels

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.home.hotels.HotelJsonProtocol._
import purecsv.unsafe.CSVReader

import scala.collection.immutable
import scala.concurrent.Future
import scala.io.StdIn
import spray.json._

object Controller {

  final case class Hotel(city: String, id: Long, room: String, price: Long)

  def readAndParseCsv(path: String): immutable.Seq[Hotel] = {
    CSVReader[Hotel].readCSVFromFile(new File(path), skipHeader = true)
  }

  def main(args: Array[String]) {

    val filePath = "/Users/jofa01/workspaces/GIT/rest-api-scala-akka/hoteldb.csv"

    val hotels = readAndParseCsv(filePath)

    val grouped: Map[String, immutable.Seq[Hotel]] = hotels
                    .groupBy(h => h.city)

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    // (fake) async database query api
    def getHotelById(hotelId: Long, hotels: Seq[Hotel]): Future[Option[Hotel]] = Future {
      hotels.find(o => o.id == hotelId)
    }

    def getHotelsByCity(city: String, grouped: Map[String, immutable.Seq[Hotel]]): Future[Option[Seq[Hotel]]] = Future {
      grouped.get(city)
/*      grouped.get(city) match {
        case Some(h) => h
        case None => println(s"Sorry, city $city not found in our records"); Seq()
      }*/
    }

    val route =
      get {
        pathPrefix("hotel" / Remaining) { city =>
          // there might be no item for a given id
          val maybeHotels: Future[Option[Seq[Hotel]]] = getHotelsByCity(city, grouped)

          onSuccess(maybeHotels) {
            case Some(l) => complete(HttpEntity(ContentTypes.`application/json`, l.toJson.prettyPrint))
            case None       => complete(StatusCodes.NotFound)
          }
        }
      }

    val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}