package com.home.hotels

import com.home.hotels.Controller.Hotel
import spray.json._

object HotelJsonProtocol extends DefaultJsonProtocol {
  implicit val hotelJsonProtocol = jsonFormat4(Hotel)
}
