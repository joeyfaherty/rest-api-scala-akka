package com.home.hotels

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.home.hotels.actors.{ActorType, ClientActor}
import com.home.hotels.resource.HotelResource

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * In this case each Actor represents a client
  */
object Server extends App with HotelResource {
  implicit val system = ActorSystem("agoda")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(3.seconds)
  val config = system.settings.config


  // akka cluster sharding is used to represent the requests
  val actorShard = ClusterSharding(system).start(
    typeName = ActorType.Sharding.shardName,
    entityProps = Props[ClientActor],
    settings = ClusterShardingSettings(system),
    extractEntityId = ActorType.Sharding.extractEntityId,
    extractShardId = ActorType.Sharding.shardIdExtractor(config.getInt("clients.sharding.number-of-shards"))
  )

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  bindingFuture.onComplete {
    case Success(serverBinding) =>
      system.log.info(s"Server online at ${serverBinding.localAddress}")

    case Failure(error) =>
      system.log.error(error.getMessage)
      system.terminate()
  }

}
