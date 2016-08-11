package com.mfglabs

import play.api._

object Helpers {

  class AmerTestApplicationLoader {
    def load(context: ApplicationLoader.Context) = {
      new AmerComponents(context)
    }
  }

  val testCtx = ApplicationLoader.createContext(new Environment(new java.io.File("."), ApplicationLoader.getClass.getClassLoader, Mode.Test))

  class WithApplicationLoader(
      applicationLoader: AmerTestApplicationLoader,
      context: ApplicationLoader.Context = testCtx
  ) {
    def apply(f: AmerComponents => Any) = {
      implicit lazy val ctx = applicationLoader.load(context)
      test.Helpers.running(ctx.application) {
        try { f(ctx) }
        finally { ctx.database.down() }
        ()
      }
    }
  }

  object TestApp {
    def apply(f: AmerComponents => Any) = new WithApplicationLoader(new AmerTestApplicationLoader).apply(f)
  }
}

import org.scalatest._

import scala.reflect.runtime.universe.WeakTypeTag
import doobie.imports._
import doobie.util.transactor.Transactor
import doobie.util.query.{ Query, Query0 }
import doobie.util.update.{ Update, Update0 }
import doobie.util.analysis._
import doobie.util.pretty._

import scalaz.concurrent.Task
import scalaz._

// Shamelessly stolen from https://gist.github.com/veegee/ac202e30579ab6aaf68f3bd007208e67
trait QueryChecker extends WordSpec {
  def transactor: Transactor[Task]

  def check[A, B](q: Query[A, B])(implicit A: WeakTypeTag[A], B: WeakTypeTag[B]) =
    checkAnalysis(s"Query[${typeName(A)}, ${typeName(B)}]", q.stackFrame, q.sql, q.analysis)

  def check[A](q: Query0[A])(implicit A: WeakTypeTag[A]) =
    checkAnalysis(s"Query0[${typeName(A)}]", q.stackFrame, q.sql, q.analysis)

  def checkOutput[A](q: Query0[A])(implicit A: WeakTypeTag[A]) =
    checkAnalysis(s"Query0[${typeName(A)}]", q.stackFrame, q.sql, q.outputAnalysis)

  def check[A](q: Update[A])(implicit A: WeakTypeTag[A]) =
    checkAnalysis(s"Update[${typeName(A)}]", q.stackFrame, q.sql, q.analysis)

  def check[A](q: Update0)(implicit A: WeakTypeTag[A]) =
    checkAnalysis(s"Update0", q.stackFrame, q.sql, q.analysis)

  /** Check if the analysis has an error */
  private def hasError(analysis: ConnectionIO[Analysis]): Boolean = {
    transactor.trans(analysis).attemptRun match {
      case -\/(e) => true
      case \/-(a) => !(a.paramDescriptions.map { case (s, es) => es.isEmpty } ++ a.columnDescriptions.map { case (s, es) => es.isEmpty }).forall(x => x)
    }
  }

  private def checkAnalysis(typeName: String, stackFrame: Option[StackTraceElement], sql: String, analysis: ConnectionIO[Analysis]) = {
    if (hasError(analysis)) {
      val analysisOutput = transactor.trans(analysis).attemptRun match {
        case -\/(e) =>
          failure("SQL Compiles and Typechecks", formatError(e.getMessage))
        case \/-(a) =>
          success("SQL Compiles and Typechecks", None) +
            a.paramDescriptions.map { case (s, es) => assertEmpty(s, es) }.mkString("\n") +
            a.columnDescriptions.map { case (s, es) => assertEmpty(s, es) }.mkString("\n")
      }

      println(formatSql(sql))
      println(analysisOutput)
      fail()
    }
  }

  private val packagePrefix = "\\b[a-z]+\\.".r

  private def assertEmpty(name: String, es: List[AlignmentError]): String =
    if (es.isEmpty) success(name, None)
    else failure(name, es.map(formatError).mkString("\n"))

  private def typeName[A](tag: WeakTypeTag[A]): String =
    packagePrefix.replaceAllIn(tag.tpe.toString, "")

  private def formatError(e: AlignmentError): String =
    formatError(e.msg)

  private def formatError(s: String): String =
    (wrap(80)(s) match {
      case s :: ss => (s"${Console.RED}  - $s${Console.RESET}") :: ss.map(s => s"${Console.RED}    $s${Console.RESET}")
      case Nil => Nil
    }).mkString("\n")

  def formatSql(sql: String): String = {
    // val line = sql.lines.dropWhile(_.trim.isEmpty).map(s => s"  \033[37m$s${Console.RESET}").mkString("")
    // s"\n$line\n"
    sql
  }

  def failure(name: String, desc: String): String =
    s"${Console.RED}  ✕ ${Console.RESET}$name\n" + desc.lines.map(s => s"  $s").mkString("\n")

  def success(name: String, desc: Option[String]): String =
    s"${Console.GREEN}  ✓ ${Console.RESET}$name\n" + desc.mkString("\n")
}