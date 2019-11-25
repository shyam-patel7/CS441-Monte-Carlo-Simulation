# HW3: Spark Monte Carlo Simulation
### Description: gain experience with the Spark computational model in AWS cloud datacenter.
This is a homework assignment for CS441 at the University of Illinois at Chicago.
This project utilizes the open-source [Apache Spark 2.4.4](https://spark.apache.org) cluster-computing framework to program clusters with implicit data parallelism on the [Amazon EMR](https://aws.amazon.com/emr) cloud data platform.


## Background
...


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

To view these steps visually, please view the [video](https://youtu.be/839ZA6zJOWM).


## Tests
This project includes 14 unit tests based on the [ScalaTest](http://www.scalatest.org) testing framework, which are located in the project's `test/scala` directory and include app configuration and FloatArrayWritable creation tests.
The tests will run automatically when the JAR is assembled. However, if you would like to run them again, simply `cd` into the project root directory and enter the following command: `sbt test`.


## Monte Carlo
The Monte Carlo simulation in this project is comprised of the following.

1. First...

## Results
The results of the Spark application can be obtained using the [Amazon S3](https://docs.aws.amazon.com/s3) portal. From the S3 bucket directory, select `results.txt` and click the `Download` button.

![Amazon S3](https://bitbucket.org/spate54/shyam_patel_hw3/raw/d254c0acb71fba2cc231d35229404ccdb9da0c87/images/s3results.png)


The following are the results obtained from running the simulation on Amazon EMR.
```
Mean:             $7113.54
Std deviation:    5335.372
5th  percentile:  $1572.98
25th percentile:  $2663.25
50th percentile:  $5292.40
75th percentile:  $10518.66
95th percentile:  $18186.97
```

To view images and analysis, see `Documentation.pdf` located in the project root directory.
