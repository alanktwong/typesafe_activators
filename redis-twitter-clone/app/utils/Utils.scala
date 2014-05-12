package utils

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Applicative


object Utils {

  /**
   * Predicate is used in place of an if statement in for comprehensions over futures.
   * It allows for custom errors instead of the NoSuchElement exceptions generated by if statements
   * use: _ <- predicate(foo.isBar(), "foo is not bar")
   * @param condition a boolean predicate
   * @param fail the message to fail with if condition is false
   * @return an empty preevaluated future
   */
  def predicate(condition: Boolean, fail: String): Future[Unit] =
    if (condition) Future.successful(Unit) else Future.failed(Predicate(fail))

  /**
   * Predicate is used in place of an if statement in for comprehensions over futures.
   * It allows for custom errors instead of the NoSuchElement exceptions generated by if statements
   * use: _ <- predicate(foo.isBar(), MyThrowable("foo is not bar"))
   * @param condition a boolean predicate
   * @param fail the throwable to fail with if condition is false
   * @return an empty preevaluated future
   */
  def predicate(condition: Boolean, fail: Throwable): Future[Unit] =
    if (condition) Future.successful(Unit) else Future.failed(fail)

  /**
   * match_or_else is used in place of pattern matching in for comprehensions over futures.
   * @param to_match a
   * @param fail the throwable to fail with if partial function pf is not applicable
   * @param pf a partial function from A to B
   * @tparam A the type to be matched on
   * @tparam B the type to return
   * @return a future containing the result of partial function pf if applicable or a failed future containing fail
   */
  def match_or_else[A, B](to_match: A, fail: => String)(pf: PartialFunction[A, B]): Future[B] =
    if (pf.isDefinedAt(to_match)){
      try{
        Future.successful( pf(to_match) )
      } catch {
        case e: Throwable => Future.failed(e)
      }
    }else{
      Future.failed(Predicate(fail))
    }


  /**
   * errors whose cause can be displayed client-side. Ex: username already in use, invalid password, etc.
   * @param reason reason for failing which can be displayed to users
   */
  case class UserVisibleError(reason: String) extends Exception(s"user visible error:: $reason")

  /**
   * generic internal errors that should not be displayed client-side
   * @param reason reason for failure
   */
  case class Predicate(reason: String) extends Exception(s"predicate failed:: $reason")

  // this typeclass definition allows us to use scalaz's applicative syntax to dispatch multiple futures concurrently
  implicit def FutureApplicative(implicit executor: ExecutionContext) = new Applicative[Future] {
    def point[A](a: => A) = Future(a)
    def ap[A,B](fa: => Future[A])(f: => Future[A => B]) =
      (f zip fa) map { case (f1, a1) => f1(a1) }
  }
}
