package com.mfglabs
package commons

import scala.concurrent.ExecutionContext
import akka.actor.ActorSystem

case class Contexts(val actorSystem: ActorSystem) {
  import Contexts._

  implicit val ctrlsCtx = CtrlExeCtx(play.api.libs.concurrent.Execution.defaultContext)

  implicit val dbCtx = DBExeCtx(actorSystem.dispatchers.lookup("contexts.db-context"))
  implicit val wsCtx = WSExeCtx(actorSystem.dispatchers.lookup("contexts.non-blocking-context"))
  implicit val blockingCtx = BlockingExeCtx(actorSystem.dispatchers.lookup("contexts.blocking-context"))
}

object Contexts {

  case class DBExeCtx(val underlying: ExecutionContext) extends AnyVal
  case class WSExeCtx(val underlying: ExecutionContext) extends AnyVal
  case class CtrlExeCtx(val underlying: ExecutionContext) extends AnyVal
  case class BlockingExeCtx(val underlying: ExecutionContext) extends AnyVal

  implicit def dbToEC(implicit ec: DBExeCtx) = ec.underlying
  implicit def wsToEC(implicit ec: WSExeCtx) = ec.underlying
  implicit def ctrlToEC(implicit ec: CtrlExeCtx) = ec.underlying
  implicit def blockingToEC(implicit ec: BlockingExeCtx) = ec.underlying
}