package com.home.akkacourse.part1recap

object GeneralRecap extends App {


  // FP

  val incrementor = new Function[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  val incremented = incrementor(42) // 43
                    // incrementor.apply(42)

  val syntacticSugarIncre = (x: Int) => x + 1


  //println(syntacticSugarIncre(4))

  List(1,2,3,4).map(syntacticSugarIncre).foreach(println)

  // for comprehension

  // this
  val pairs = for {
    num <- List(1,2,3)
    char <- List('a','b', 'c')
  } yield num + "&" + char

  // translates to
  //List(1,2,3).flatMap(num => List('a','b', 'c').map(char => num + "&" + char))




}
