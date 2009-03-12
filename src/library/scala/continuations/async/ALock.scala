package scala.continuations.async

import scala.collection.immutable.Queue
import scala.continuations._
import scala.continuations.ControlContext._

/**
 * An asynchronous lock implementation.
 */
class ALock {

  // TODO: Should we parameterise the Suspender?

  sealed trait State
  case object Unlocked extends State
  case class Locked(q: Queue[Suspendable[Unit]]) extends State

  /**
   * The internal state.
   */
  private var state: State = Unlocked

  def lock = {
    ActorSuspender.shiftSuspendable { (suspendable: Suspendable[Unit]) =>
      synchronized {
        state match {
          case Unlocked => {
            state = Locked(Queue.Empty)
            //println("lock: was unlocked, returning")
            suspendable.transfer(())
          }
          case Locked(q) => {
            state = Locked(q.enqueue(suspendable))
            //println("lock: was locked, queueing and suspending")
            suspendable.suspend
          }
        }
      }
    }
  }

  def tryLock = synchronized {
    state match {
      case Unlocked => {
        state = Locked(Queue.Empty)
        true
      }
      case Locked(q) => {
        false
      }
    }
  }

  def unlock: Unit = synchronized {
    state match {
      case Locked(q) => {
        if (q.isEmpty) {
          state = Unlocked
          //println("unlock: nothing queued")
        } else {
          val (suspendable, newQ) = q.dequeue
          state = Locked(newQ)
          //println("unlock: resuming queued task")
          suspendable.resume(())
        }
      }
      case s => throw new IllegalStateException(s.toString)
    }
  }

  // TODO: Actually implement this.
  def sync[A](body: => A @suspendable): A @suspendable = {
    lock
    val result = body
    unlock
    result
  }
  
  /*
  def sync[A,B](body: => A @cps[B,B]): A @cps[B,B] = {
    shift2 { (ret: A => B, thr: Throwable => B) =>
      reset {
        println("sync: claiming lock")
        lock
        println("sync: building ret0 and thr0")
        val ret0 = { x: A => Result.send(ret, thr) { unlock ; x } }
        val thr0 = { t: Throwable => Result.send(thr, thr) { unlock ; t } }
        println("sync: calling relay")
        relay(ret0, thr0) {
          println("sync: executing body")
          body
          //println("sync: finished executing body")
        }
      }
    }
  }
  */
}
