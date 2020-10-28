package com.murdix.bm.jobs.local

import org.apache.spark.sql.{Dataset, SparkSession}
import java.io.File
import java.net.URL
import scala.sys.process._

object TransformaterTextJobLocal extends App {
  implicit val spark: SparkSession = SparkSession.builder
    .appName("TransformaterTextJobLocal")
    .config("spark.master", "local")
    .getOrCreate()

  import spark.implicits._

  /* If there is no argument, we take the default */
  val csvPath: String = if (args.length == 0) "src/main/resources/product_catalog.csv" else args(0)

  /* Download the file */
  new URL("https://backmarket-data-jobs.s3-eu-west-1.amazonaws.com/data/product_catalog.csv") #> new File(csvPath) !!


  /* We read the csv and we read line by line */
  val rawFile: Dataset[String] = spark.read.textFile(csvPath)

  // We show the lines in the terminal
  rawFile.show()

  // Close the spark session
  spark.close
}
