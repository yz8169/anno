name := "anno"
 
version := "1.0" 
      
lazy val `anno` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
scalaVersion := "2.11.11"

libraryDependencies ++= Seq( jdbc , cache , ws , specs2 % Test,filters )

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

libraryDependencies += "com.typesafe.slick" % "slick-codegen_2.11" % "3.2.0"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.25"

libraryDependencies += "com.typesafe.play" % "play-slick_2.11" % "2.1.0"

libraryDependencies += "com.github.tototoshi" % "slick-joda-mapper_2.11" % "2.3.0"

libraryDependencies += "commons-io" % "commons-io" % "2.5"

libraryDependencies += "org.biojava" % "biojava-core" % "5.0.0"

libraryDependencies += "org.biojava" % "biojava-structure" % "5.0.0"

libraryDependencies += "org.zeroturnaround" % "zt-zip" % "1.11"


excludeDependencies += "org.apache.logging.log4j" % "log4j-slf4j-impl"









      