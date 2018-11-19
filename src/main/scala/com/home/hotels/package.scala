package com.home

import java.io.File

import purecsv.unsafe.CSVReader

import scala.collection.immutable


package object hotels {

  final case class Hotel(city: String, id: Long, room: String, price: Long)

  def getOrdering(sort: Option[String]): Ordering[Long] = {
    val ordering = sort match {
      case Some(param) => {
        param.toLowerCase match {
          case "asc" => Ordering[Long]
          case "desc" => Ordering[Long].reverse
          case _ => Ordering[Long]
        }
      }
      case None => Ordering[Long]
    }
    ordering
  }

  def readAndParseCsv(path: String): immutable.Seq[Hotel] = {
    CSVReader[Hotel].readCSVFromFile(new File(path), skipHeader = true)
  }

  def getHotelsByCityId(cityId: String, hotels: Map[String, Seq[Hotel]]): Option[Seq[Hotel]] = {
    hotels.get(cityId)
  }


}
