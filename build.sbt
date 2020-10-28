name := "productcatalog"

version := "0.1"

scalaVersion := "2.12.12"

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.1"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.1"

libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.3.0"

libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "3.3.0"

libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "3.3.0"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % "test"

enablePlugins(JavaAppPackaging)