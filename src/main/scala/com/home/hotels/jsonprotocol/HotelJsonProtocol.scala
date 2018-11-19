package com.home.hotels.jsonprotocol

import com.home.hotels.Hotel
import spray.json._

object HotelJsonProtocol extends DefaultJsonProtocol {

  // simple deserializers
  implicit val hotelJsonProtocol = jsonFormat4(Hotel)
}
