import sbt._
import com.github.siasia._
import Keys._
import sbt.Package._
import sbtrelease._
import sbtrelease.ReleasePlugin._
import net.virtualvoid.sbt.graph.Plugin.graphSettings
import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleaseStateTransformations._
import aether.Aether._


object MacroflectionBuild extends Build {
  import BuildSettings._

  lazy val buildSettings = Seq(
    organization := "com.github.kevinwright.macroflection",
    scalaVersion := "2.10.1",
    scalacOptions := Seq("-feature", "-deprecation", "-unchecked", "-Xlint", "-Yrangepos", "-encoding", "utf8"),
    scalacOptions in (console) += "-Yrangepos"
  )

  lazy val root = Project(id = "root", base = file("."))
    .aggregate(kernel, tests)
    .settings(commonSettings : _*)

  lazy val kernel = Project(id = "kernel", base = file("kernel"))
    .configs(IntegrationTest)
    .settings(commonSettings : _*)

  lazy val tests = Project(id = "tests", base = file("tests"))
    .dependsOn(kernel)
    .configs(IntegrationTest)
    .settings(commonSettings : _*)



  lazy val commonSettings = Defaults.defaultSettings ++
    sbtPromptSettings ++
    buildSettings ++
    graphSettings ++
    releaseSettings ++
    Defaults.itSettings ++
    publishSettings ++
    releaseSettings ++
    packagingSettings ++
    aetherSettings ++
    aetherPublishSettings ++
    miscSettings

  lazy val miscSettings = Seq(
    resolvers ++= Resolvers.all,
    ivyXML := ivyDeps,
    libraryDependencies ++= Dependencies.core,
    libraryDependencies ++= Dependencies.test,
    libraryDependencies <++= (scalaVersion)(sv =>
      Seq(
        "org.scala-lang" % "scala-reflect" % sv,
        "org.scala-lang" % "scala-compiler" % sv
      )
    ),
    testOptions in Test += Tests.Argument("console") //, "junitxml")
  )

  lazy val publishSettings: Seq[Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / "zeebox.credentials"),
    publishMavenStyle := true,
    publishTo <<= version((v: String) =>
      Some(if (v.trim endsWith "SNAPSHOT") Resolvers.zeeboxSnapshots else Resolvers.zeeboxReleases)
    )
  )

  lazy val releaseSettings = Seq(
    releaseVersion := { ver => optEnv("GO_PIPELINE_COUNTER").getOrElse(versionFormatError) },
    nextVersion    := { ver => optEnv("GO_PIPELINE_COUNTER").getOrElse(versionFormatError) },
    releaseProcess := Seq[ReleaseStep](
      //checkSnapshotDependencies,
      inquireVersions,
      //runTest,
      setReleaseVersion,
      commitReleaseVersion, // performs the initial git checks
      tagRelease,
      publishArtifacts     // checks whether `publishTo` is properly set up
      //setNextVersion,
      //commitNextVersion
      //pushChanges           // also checks that an upstream branch is properly configured
    )
  )

  lazy val packagingSettings = Seq(
    packageOptions <<= (Keys.version, Keys.name, Keys.artifact) map {
      (version: String, name: String, artifact: Artifact) =>
        Seq(ManifestAttributes(
          "Implementation-Vendor" -> "kevinwright",
          "Implementation-Title" -> "macroflection",
          "Version" -> version,
          "Build-Number" -> optEnv("GO_PIPELINE_COUNTER").getOrElse("n/a"),
          "Group-Id" -> name,
          "Artifact-Id" -> artifact.name,
          "Git-SHA1" -> Git.hash,
          "Git-Branch" -> Git.branch,
          "Built-By" -> "Oompa-Loompas",
          "Build-Jdk" -> prop("java.version"),
          "Built-When" -> (new java.util.Date).toString,
          "Build-Machine" -> java.net.InetAddress.getLocalHost.getHostName
        )
      )
    }
  )

  val ivyDeps = {
    <dependencies>
      <!-- commons logging is evil. It does bad, bad things to the classpath and must die. We use slf4j instead -->
        <exclude module="commons-logging"/>
      <!-- Akka dependencies must be excluded if transitivly included,
           replaced with corresponding atmos-akka-xxx -->
      
        <!--
        <exclude module="akka-actor"/>
        <exclude module="akka-remote"/>
        <exclude module="akka-slf4j"/>
        <exclude module="slf4j-simple"/>
        -->
      
      <!-- Flume dependencies can be excluded if Flume isn't used -->
        <exclude module="flume"/>
        <exclude module="googleColl"/>
        <exclude module="libthrift"/>
        <exclude module="hadoop-core"/>
    </dependencies>
  }
}
