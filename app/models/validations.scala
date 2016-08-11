package com.mfglabs
package models

import play.api.libs.json.{ JsValue, JsString }
import jto.validation._
import shapeless.{ ::, HNil }
import java.time._
import java.time.format.DateTimeFormatter

trait LowPriorityDerivations {
  import shapeless.{ HList, Generic }

  implicit def hlistRule[I, H](implicit r: Rule[I, H]): Rule[I, H :: HNil] = r.map(_ :: HNil)

  implicit def anyvalRuleDerivation[I, N <: AnyVal, H <: HList](implicit gen: Generic.Aux[N, H], r: Rule[I, H]): Rule[I, N] =
    r.map(gen.from _)

  implicit def hlistWrite[O, H](implicit w: Write[H, O]): Write[H :: HNil, O] = w.contramap(_.head)

  implicit def anyvalWriteDerivation[O, N <: AnyVal, H <: HList](implicit gen: Generic.Aux[N, H], w: Write[H, O]): Write[N, O] =
    w.contramap(gen.to _)
}

object validations extends LowPriorityDerivations {

  implicit def localdateR[I](implicit r: Rule[I, String]): Rule[I, LocalDate] =
    r andThen Rule.fromMapping[String, LocalDate] { s =>
      scala.util.Try(LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE))
        .map(cats.data.Validated.Valid.apply)
        .getOrElse(cats.data.Validated.Invalid(Seq(ValidationError("error.expected.localdate.format", s))))
    }

  implicit def instantR[I](implicit r: Rule[I, String]): Rule[I, Instant] =
    r andThen Rule.fromMapping[String, Instant] { s =>
      scala.util.Try(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(s)))
        .map(cats.data.Validated.Valid.apply)
        .getOrElse(cats.data.Validated.Invalid(Seq(ValidationError("error.expected.instant.format", s))))
    }

  implicit def localdateW(implicit w: Write[String, JsValue]): Write[LocalDate, JsValue] =
    w.contramap(_.format(DateTimeFormatter.ISO_LOCAL_DATE))

  implicit def enumerationW[T <: Enumeration]: Write[T#Value, JsString] = Write(s => JsString(s.toString.capitalize))

  implicit def uuidW(implicit w: Write[String, JsValue]): Write[java.util.UUID, JsValue] = w.contramap(_.toString)

  def enumR[I, T <: Enumeration](t: T)(implicit r: Rule[I, String]) =
    r.flatMap { s =>
      Rule.fromMapping { _ =>
        scala.util.Try(t.withName(s.toLowerCase.capitalize))
          .map(cats.data.Validated.Valid.apply)
          .getOrElse(cats.data.Validated.Invalid(Seq(ValidationError(s"""error.unknow.value '$s' expected one of: ${t.values.toList.mkString(", ")}""", s))))
      }
    }
}