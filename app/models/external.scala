package com.mfglabs
package models
package external

import org.http4s.Http4s.uri
import org.http4s.{ Request, Method, Headers, Header }
import java.time.Instant

object StravaRoutes {
  def activities(activitiesPerPage: Int)(token: Strava.Token)(after: Instant) =
    Request(
      Method.GET,
      uri("https://www.strava.com/api/v3/activities")
        .withQueryParam("per_page", activitiesPerPage)
        .withQueryParam("after", after.getEpochSecond),
      headers = Headers(Header("Authorization", s"Bearer ${token.value}"))
    )
}