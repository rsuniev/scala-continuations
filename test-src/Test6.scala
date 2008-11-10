// $Id$

import scala.continuations._
import scala.continuations.CPS._



object Test6 {

  def loopWhile[T](cond: =>Boolean)(body: =>(Unit @cpstypes[T,T])): Unit @cpstypes[T,T] = {
    if (cond) {
      body
      loopWhile[T](cond)(body)
    } else
      ()
  }

/*
def loopWhile[T](cond: =>Boolean)(body: =>(Unit @cpstypes[T,T])): Unit  = {
  if (cond) {
    body
    loopWhile[T](cond)(body)
  }
  ()
}
*/


  def main(args: Array[String]) {

    val result = reset {

      var x = 7
      
      loopWhile(x > 0) {
	
        x = x - 1

        shift((continue:Unit=>Any)=> {
          if (x != 2)
            continue()
          else
            "aborted"
        })

        println(x)
      }

      "run to completion"
    }

    println(result)
  }
}