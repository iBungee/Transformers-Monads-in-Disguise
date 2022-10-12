ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

libraryDependencies in ThisBuild ++= Seq(
  "org.scalaz" %%  "scalaz-core" % "7.3.6",
  //compilerPlugin("org.typelevel" % "kind-projector" % "0.12.0" cross CrossVersion.full),
)



lazy val root = (project in file("."))
  .settings(
    name := "Transformers Monads in Disguise"
  )
