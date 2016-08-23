package com.mfglabs
package commons

import scala.concurrent.Future

import com.mfglabs.precepte._
import corescalaz._
import default.{ Category, Callee, BaseTags }

import Metrics._

trait Metrics {

  type SF[T] = (ST, Future[T])
  def noop =
    new (SF ~~> Future) {
      def apply[A](sf: SF[A]): Future[A] = sf._2
    }

  def Timed[A](category: Category)(f: MonitoringContext => Future[A])(implicit fu: scalaz.Functor[Future], callee: Callee, ec: scala.concurrent.ExecutionContext): Pre[A] =
    Precepte(BaseTags(callee, category)) { (st: ST) =>
      f(MonitoringContext.build(st))
    }

  def TimedM[A](category: Category)(f: MonitoringContext => Pre[A])(implicit mo: scalaz.Monad[Future], callee: Callee, ec: scala.concurrent.ExecutionContext): Pre[A] =
    Precepte(BaseTags(callee, category)) { (st: ST) =>
      f(MonitoringContext.build(st)).eval(st)
    }
}

case class InfluxMetrics(conf: Conf, ctx: Contexts) extends Metrics {

  import Contexts.blockingToEC
  implicit val ec = ctx.blockingCtx

  val influx = conf.Influx.map { i => Influx(i.url, i.user, i.password, i.dbName) }

  val monitor = influx.map(_.monitor).getOrElse(noop)

  override def Timed[A](category: Category)(f: MonitoringContext => Future[A])(implicit fu: scalaz.Functor[Future], callee: Callee, ec: scala.concurrent.ExecutionContext): Pre[A] =
    super.Timed(category)(f).mapSuspension(monitor)

  override def TimedM[A](category: Category)(f: MonitoringContext => Pre[A])(implicit mo: scalaz.Monad[Future], callee: Callee, ec: scala.concurrent.ExecutionContext): Pre[A] =
    super.TimedM(category)(f).mapSuspension(monitor)
}

object Metrics {

  object HttpCat extends Category("http")
  object StravaCat extends Category("strava")

  type ST = default.ST[Unit]
  type Pre[A] = default.DPre[Future, Unit, A]

  import doobie.imports.ConnectionIO
  type DBPre[A] = default.DPre[ConnectionIO, Unit, A]

  def apply(config: Conf, ctx: Contexts): Metrics =
    InfluxMetrics(config, ctx)
}