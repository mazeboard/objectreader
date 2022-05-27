import sbt.Keys.libraryDependencies

ThisBuild / organization := "com.ankbot"
ThisBuild / version      := "1.0.0"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / publishTo := Some(MavenCache("local-maven",
  Path.userHome.asFile.toURI.toURL + ".m2/repository",
  file("/")))

publishConfiguration := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)

val foo: String = System.setProperty("LoggerLevel", "INFO")

lazy val root = (project in file(".") withId "objectreader")
  .settings(
    name := "objectreader",
    inThisBuild(Seq(
      IntegrationTest / parallelExecution  := false,
      resolvers += Resolver.mavenLocal,
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.json" % "json" % "20211205",
      "com.typesafe" % "config" % "1.4.2",
        "com.ankbot" %% "macros" % "1.1.4",
      "org.apache.spark" %% "spark-core" % "3.2.1" % Test withSources() withJavadoc(),
      "org.scalatest" %% "scalatest" % "3.2.11" % "test"
    )
  )))
