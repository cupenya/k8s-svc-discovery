import ReleaseTransformations._
import com.typesafe.sbt.packager.docker.Cmd

name          := """k8s-svc-discovery"""
organization  := "com.github.cupenya"
scalaVersion  := "2.11.8"
scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

resolvers += Resolver.jcenterRepo

libraryDependencies ++= {
  val akkaV            = "2.4.10"
  val ficusV           = "1.2.4"
  val scalaTestV       = "3.0.0-M15"
  val slf4sV           = "1.7.10"
  val logbackV         = "1.1.3"
  Seq(
    "com.typesafe.akka" %% "akka-http-core"                    % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental"            % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-slf4j"                        % akkaV,
    "org.slf4s"         %% "slf4s-api"                         % slf4sV,
    "ch.qos.logback"    %  "logback-classic"                   % logbackV,
    "org.scalatest"     %% "scalatest"                         % scalaTestV       % Test,
    "com.typesafe.akka" %% "akka-http-testkit"                 % akkaV            % Test
  )
}

val branch = "git rev-parse --abbrev-ref HEAD" !!
val cleanBranch = branch.toLowerCase.replaceAll(".*(cpy-[0-9]+).*", "$1").replaceAll("\\n", "").replaceAll("\\r", "")

enablePlugins(JavaServerAppPackaging)

publishArtifact in (Compile, packageDoc) := false

val shortCommit = ("git rev-parse --short HEAD" !!).replaceAll("\\n", "").replaceAll("\\r", "")


Revolver.settings

initialCommands := """|import akka.actor._
                      |import akka.pattern._
                      |import akka.util._
                      |import scala.concurrent._
                      |import scala.concurrent.duration._""".stripMargin

publishMavenStyle := true
publishArtifact in Test := false
releasePublishArtifactsAction := PgpKeys.publishSigned.value
pomIncludeRepository := { _ => false }
credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
publishTo := {
  val nexus = "https://test.cupenya.com/nexus/content/repositories"
  Some("snapshots" at nexus + "/snapshots")
}
pomExtra :=
  <url>https://github.com/cupenya/k8s-svc-discovery</url>
  <licenses>
    <license>
      <name>Apache-2.0</name>
      <url>http://opensource.org/licenses/Apache-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>https://github.com/cupenya/k8s-svc-discovery</url>
    <connection>scm:git:git@github.com:cupenya/k8s-svc-discovery.git</connection>
  </scm>
  <developers>
    <developer>
      <id>cupenya</id>
    <name>Jeroen Rosenberg</name>
      <url>https://github.com/cupenya/</url>
    </developer>
  </developers>

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _)),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges
)