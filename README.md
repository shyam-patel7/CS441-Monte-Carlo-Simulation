# HW3: Spark Monte Carlo Simulation
### Description: gain experience with the Spark computational model in AWS cloud datacenter.
This is a homework assignment for CS441 at the University of Illinois at Chicago.
This project utilizes the open-source [Apache Spark 2.4.4](https://spark.apache.org) cluster-computing framework to program clusters with implicit data parallelism on the [Amazon EMR](https://aws.amazon.com/emr) cloud data platform.


## Background
...


## Running
To successfully run this project, access to [Amazon EMR](https://aws.amazon.com/emr), or the [Hortonworks Data Platform (HDP)](https://www.cloudera.com/downloads/hortonworks-sandbox.html) on Sandbox with Apache Hadoop with [VMware](https://my.vmware.com/en/web/vmware/downloads) virtualization software, [IntelliJ IDEA](https://www.jetbrains.com/idea), [sbt](https://docs.scala-lang.org/getting-started/sbt-track/getting-started-with-scala-and-sbt-on-the-command-line.html) and [Java 8 JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html) (version 1.8 or higher) are required.

1. From the project root directory, enter the following Terminal command to run all tests and assemble the JAR:

        sbt clean assembly

2. Start the HDP sandbox. Then, enter the following command in Terminal to transfer the JAR into the home directory.

        scp -P 2222 target/scala-2.13/Shyam_Patel_hw2-assembly-0.1.jar maria_dev@sandbox-hdp.hoziprtonworks.com:~/

3. SSH into the HDP sandbox.

        ssh maria_dev@sandbox-hdp.hoziprtonworks.com -p 2222

4. Download and extract the DBLP dataset.

        wget https://dblp.uni-trier.de/xml/dblp.xml.gz
        gzip -d dblp.xml.gz

5. Create input and output directories.

        hdfs dfs -mkdir -p /user/maria_dev/input
        hdfs dfs -mkdir -p /user/maria_dev/output

6. Copy the DBLP dataset into the input directory.

        hdfs dfs -put dblp.xml /user/maria_dev/input

7. Run the MapReduce job.

        hadoop jar Shyam_Patel_hw2-assembly-0.1.jar /user/maria_dev/input /user/maria_dev/output


## Tests
This project includes 14 unit tests based on the [ScalaTest](http://www.scalatest.org) testing framework, which are located in the project's `test/scala` directory and include app configuration and FloatArrayWritable creation tests.
The tests will run automatically when the JAR is assembled. However, if you would like to run them again, simply `cd` into the project root directory and enter the following command: `sbt test`.


## MapReduce
The MapReduce implementation in this project is comprised of 5 phases.

1. First, the **XmlInputFormat** class, which extends *TextInputFormat*, returns a custom record reader that uses the pre-defined start and end tags (e.g., located in *application.conf*) to scan through the input stream byte-by-byte to return publication records that are sent to mappers as key–value pairs, where keys are positions in the XML input file (e.g., type *LongWritable*), and values are the lines of text that contain the publication record (e.g., type *Text*).
2. During the **mapper #1** phase, publication records are mapped into various key–value pairs. For each publication, the venue type is ascertained, as well as any authors or editors that are listed in the record. If author(s) is/are listed, their names are stored, through which their count is determined, and each co-authorís score is calculated using the formula described in the documentation. Then, each author is emitted as a key–value pair, where the key is the authorís name, and the value consists of the number of co-authors in the publication and the score the author received for his or her contribution. Similarly, if author(s) is/are listed, the venue is mapped as a key–value pair, where the key is the venue type and the value is the number of co-authors in the publication. If the venue type is ascertained to be a conference or a journal, key–value pairs with value one are emitted, where the keys represent the names of conferences or respective journals. In order to group numbers of co-authors into bins that can be used to plot a histogram that describes publications by co-authors, the number of co-authors in the publication are matched into a corresponding bin, which is mapped as the key in a key–value pair with value one. If the year of the publication is listed, it is matched into its corresponding decade bin, mapped as the key in a key–value pair with value one.
3. During the **reducer** phase, key–value pairs emitted by mapper #1 are reduced. For author key–value pairs (e.g., where the key represents a single author), authorship score values are summed, and numbers of co-authors are merged into a single set. Similarly, for venue key–value pairs (e.g., where the key represents a single venue type), numbers of co-authors are merged. For all other key–value pairs, counts are summed and updated.
4. During the **mapper #2** phase, key–value pairs that have already passed through the reducer phase undergo their final mappings. For both author and venue type key–value pairs, sets of numbers of co-authors are sorted and transformed into smaller sets that each contain the total number of publications, the maximum number of co-authors, the median number of co-authors, and the average number of co-authors. Author key–value pairs also retain cumulative authorship scores for each author, who are ranked into lists of the top 100 and the bottom 100 authors. For conference and journal key–value pairs, counts are also placed into bins. All final key–value pairs are emitted.
5. Finally, the **CsvOutputFormat** class gets the default path for the output with *.csv* extension, sets its base name (e.g., results) and returns a custom record writer that writes key–value pairs into the data output stream with a comma separator in accordance with the CSV file format.


## Results
The results of the MapReduce job can be obtained using Ambari’s [Files View UI](http://sandbox-hdp.hoziprtonworks.com:8080). From the user’s output directory, select *results.csv* and the Plotly charts named *co-authors.html*, *conferences.html*, *journals.html* and *years.html*. Click Download.

![Ambari](https://bitbucket.org/spate54/shyam_patel_hw2/raw/10000b514fc37d79b36f594eac677dde9e0f748b/images/AmbariFilesView.png)


**Figure 1.** The number of co-authors and decades for each publication in the dataset.
```
========== Co-authors ==========   ============ Years =============
| Num Co-authors  | Num Pub    |   | Decade          | Num Pub    |
|  1 co-author    |     812090 |   | 1970s & earlier |      55519 |
|  2-3 co-authors |    2481779 |   | 1980s           |     128386 |
|  4-6 co-authors |    1305908 |   | 1990s           |     442477 |
|  7-9 co-authors |     137770 |   | 2000s           |    1403224 |
| 10+ co-authors  |      35301 |   | 2010s           |    2795755 |
```

To view images and analysis, see `Documentation.pdf` located in the project root directory.
