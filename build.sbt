/*
 * =========================================================================================
 * Copyright © 2013-2017 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

val kamonTestKit        = "io.kamon" %% "kamon-testkit"         % "1.0.0"
val kamonAkka24         = "io.kamon" %% "kamon-akka-2.4"        % "1.0.0"
val kamonAkka25         = "io.kamon" %% "kamon-akka-2.5"        % "1.0.2"
val akkaHttpJson        = "de.heikoseeberger" %% "akka-http-json4s" % "1.18.1"
val json4sNative        = "org.json4s" %% "json4s-native" % "3.5.3"

val http24         = "com.typesafe.akka" %% "akka-http"          % "10.0.11"
val stream24       = "com.typesafe.akka" %% "akka-stream"        % "2.4.20"
val httpTestKit24  = "com.typesafe.akka" %% "akka-http-testkit"  % "10.0.11"

val http25         = "com.typesafe.akka" %% "akka-http"          % "10.0.11"
val stream25       = "com.typesafe.akka" %% "akka-stream"        % "2.5.8"
val httpTestKit25  = "com.typesafe.akka" %% "akka-http-testkit"  % "10.0.11"

val kanelaScalaExtension  = "io.kamon"  %%  "kanela-scala-extension"  % "0.0.10"


lazy val baseSettings = Seq(
  scalaSource in Compile := baseDirectory.value / ".." / ".." / "kamon-akka-http"/ "src" / "main" / "scala",
  scalaSource in Test    := baseDirectory.value / ".." / ".." / "kamon-akka-http"/ "src" / "test" / "scala",
  javaSource in Compile  := baseDirectory.value / ".." / ".." / "kamon-akka-http"/ "src" / "main" / "java",
  resourceDirectory in Compile := file(".") / "kamon-akka-http" / "src" / "main" / "resources",
  unmanagedClasspath in Test ++= Seq(
    baseDirectory.value / ".." / ".." / "kamon-akka-http" / "src" / "main" / "resources",
    baseDirectory.value / ".." / ".." / "kamon-akka-http" / "src" / "test" / "resources"
  )
)

lazy val root = (project in file("."))
  .aggregate(kamonAkkaHttp24, kamonAkkaHttp25, kamonAkkaHttpPlayground)
  .settings(noPublishing: _*)
  .settings(Seq(crossScalaVersions := Seq("2.11.8", "2.12.1")))

lazy val kamonAkkaHttp24 = Project("kamon-akka-http-24", file("target/kamon-akka-http-24"))
  .settings(name := "kamon-akka-http-24", moduleName := "kamon-akka-http-2.4", bintrayPackage := "kamon-akka-http")
  .enablePlugins(JavaAgent)
  .settings(javaAgents ++= resolveAgent)
  .settings(baseSettings: _*)
  .settings(Seq(
    scalaVersion := "2.12.1",
    crossScalaVersions := Seq("2.11.8", "2.12.1")))
  .settings(libraryDependencies ++=
    compileScope(http24, stream24, kamonAkka24, kanelaScalaExtension) ++
    testScope(httpTestKit24, scalatest, slf4jApi, logbackClassic, kamonTestKit, akkaHttpJson, json4sNative))

lazy val kamonAkkaHttp25 = Project("kamon-akka-http-25", file("target/kamon-akka-http-25"))
  .settings(name := "kamon-akka-http-25", moduleName := "kamon-akka-http-2.5", bintrayPackage := "kamon-akka-http")
  .enablePlugins(JavaAgent)
  .settings(javaAgents ++= resolveAgent)
  .settings(baseSettings: _*)
  .settings(Seq(
    scalaVersion := "2.12.1",
    crossScalaVersions := Seq("2.11.8", "2.12.1")))
  .settings(libraryDependencies ++=
    compileScope(http25, stream25, kamonAkka25, kanelaScalaExtension) ++
    testScope(httpTestKit25, scalatest, slf4jApi, logbackClassic, kamonTestKit, akkaHttpJson, json4sNative))

lazy val kamonAkkaHttpPlayground = Project("kamon-akka-http-playground", file("kamon-akka-http-playground"))
  .dependsOn(kamonAkkaHttp25)
  .settings(Seq(
    scalaVersion := "2.12.1",
    crossScalaVersions := Seq("2.11.8", "2.12.1")))
  .enablePlugins(JavaAgent)
  .settings(javaAgents ++= resolveAgent)
  .settings(noPublishing: _*)
  .settings(settingsForPlayground: _*)
  .settings(libraryDependencies ++=
    compileScope(http25) ++
    testScope(httpTestKit25, scalatest, slf4jApi, slf4jnop))


lazy val settingsForPlayground: Seq[Setting[_]] = Seq(
  connectInput in run := true,
  cancelable in Global := true
)

import sbt.Tests._
def singleTestPerJvm(tests: Seq[TestDefinition], jvmSettings: Seq[String]): Seq[Group] =
  tests map { test =>
    Group(
      name = test.name,
      tests = Seq(test),
      runPolicy = SubProcess(ForkOptions(runJVMOptions = jvmSettings)))
  }

def resolveAgent: Seq[ModuleID] = {
  val agent = Option(System.getProperty("agent")).getOrElse("aspectj")
  if(agent.equalsIgnoreCase("kanela"))
    Seq("org.aspectj" % "aspectjweaver" % "1.9.1" % "compile", "io.kamon" % "kanela-agent" % "0.0.300" % "compile;test")
  else
    Seq("org.aspectj" % "aspectjweaver" % "1.9.1" % "compile;test", "io.kamon" % "kanela-agent" % "0.0.300" % "compile")
}
