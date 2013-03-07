resolvers ++= Seq(
  "Maven Central"          at "http://repo1.maven.org/maven2/",
  Classpaths.typesafeResolver,
  "zeebox-nexus"           at "http://nexus.zeebox.com:8080/nexus/content/groups/public/",
  "sbt-idea-repo"          at "http://mpeltonen.github.com/maven/",
  "gseitz@github"          at "http://gseitz.github.com/maven/",
  "scct-github-repository" at "http://mtkopone.github.com/scct/maven-repo"
)

libraryDependencies ++= Seq(
  "org.jacoco"         % "org.jacoco.core"   % "0.5.9.201207300726" artifacts(Artifact("org.jacoco.core", "jar", "jar")),
  "org.jacoco"         % "org.jacoco.report" % "0.5.9.201207300726" artifacts(Artifact("org.jacoco.report", "jar", "jar")),
  "com.github.siasia" %% "xsbt-web-plugin"   % "0.12.0-0.2.11.1"
)

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.0.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.1.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.6.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.6")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "1.2.4")

addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.8")


