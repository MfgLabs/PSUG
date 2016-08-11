package com.mfglabs

import play.api._
import play.api.ApplicationLoader.Context
import router.Routes

class DemoApplicationLoader extends ApplicationLoader {
  def load(context: Context) = new DemoComponents(context).application
}

import play.api.cache.EhCacheComponents
import models.db.Database

class DemoComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with EhCacheComponents
    with play.filters.gzip.GzipFilterComponents {

  import commons._

  val flyway = new org.flywaydb.play.PlayInitializer(configuration, environment, webCommands)

  lazy val conf = Conf(configuration)
  lazy val database = Database(ctx, conf, metrics)

  lazy val ctx = Contexts(actorSystem)
  lazy val metrics = InfluxMetrics(conf, ctx)

  lazy val applicationController = new controllers.Application(metrics, conf, ctx, database)
  lazy val assets = new _root_.controllers.Assets(httpErrorHandler)

  lazy val router = new Routes(httpErrorHandler, applicationController)
}

object DemoComponents {
  def default = new DemoComponents(ApplicationLoader.createContext(Environment.simple()))
}