import sbt._
import scala._

object Resolvers {
  lazy val localm2 = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
  lazy val mvncentral = "Maven Central" at "http://repo1.maven.org/maven2/"
  lazy val typesafe = Classpaths.typesafeReleases
  //"SSH port forward" at "http://localhost:6969/nexus/content/repositories/releases",
  lazy val ossreleases = "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"
  lazy val osssnapshots = "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

  lazy val zeeboxReleases   = "releases"      at "http://nexus.zeebox.com:8080/nexus/content/repositories/releases"
  lazy val zeeboxSnapshots  = "snapshots"     at "http://nexus.zeebox.com:8080/nexus/content/repositories/snapshots"


  lazy val all = Seq(localm2, mvncentral, typesafe, ossreleases, osssnapshots)
}

object Dependencies {
  val core = Core.all
  val test = Test.all

  object Core {
    lazy val scalaz =    "org.scalaz"                 %% "scalaz-core"                   % "6.0.4"
    lazy val shapeless = "com.chuusai"                %% "shapeless"                     % "1.2.3"
    lazy val slf4j =     "org.slf4j"                  %  "slf4j-api"                     % "1.6.4"

    lazy val all = Seq(scalaz, shapeless, slf4j)
  }

  object Test {
    lazy val logback =   "ch.qos.logback"             %  "logback-classic"               % "1.0.0"   % "test"
    lazy val groovy =    "org.codehaus.groovy"        %  "groovy-all"                    % "1.7.6"   % "test"
    lazy val janino =    "janino"                     %  "janino"                        % "2.5.10"  % "test"
    lazy val specs2 =    "org.specs2"                 %% "specs2"                        % "1.13"    % "test"
    lazy val mockito =   "org.mockito"                %  "mockito-core"                  % "1.9.0"   % "test"
    lazy val hamcrest =  "org.hamcrest"               %  "hamcrest-core"                 % "1.3"     % "test"
    lazy val junit =     "junit"                      % "junit"                          % "4.7"     % "test" //for xml output

    lazy val all = Seq(logback, groovy, janino, specs2, mockito, hamcrest)

  }

}
