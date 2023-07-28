
/*
 build.sbt adapted from https://github.com/pbassiner/sbt-multi-project-example/blob/master/build.sbt
*/


name := "bwhc-csv-mappings"
ThisBuild / organization := "de.bwhc"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version      := "1.1-SNAPSHOT"

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", "services", xs @ _*) => MergeStrategy.first
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

//-----------------------------------------------------------------------------
// PROJECTS
//-----------------------------------------------------------------------------

lazy val core = project
  .settings(
    name := "csv-mappings-core",
    settings,
    libraryDependencies ++= Seq(
      dependencies.scalatest,
      dependencies.bwhc_mtb_data_api,
      dependencies.bwhc_mtb_dto_generators,
      dependencies.hgnc_catalog_impl % Test,
      dependencies.icd_catalogs_impl % Test, 
      dependencies.med_catalog_impl % Test 
    )
  )


lazy val app =
  project.in(file("app"))
    .settings(
      settings,
      libraryDependencies ++= Seq(
        dependencies.hgnc_catalog_impl,
        dependencies.icd_catalogs_impl, 
        dependencies.med_catalog_impl 
      ),
      assembly / assemblyJarName := "bwhc-mtb-csv-converter.jar",
      assembly / mainClass := Some("de.bwhc.mtb.csv.app.Main")
    ) 
    .dependsOn(
      core
    )


//-----------------------------------------------------------------------------
// DEPENDENCIES
//-----------------------------------------------------------------------------

lazy val dependencies =
  new {
    val scalatest               = "org.scalatest"   %% "scalatest"               % "3.1.1" % Test
    val slf4j                   = "org.slf4j"       %  "slf4j-api"               % "1.7.32"
    val bwhc_mtb_data_api       = "de.bwhc"         %% "data-entry-service-api"  % "1.1-SNAPSHOT"
    val bwhc_mtb_dto_generators = "de.bwhc"         %% "mtb-dto-generators"      % "1.1-SNAPSHOT" % Test
    val hgnc_catalog_impl       = "de.bwhc"         %% "hgnc-impl"               % "1.0-SNAPSHOT"
    val icd_catalogs_impl       = "de.bwhc"         %% "icd-catalogs-impl"       % "1.0-SNAPSHOT"
    val med_catalog_impl        = "de.bwhc"         %% "medication-catalog-impl" % "1.0-SNAPSHOT"
  }


//-----------------------------------------------------------------------------
// SETTINGS
//-----------------------------------------------------------------------------

lazy val settings = commonSettings


lazy val compilerOptions = Seq(
  "-encoding", "utf8",
  "-unchecked",
  "-feature",
//  "-language:existentials",
//  "-language:higherKinds",
//  "-language:implicitConversions",
  "-language:postfixOps",
  "-Xfatal-warnings",
  "-deprecation",
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= 
    Seq("Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository") ++
    Resolver.sonatypeOssRepos("releases") ++
    Resolver.sonatypeOssRepos("snapshots")
)
