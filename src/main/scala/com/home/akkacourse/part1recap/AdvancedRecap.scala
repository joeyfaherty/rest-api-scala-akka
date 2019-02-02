package com.home.akkacourse.part1recap

object AdvancedRecap extends App {


  // partial functions
  val partialFunction: PartialFunction[Int, Int] = {
    case 5 => 66
    case 42 => 99
    case 107 => 302
  }

  val function: (Int => Int) = partialFunction

  // lifting
  val lifted = partialFunction.lift // total function Int => Option[Int]
  lifted(5) // Some(66)
  lifted(6) // None

  // orElse
  val pfChained = partialFunction.orElse[Int, Int] {
    case 7 => 77
  }

  pfChained(5) // 66 as per partialFunction
  pfChained(7) // 77 as per chained
  pfChained(100) // throws a MatchError

  // type aliases

}
