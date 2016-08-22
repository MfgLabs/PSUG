package com.mfglabs
package models

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
  implicit def instantR[I](implicit r: Rule[I, String]): Rule[I, Instant] =
    r andThen Rule.fromMapping[String, Instant] { s =>
      scala.util.Try(Instant.from(DateTimeFormatter.ISO_INSTANT.parse(s)))
        .map(cats.data.Validated.Valid.apply)
        .getOrElse(cats.data.Validated.Invalid(Seq(ValidationError("error.expected.instant.format", s))))
    }
}