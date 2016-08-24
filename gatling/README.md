# Creating Activity with Gatling

Launch SBT and run `DemoSimulation`:

```
➭ sbt
> gatling:testOnly psugdemo.DemoSimulation
```

This simulation:
* checks the `/status` endpoint every 5 seconds,
* loads users activies defined in `src/test/resources/tokens.csv',
* calls the `/activities` endpoint randomly for up to 10 users,
* stops after 5 minutes.

**Warning**: this simulation requires an empty `strava.activities` table to be successful.

