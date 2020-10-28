# BM - Product Catalog Spark Job

The idea of this repo is two show two alternatives to ingest and process files in a big data environment. To do so, 
we take the [ProductCatalog Public Dataset](https://backmarket-data-jobs.s3-eu-west-1.amazonaws.com/data/product_catalog.csv) 
that contains 1000 rows.

We present two different approaches that can be used depending on the nature of the data. The first solution is using 
the native CSV library from Apache Spark, and the second is using the the textFile method and parsing row by row.

Both solutions are enclosed into a Spark job that can be run independently (locally).

- TransformaterCSVJobLocal (Using CSV method)
- TransformaterTextJobLocal (Using textFile)

## Structure
In the `src` folder it is possible to find the package `com.murdix.bm` containing all the code. 

In `jobs.local` there are the two (Scala) Objects with a Main.

In `entities` we define the class that will represent each line of the Product Catalog. This is the core of using
Dataset to apply transformations on a structured and strongly typed distributed collection.

## Output
The output of the job is stored in a columnar parquet file. This makes easier the reading by only consuming the columns needed.
The folder `output/` contains the files of the local execution.

## Assumptions & discussion
As a typical business requirement, each product category is consider valid if there is an image into the column image.
This does not mean that the row is _complete_, because a row can contain an image but not a currency. The validation of 
each row can be done easily by updating the method `isValid` or adding new methods as `isComplete`  into the `ProductCatalog` 
entity (as it is shown).


## Compilation
As the project is written using Scala, the source code must be compiled in order to be executed by the JVM.

### Requirements
- [JDK 11](https://adoptopenjdk.net/): The Java runtime to execute the code 
- [SBT](https://www.scala-sbt.org/download.html): The interactive build tool to compile and execute the project

## Execution
To execute the code, just type in a terminal (depending on the job):
```bash
$ sbt "runMain com.murdix.bm.jobs.local.TransformaterCSVJobLocal"
```
or 

```bash
$ sbt "runMain com.murdix.bm.jobs.local.TransformaterTextJobLocal"
```

## Tests
To execute the tests, just type in a terminal:
```bash
$ sbt test
```

## Packaging
To execute (launch) a spark job in a cluster, first it needs to be packaged in a jar:
```bash
sbt clean package -J-Xmx3G -J-Xss2M
```
