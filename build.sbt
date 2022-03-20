import sbt.Keys.libraryDependencies

ThisBuild / organization := "com.ankbot"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / publishTo := Some(MavenCache("local-maven",
  Path.userHome.asFile.toURI.toURL + ".m2/repository",
  file("/")))

/*val resolutionRepos = Seq(
  "mvnrepo" at "https://mvnrepository.com"
  ,"confluent" at "https://packages.confluent.io/maven/"
  ,"jcenter" at "https://jcenter.bintray.com/"
  ,"Artima Maven Repository" at "http://repo.artima.com/releases"
  ,Resolver.bintrayRepo("ovotech", "maven")
)*/

lazy val root = (project in file(".") withId "objectreader")
  .settings(
    name := "objectreader",
    inThisBuild(Seq(
      IntegrationTest / parallelExecution  := false,
      /*scalacOptions ++= Seq(
        "-deprecation",
        "-encoding", "UTF-8",
        "-feature",
        "-explaintypes",
        "-Xfatal-warnings"
      ),
      scmInfo := Some(
        ScmInfo(
          url("https://github.com/mazeboard/objectreader"),
          "https://github.com/mazeboard/objectreader.git"
        )
      ),*/
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      libraryDependencies += "org.json" % "json" % "20211205",
      libraryDependencies += "com.typesafe" % "config" % "1.4.2",
      libraryDependencies += "org.apache.spark" %% "spark-core" % "3.2.1" % Test withSources() withJavadoc(),
      //libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.11",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11" % "test"
    )),
    /*publishArtifact := false,
    publish := {},
    publishLocal := {}*/
  )
