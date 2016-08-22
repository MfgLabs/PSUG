package com.mfglabs
package models

import scala.language.implicitConversions

object syntax {
  import scalaz.concurrent.Task
  import scala.concurrent.{ Promise, Future }
  final class TaskExtensionOps[A](x: => Task[A]) {
    import scalaz.{ \/-, -\/ }
    val p: Promise[A] = Promise()
    def runFuture(): Future[A] = {
      x.unsafePerformAsync {
        case -\/(ex) =>
          p.failure(ex); ()
        case \/-(r) => p.success(r); ()
      }
      p.future
    }
  }

  implicit def taskToTaskOps[A](t: => Task[A]) = new TaskExtensionOps[A](t)

}