package psugdemo

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class DemoSimulation extends Simulation {

  val httpConf = http.baseURL("http://127.0.0.1:9000")

  val tokens = csv("tokens.csv").records

  val statusScenario = scenario("Status")
    .forever(
      pace(5 seconds)
        .exec(
          http("status")
            .get("/status")
            .check(status is 200)
        )
    )

  val activitiesScenario = scenario("Activities")
    .forever(
      pace(5 seconds, 15 seconds)
        .exec(
          http("activities")
            .get("/activities")
            .check(status is 200)
        )
    )

  val loadScenario = scenario("Load")
    .foreach(tokens, "token") {
      exec(flattenMapIntoAttributes("${token}"))
        .exec(
          http("load")
            .post("/load")
            .queryParam("token", "${token}")
            .check(status is 204)
        ).pause(2 seconds)
    }

  setUp(
    statusScenario.inject(
      atOnceUsers(1)
    ),
    loadScenario.inject(
      atOnceUsers(1)
    ),
    activitiesScenario.inject(
      rampUsers(10) over (30 seconds)
    )
  ).protocols(httpConf)
    .maxDuration(5 minutes)
    .assertions(global.successfulRequests.percent.is(100))
}
