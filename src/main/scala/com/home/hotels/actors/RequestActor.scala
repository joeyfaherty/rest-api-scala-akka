package com.home.hotels.actors

import akka.actor.{Actor, ActorLogging}
import akka.cluster.sharding.ShardRegion
import akka.pattern.{CircuitBreaker, CircuitBreakerOpenException, pipe}
import com.home.hotels.Hotel
import com.home.hotels.Server.system
import com.home.hotels.actors.ActorType.{RequestBlocked, RequestLimited}
import com.home.hotels.ratelimiter.RateLimiter
import com.home.hotels.ratelimiter.RateLimiter.RateLimitExceeded

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class RequestActor extends Actor with ActorLogging {

  // use config from application.conf
  val config = system.settings.config

  val breaker = CircuitBreaker(
    context.system.scheduler,
    maxFailures = 5,
    callTimeout = config.getInt("rateLimit.limitHitSuspensionMinutes").minutes,
    resetTimeout = config.getInt("rateLimit.limitHitSuspension").minutes
  )

  val limiter = new RateLimiter(
    config.getInt("rateLimit.requests"),
    config.getInt("rateLimit.timeWindowSeconds").seconds
  )

  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case hotels: Option[Seq[Hotel]] =>
      log.info(s"Received hotels: $hotels")
      val theSender = sender()
      breaker.withCircuitBreaker {
        limiter.call {
          Future.successful(hotels) pipeTo theSender
        }
      }.recover {
        case RateLimitExceeded => {
          log.debug(s"RateLimitExceeded has been triggered")
          Future.failed(RequestLimited) pipeTo theSender
        }
        case _: CircuitBreakerOpenException => {
          log.debug(s"CircuitBreakerOpenException has been triggered")
          Future.failed(RequestBlocked) pipeTo theSender
        }
      }

    case e =>
      log.error("Unknown message received {}", e)
  }
}

object ActorType {

  case object RequestLimited extends Exception
  case object RequestBlocked extends Exception

  object Sharding {
    case class EntityEnvelope(id: String, hotels: Option[Seq[Hotel]])

    val shardName = "api-key"

    val extractEntityId: ShardRegion.ExtractEntityId = {
      case EntityEnvelope(id, payload) ⇒ (id.toString, payload)
    }

    def shardIdExtractor(numberOfShards: Int): ShardRegion.ExtractShardId = {
      case env: EntityEnvelope => (env.id.hashCode % numberOfShards).toString
    }
  }
}
