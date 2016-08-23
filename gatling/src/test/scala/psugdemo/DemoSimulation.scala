package psugdemo

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class DemoSimulation extends Simulation {

  val httpConf = http.baseURL("http://127.0.0.1:9000")

  val statusScenario = scenario("Status")
    .forever(
      pace(5 seconds)
        .exec(
          http("status")
            .get("/status")
            .check(status is 200)
        )
    )

  setUp(
    statusScenario.inject(
      atOnceUsers(1)
    )
  ).protocols(httpConf)
    .maxDuration(5 minutes)
    .assertions(global.successfulRequests.percent.is(100))
}
