# HW3: Monte Carlo Simulation
### Description: gain experience with the Spark computational model in AWS cloud datacenter.
This is a homework assignment for CS441 at the University of Illinois at Chicago.
This project utilizes the open-source [Apache Spark 2.4.4](https://spark.apache.org) cluster-computing framework to program clusters with implicit data parallelism on the [Amazon EMR](https://aws.amazon.com/emr) cloud data platform.


## Background
This project aims to predict stock portfolio losses using the Monte Carlo simulation. This Spark application takes advantage of *resilient distributed datasets (RDDs)*, immutable read-only, fault-tolerant distributed collections of objects that are divided into logical partitions and may be computed on different nodes of the cluster. This allows for efficient parallel processing of the data, which would otherwise take much longer to process. The RDDs are created in two ways—either by *referencing* a dataset stored in the [Hadoop Distributed File System (HDFS)](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html), as is the case when the application uses the change.csv and portfolio.csv datasets, or by *parallelizing* an existing collection in the driver program, which is what occurs when calling the `runSession` method during each run. When the results RDD is obtained, the mean, standard deviation and 5th, 25th, 50th, 75th and 95th percentiles are computed for the funds values. When the application is run locally, the master is set to run with as many worker threads as logical cores on the machine using the `local[*]` tag.

## Running
To successfully run this project, access to the [Amazon EMR](https://aws.amazon.com/emr) cloud data platform, [IntelliJ IDEA](https://www.jetbrains.com/idea), [sbt](https://docs.scala-lang.org/getting-started/sbt-track/getting-started-with-scala-and-sbt-on-the-command-line.html) and [Java 8 JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html) (version 1.8 or higher) are required.

1. From the project root directory, enter the following Terminal command to run all tests and assemble the JAR:

        sbt clean assembly

2. Upload the JAR and input files to an [Amazon S3](https://docs.aws.amazon.com/s3) bucket.

3. Open the Amazon EMR service and create a new cluster.

4. Go to advanced options. Uncheck all but the following services. Then, click the `Next` button.

        Hadoop 2.8.5
        Spark 2.4.4

5. Keep the default hardware configuration and click the `Next` button.

6. Choose the desired S3 bucket as the logging folder.

7. Keep the default security options and click `Create cluster`.

8. Click the `Steps` tab. Then, click the `Add step` button.

9. Select `Spark application` as the step type.

10. Enter the following Spark-submit options to start the application with the correct class.

        --class MonteCarloSim

11. Select the uploaded JAR from the s3 bucket as the application location.

12. Enter the following command-line arguments. Then, click the `Add` button.

        aws

13. Allow the cluster to start and run the Spark application.

14. Once completed, access the `results.txt` file from the S3 bucket.

To view these steps visually, please view the following [video](https://youtu.be/839ZA6zJOWM).

[![YouTube](https://github.com/mashy426/CS441-Monte-Carlo/blob/master/images/video.png)](https://youtu.be/839ZA6zJOWM)


## Tests
This project includes 14 unit tests based on the [ScalaTest](http://www.scalatest.org) testing framework, which are located in the project's `test/scala` directory and include app configuration and FloatArrayWritable creation tests.
The tests will run automatically when the JAR is assembled. However, if you would like to run them again, simply `cd` into the project root directory and enter the following command: `sbt test`.


## Monte Carlo
The Monte Carlo simulation in this project consists of 10,000 runs and 500 partitions. This is accomplished using the RDD parallelize function. In each run, the following tasks are implemented.

1. The historical stocks data is in the form of percent change of the close price for each day of the companies Google ([GOOGL](https://www.google.com/search?q=GOOGL)), Amazon ([AMZN](https://www.google.com/search?q=AMZN)), Facebook ([FB](https://www.google.com/search?q=FB)), Apple ([AAPL](https://www.google.com/search?q=AAPL)) and Microsoft ([MSFT](https://www.google.com/search?q=MSFT)), ranging from May 2012 to the present. The data is compiled using the `download.py` Python script that uses the [Alpha Vantage API](https://www.alphavantage.co) located in the `src/main/data` directory, the result of which is stored in `src/main/data/change.csv`.
2. The initial portfolio used in each simulation is consistent and consists of ticker symbols and the funds allocated for each in USD. This is located in `src/main/resources/portfolio.csv`.
3. In each simulation run in parallel, gains and losses are computed daily for each investment that is part of the portfolio. The subset of values is shuffled to mimic market forces. These gains and losses are recorded in the portfolio.
4. For each day in which there is a net gain in the portfolio, a decision is made to buy another stock. The stock is selected in random based on its performance for that day. Only stocks that had a net positive change in close price for the day are considered for investment.
5. For each day in which there is a net loss in the portfolio, a decision is made to possibly sell the stock that had the most negative performance that day. In this case, the investment of the stock that is sold is transferred to the stock that performed most exceptionally that day. If no such stock exists, or it is the same stock that is being considered for sale, then the buying and selling is simply not performed.
6. This process repeats itself until all the values in the shuffled subset are exhausted.


## Results
The results of the Spark application can be obtained using the [Amazon S3](https://docs.aws.amazon.com/s3) portal. From the S3 bucket directory, select `results.txt` and click the `Download` button.

![Amazon S3](https://github.com/mashy426/CS441-Monte-Carlo/blob/master/images/s3results.png)


The following are a sample of results obtained from running the simulation on Amazon EMR.
```
Mean:             $7113.54
Std deviation:    5335.372
5th  percentile:  $1572.98
25th percentile:  $2663.25
50th percentile:  $5292.40
75th percentile:  $10518.66
95th percentile:  $18186.97
```

As can be observed, the mean investment at the end of the Monte Carlo simulation (spanning ~7 years) have increased sharply from the initial $1,250 investment recorded in `portfolio.csv`. The funds vary based on the buying and selling prescribed by the algorithm that takes place through each run of the simulation. The overall takeaway is that the funds tend to gain over time.

To view images and analysis, see `Documentation.pdf` located in the project root directory.
