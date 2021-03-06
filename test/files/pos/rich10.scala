import scala.continuations.ControlContext._

object Test {
  def suspended[A](x: A): A @suspendable = x
  def test[A](x: A, y: A): A @suspendable = if (suspended(x) == y) x else y
}
