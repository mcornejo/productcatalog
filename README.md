# BM - Product Catalog Spark Job

The idea of this repo is two show three alternatives to ingest and process files in a big data environment. To do so, 
we take the [ProductCatalog Public Dataset](https://backmarket-data-jobs.s3-eu-west-1.amazonaws.com/data/product_catalog.csv) 
that contains 1000 rows.

We present two different approaches to, run locally, that can be used depending on the nature of the data. The first solution is using 
the native CSV library from Apache Spark, and the second is using the textFile method and parsing row by row.

Both solutions are enclosed into a Spark job that can be run independently (locally).

- [TransformaterCSVJobLocal.scala](https://github.com/mcornejo/productcatalog/blob/main/src/main/scala/com/murdix/bm/jobs/local/TransformaterCSVJobLocal.scala)` (Using CSV method)
- [TransformaterTextJobLocal.scala](https://github.com/mcornejo/productcatalog/blob/main/src/main/scala/com/murdix/bm/jobs/local/TransformaterTextJobLocal.scala) (Using textFile)

A third solution is also given [TransformaterCSVJob.scala](https://github.com/mcornejo/productcatalog/blob/main/src/main/scala/com/murdix/bm/jobs/cluster/TransformaterCSVJob.scala) that is using the CSV method, with the extensions of support of s3a
filesystem. This is an example of a _real_ job that can be run in a real hadoop cluster (cf. [Running in an EMR cluster](#running-in-an-EMR-cluster)).
This job supports reading multiple files in parallel, and the execution of the job in parallel as well. 

## Structure
In the `src` folder it is possible to find the package `com.murdix.bm` containing all the code. 

In `entities` we define the class that will represent each line of the Product Catalog. This is the core of using
Dataset to apply transformations on a structured and strongly typed distributed collection.

In `jobs.local` there are the two (Scala) Objects with a Main.

In `jobs.cluster` there is the _real_ job that can be executed in a cluster. This job requires some configuration 
(like adding real paths for reading and writing).

## Output
The output of the job is stored in a columnar parquet file. This makes easier the reading by only consuming the columns needed.
The folder `output/` contains the files of the local execution.

## Assumptions & discussion
### Business rules
As a typical business requirement, each product category is consider valid if there is an image into the column image.
This does not mean that the row is _complete_, because a row can contain an image but not a currency. The validation of 
each row can be done easily by updating the method `isValid` or adding new methods as `isComplete` into the `ProductCatalog` 
entity (cf `com.murdix.bm.entities.ProductCatalog`). Using this assumption, the Spark job split the 100 lines into: 
750 valid lines and 260 invalid lines. 

### Scala as programming language
Scala was chosen as programming language due to its native and natural integration in Spark and in the big data world.
By adding a type system into a job, it increases the quality of the code, reducing bugs that can be detected at compilation 
time and more importantly it creates an interoperability layer into the different jobs. Usually the output of a job is the input
of another job, and it is possible to keep a clean pipeline by using strict types. Immutability is another factor why Scala
y a better option to write a Spark job. 

Immutability allows parallelism and (real) multithreading. Scala allows parallelism and concurrency out of the box. 
By using immutable data structures, we can profit of using multiple cores in a single machine to increase speed and performance. 
We also reduce stack calls by not needing to go through Apache Arrow for interoperability. Spark is written in Scala and
Apache Hadoop is written in Java and executes in a JVM environment. Scala seems the natural option.

### Interoperability
One of the benefits of using Scala and Datasets is the interoperability. Using strongly typed classes works as pipes connections, 
they ensure compatibility and correctness all the downstream. By having typed classes, other jobs can use them to ensure they
are reading exactly the data needed with the type annotation, and in the same manner, they ensure the quality of the data
for the next step.

### Scalability
A _real_ job example was added to address the problem of multiple massive CSV files (cf. `com.murdix.bm.jobs.cluster` package). 
In this case, the number of repartitions must be carefully selected depending on the size of the cluster. Same principle for 
the partition so that it can reduce the access (hits) to the files.

This _real_ job is capable to read from S3 multiple CSV files in parallel and process them at the same time. Once the processing 
is finished it can store it in a parallel-fashion as well. One of the important details is not to exceed the number of request per second
that amazon [supports](https://aws.amazon.com/premiumsupport/knowledge-center/emr-s3-503-slow-down/). To avoid this exception, 
the data should be stored and partitioned in a smart way. 

A fine-tuning is needed to select all the parameters for optimal performance. The more balanced the files, the better performance
it can be obtained.

### Versioning
A small changelog was added to review easily the changes in the repo. Comments on the code is also given. The comments on the
commits are in the [Conventional Commits standard](https://www.conventionalcommits.org/en/v1.0.0/).


### Idempotency
The jobs are idempotent, which means if they are executed multiple times with the same input, they produce the same output. This 
is particularly important in a data environment due to the fact that jobs and pipelines often fail. A job should allow to be 
recomputed and producing the very same output. All of the jobs presented here have this property. 


***
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
We provided tests for the parser from textFile. To execute the tests, just type in a terminal:
```bash
$ sbt test
```

## Packaging
To execute (launch) a spark job in a cluster, first it needs to be packaged in a jar:
```bash
sbt clean package -J-Xmx3G -J-Xss2M
```

## Running in an EMR cluster
The usual procedure to run a Spark job in a cluster using EMR, first we compile the code, then we package it and
upload to S3. At the moment of the creation of the cluster we can point the path to the S3 bucket where the jar is contained.
EMR will run that job once the cluster is configured.

Assuming that the AWS-cli is configured:
- We compile and create the jar
- We upload the jar into S3
- We create the EMR cluster using 9 secondary nodes and 1 main node m3.xlarge.

```bash
sbt clean package -J-Xmx3G -J-Xss2M
aws s3 cp target/scala-2.12/productcatalog_2.12-0.1.jar.jar s3://backmarket-data-jobs/jobs/cluster_job.jar
aws emr create-cluster --applications Name=Ganglia Name=Spark Name=Zeppelin --ec2-attributes '{"KeyName":"secret-key","InstanceProfile":"EMR_EC2_DefaultRole","SubnetId":"subnet-ID","EmrManagedSlaveSecurityGroup":"sg-ID","EmrManagedMasterSecurityGroup":"sg-ID"}' --release-label emr-5.17.0 --log-uri 's3n://backmarket-data-jobs/logs/cluster_job/' --steps '[{"Args":["spark-submit","--deploy-mode","cluster","--master","yarn","--executor-memory","9g","--class","com.murdix.bm.jobs.cluster.TransformaterCSVJob","s3://backmarket-data-jobs/jobs/cluster_job.jar"],"Type":"CUSTOM_JAR","ActionOnFailure":"TERMINATE_CLUSTER","Jar":"command-runner.jar","Properties":"=","Name":"Spark application"}]' --instance-groups '[{"InstanceCount":9,"InstanceGroupType":"CORE","InstanceType":"m3.xlarge","Name":"Core Instance Group"},{"InstanceCount":1,"InstanceGroupType":"MASTER","InstanceType":"m3.xlarge","Name":"Master Instance Group"}]' --configurations '[{"Classification":"spark","Properties":{"maximizeResourceAllocation":"true"},"Configurations":[]}]' --auto-terminate --ebs-root-volume-size 10 --service-role EMR_DefaultRole --enable-debugging --name 'cluster-costumer-segments-tagged-cisbio' --scale-down-behavior TERMINATE_AT_TASK_COMPLETION --region eu-west-1
```
