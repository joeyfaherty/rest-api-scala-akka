package com.home.hotels.ratelimiter

import com.home.hotels.RateLimitExceeded

import scala.concurrent.Future
import scala.concurrent.duration.{Deadline, FiniteDuration}

class RateLimiter(requests: Int, period: FiniteDuration) {
  private val startTimes = {
    val onePeriodAgo = Deadline.now - period
    Array.fill(requests)(onePeriodAgo)
  }
  private var position = 0

  private def lastTime = startTimes(position)

  private def enqueue(time: Deadline): Unit = {
    startTimes(position) = time
    position += 1
    if (position == requests) position = 0
  }

  def call[T](block: => Future[T]): Future[T] = {
    val now = Deadline.now
    if ((now - lastTime) < period) Future.failed(RateLimitExceeded)
    else {
      enqueue(now)
      block
    }
  }
}