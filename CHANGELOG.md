Changelog for BM-ProductCatalog

## 1.0
* feature: Read and Write: First version, with basic support to download and read CSV / text files.

## 2.0
* feature: Parquet: Added support to use parquet files (compressed using snappy).
* feature: Parser: Added support to parse text files into a ProductCatalog object.
 
# 3.0
* feature: Clean files: Added support to split the text files into valid and invalid, storing both in parquet.

# 4.0
* feature: Cluster execution: Added an example of how to run the one job using a cluster and reading multiple files from
a S3 bucket. 