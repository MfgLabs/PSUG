package com.mfglabs
package models
package db

import com.mfglabs.commons.{ Metrics, Contexts, MonitoringContext }
import scala.concurrent.Future

object Await {
  import scala.concurrent.{ Await => SAwait }
  import scala.concurrent.duration._
  import com.mfglabs.precepte._
  import corescalaz._
  import default._
  import scala.language.postfixOps
  import scalaz.std.scalaFuture._
  import commons.semiUnit

  val env = BaseEnv(Host(java.net.InetAddress.getLocalHost().getHostName()), Environment.Dev, Version(com.mfglabs.BuildInfo.version))
  def nostate = ST(Span.gen, env, Vector.empty, ())

  import com.mfglabs.commons.Metrics.Pre
  def await[A](p: Pre[A]) = {
    implicit val ctxt = play.api.libs.concurrent.Execution.defaultContext
    SAwait.result(p.eval(nostate), 500 minutes)
  }
}

object Atoms {
  import doobie.imports._
  import java.sql.Timestamp
  import java.time.Instant

  implicit val instantMeta: Meta[Instant] =
    Meta[Timestamp].nxmap(_.toInstant, Timestamp.from)
}

object Database {
  import doobie.imports.ConnectionIO
  import Metrics.{ DBPre, ST }

  import com.mfglabs.precepte._, default._

  def WithTrans[A](f: MonitoringContext => ConnectionIO[A])(implicit callee: Callee): DBPre[A] =
    Precepte(BaseTags(callee, Category.Database)) { (st: ST) =>
      f(MonitoringContext.build(st))
    }
}

case class Database(ctx: Contexts, conf: commons.Conf, metrics: Metrics) {

  import com.mfglabs.precepte._
  import default.{ Callee, Category }
  import Contexts.{ DBExeCtx, dbToEC }
  import doobie.imports._

  lazy val xa = {
    import conf.Database._
    import com.mfglabs.precepte._, corescalaz._, default._
    import scalaz.{ Catchable, \/, -\/, \/- }
    import scalaz.std.scalaFuture._

    import ctx.dbCtx
    import commons.semiUnit

    val env = BaseEnv(Host(java.net.InetAddress.getLocalHost().getHostName()), Environment.Dev, Version(com.mfglabs.BuildInfo.version))
    def nostate = ST(Span.gen, env, Vector.empty, ())

    implicit val pcatch =
      new Catchable[Metrics.Pre] {
        def attempt[A](f: Metrics.Pre[A]): Metrics.Pre[Throwable \/ A] =
          Precepte.liftF(f.eval(nostate).map(\/-.apply).recover { case e => -\/(e) })

        def fail[A](err: Throwable): Metrics.Pre[A] =
          Precepte.liftF(Future.failed(err))
      }

    implicit val pcapture =
      new Capture[Metrics.Pre] {
        def apply[A](a: => A) = Precepte.liftF(Future(a)) // Execute SQL calls in a Future
      }

    DriverManagerTransactor[Metrics.Pre](driver, url, user, password)
  }

  def run[A](pre: commons.Metrics.DBPre[A])(st: Metrics.ST)(implicit mo: MetaMonad[ConnectionIO], se: MetaSemigroup[Unit]): Metrics.Pre[A] =
    pre.eval(st).transact(xa)

  def down(): Unit =
    if (conf.environment == commons.Env.Test) {
      import doobie.imports._
      val down =
        List(
          sql"drop table IF EXISTS schema_version",
          sql"drop schema IF EXISTS strava CASCADE"
        )

      for {
        s <- down
      } Await.await(s.update.run.transact(xa))
    }

  import doobie.imports._
  def test(implicit ec: DBExeCtx): Metrics.Pre[Unit] = {
    import com.mfglabs.precepte.default.Macros.callee
    WithCon { mc =>
      sql"""SELECT 1""".query[Int].unique
    }.map(_ => ())
  }

  def WithCon[T](f: MonitoringContext => ConnectionIO[T])(implicit ec: DBExeCtx, callee: Callee): Metrics.Pre[T] = {
    import scalaz.std.scalaFuture._
    metrics.TimedM(Category.Database)(f(_).transact(xa))
  }
}