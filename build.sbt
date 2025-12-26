val scala3Version = "3.3.7"

lazy val root = project
  .in(file("."))
  .settings(
    name := "job-scheduler-scala",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.6.3"
    ),
    scalacOptions += "-Wnonunit-statement",
    Compile / run / fork := true
  )
