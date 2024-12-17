import org.openurp.parent.Dependencies.*
import org.openurp.parent.Settings.*

ThisBuild / organization := "org.openurp.degree.thesis"
ThisBuild / version := "0.0.4-SNAPSHOT"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/openurp/thesis"),
    "scm:git@github.com:openurp/thesis.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "chaostone",
    name = "Tihua Duan",
    email = "duantihua@gmail.com",
    url = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "The OpenURP Thesis Webapp"
ThisBuild / homepage := Some(url("http://openurp.github.io/thesis/index.html"))
ThisBuild / resolvers += Resolver.mavenLocal

val apiVer = "0.41.14"
val starterVer = "0.3.50"
val eduCoreVer = "0.3.7"
val openurp_base_api = "org.openurp.base" % "openurp-base-api" % apiVer
val openurp_degree_api = "org.openurp.degree" % "openurp-degree-api" % apiVer
val openurp_stater_web = "org.openurp.starter" % "openurp-starter-web" % starterVer
val openurp_edu_core = "org.openurp.edu" % "openurp-edu-core" % eduCoreVer

val itextpdf = "com.itextpdf" % "itextpdf" % "5.5.13.3"
val itext_asian = "com.itextpdf" % "itext-asian" % "5.2.0"
lazy val root = (project in file("."))
  .enablePlugins(WarPlugin, UndertowPlugin, TomcatPlugin)
  .settings(
    name := "openurp-degree-thesis-webapp",
    common,
    libraryDependencies ++= Seq(beangle_ems_app,beangle_webmvc),
    libraryDependencies ++= Seq(openurp_base_api, openurp_degree_api, openurp_stater_web,openurp_edu_core),
    libraryDependencies ++= Seq(itextpdf, itext_asian),
    libraryDependencies ++= Seq(beangle_doc_docx),
    libraryDependencies ++= Seq(logback_classic)
  )

