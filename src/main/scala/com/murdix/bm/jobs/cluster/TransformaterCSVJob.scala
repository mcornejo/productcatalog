package com.murdix.bm.jobs.cluster

import com.murdix.bm.entities.ProductCatalog
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}

object TransformaterCSVJob extends App {
  implicit val spark: SparkSession = SparkSession.builder
    .appName("TransformaterCSVJob")
    .getOrCreate()

  /* Not needed when running in an EMR cluster */
  spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", "awsaccesskey value")
  spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", "aws secretkey value")
  spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "s3-eu-west1.amazonaws.com")

  import spark.implicits._

  /* If there is no argument, we take use this as default.
  *
  * When running in a cluster, to increase the throughput, each node should read a file and process it,
  * in order to do so, we can call read directly from a s3 bucket and using a * to signal all the files in that bucket.
  *  */
  val csvPath: String = if (args.length == 0) "s3a://backmarket-data-jobs/data/*" else args(0)

  val allProductCatalog: Dataset[ProductCatalog] = spark.read
    .option("delimiter", ",")
    .option("quote", "\"")
    .option("header", "true")
    .option("inferSchema", "true")
    .csv(csvPath).as[ProductCatalog]

  val validProducts: Dataset[ProductCatalog] = allProductCatalog.filter(_.isValid)
  val invalidProducts: Dataset[ProductCatalog] = allProductCatalog.filter(!_.isValid)


  /* Each node will process each file and do a repartition (random in this case) and will store the data using a
  partition key of brand and year_release */

  validProducts
    .repartition(10)
    .write
    .partitionBy(
      "brand",
      "year_release"
    )
    .format("parquet")
    .option("compression", "snappy")
    .mode(SaveMode.Overwrite)
    .save("s3a://backmarket-data-jobs/products/valid")

  invalidProducts
    .repartition(10)
    .write
    .partitionBy(
      "brand",
      "year_release"
    )
    .format("parquet")
    .option("compression", "snappy")
    .mode(SaveMode.Overwrite)
    .save("s3a://backmarket-data-jobs/products/invalid")

  // Close the spark session
  spark.close
}
