val ScalaVersion = "2.13.9"

val AkkaVersion = "2.6.20"
val LogbackVersion = "1.2.3"
val ScalaTest = "3.1.4"

lazy val chapter02 = project
  .in(file("chapter02"))
  .settings(
    scalaVersion := ScalaVersion,
    libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
        "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
        "ch.qos.logback" % "logback-classic" % LogbackVersion,
        "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
        "org.scalatest" %% "scalatest" % ScalaTest % Test))

lazy val chapter03 = project
  .in(file("chapter03"))
  .settings(
    scalaVersion := ScalaVersion,
    libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
        "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
        "ch.qos.logback" % "logback-classic" % LogbackVersion,
        "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
        "org.scalatest" %% "scalatest" % ScalaTest % Test))

lazy val chapter04 = project
  .in(file("chapter04"))
  .settings(
    scalaVersion := ScalaVersion,
    libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
        "ch.qos.logback" % "logback-classic" % LogbackVersion,
        "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
        "org.scalatest" %% "scalatest" % ScalaTest % Test))

lazy val chapter05 = project
  .in(file("chapter05"))
  .settings(
    scalaVersion := ScalaVersion,
    libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
        "ch.qos.logback" % "logback-classic" % LogbackVersion,
        "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
        "org.scalatest" %% "scalatest" % ScalaTest % Test))

lazy val chapter06 = project
  .in(file("chapter06"))
  .settings(
    scalaVersion := ScalaVersion,
    libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
        "ch.qos.logback" % "logback-classic" % LogbackVersion,
        "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
        "org.scalatest" %% "scalatest" % ScalaTest % Test))
