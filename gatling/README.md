# Creating Activity with Gatling

Launch SBT and run `DemoSimulation`:

```
➭ sbt
> gatling:testOnly psugdemo.DemoSimulation
```

This simulation is:
* calling the `/status` endpoint every 5 seconds,
* stopping after 5 minutes.

