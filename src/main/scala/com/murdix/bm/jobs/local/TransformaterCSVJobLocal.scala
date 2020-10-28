package com.murdix.bm.jobs.local

import com.murdix.bm.entities.ProductCatalog
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}
import java.io.File
import java.net.URL

import scala.sys.process._

object TransformaterCSVJobLocal extends App {
  implicit val spark: SparkSession = SparkSession.builder
    .appName("TransformaterCSVJobLocal")
    .config("spark.master", "local")
    .getOrCreate()

  import spark.implicits._

  /* If there is no argument, we take the default */
  val csvPath: String = if (args.length == 0) "src/main/resources/product_catalog.csv" else args(0)

  /* Download the file */
  new URL("https://backmarket-data-jobs.s3-eu-west-1.amazonaws.com/data/product_catalog.csv") #> new File(csvPath) !!

  /* We read the csv and parse it using spark */
  val allProductCatalog: Dataset[ProductCatalog] = spark.read
    .option("delimiter", ",")
    .option("quote", "\"")
    .option("header", "true")
    .option("inferSchema", "true")
    .csv(csvPath).as[ProductCatalog]

  // We save the dataset in parquet
  allProductCatalog
    .write
    .format("parquet")
    .option("compression", "snappy")
    .mode(SaveMode.Overwrite)
    .save("output/csv/products/")

  // Close the spark session
  spark.close
}
