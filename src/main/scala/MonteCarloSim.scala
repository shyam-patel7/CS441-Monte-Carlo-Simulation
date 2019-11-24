/* CS441 HW3: Spark Monte Carlo Simulation
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Nov 24, 2019
 */

import com.typesafe.config.ConfigFactory.load
import org.apache.hadoop.fs.Path
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory.getLogger
import scala.collection.{mutable => my}
import scala.util.Random.shuffle


// encapsulates Monte Carlo simulation for predicting stock portfolio losses/gains using spark
object MonteCarloSim {
  private val log  = getLogger(getClass)                                            // logger
  private val conf = load                                                           // scala app configuration
  private val c    = new SparkConf                                                  // spark app configuration

  // main driver method that runs Spark application
  def main(args: Array[String]): Unit = {
    val aws    = if (args.length > 0) true      else false                          // check for aws argument
    val bucket = if (args.length > 0) args.head else ""                             // name of s3 bucket

    // retrieve input data and output paths
    val s3            = conf.getString("spark.aws_prefix")
    val portfolioPath = if (aws) s3 + bucket + conf.getString("spark.portfolio_path_aws")
                        else                   conf.getString("spark.portfolio_path")
    val dataPath      = if (aws) s3 + bucket + conf.getString("spark.data_path_aws")
                        else                   conf.getString("spark.data_path")
    val output        = if (aws)               conf.getString("spark.output_path_aws")
                        else                   conf.getString("spark.output_path")
    val outputPath    =          new Path(output)
    val srcPath       = if (aws) new Path(conf.getString(s3 + bucket + "spark.output_src_path_aws"))
                        else     new Path(conf.getString("spark.output_src_path"))
    val destPath      = if (aws) new Path(conf.getString(s3 + bucket + "spark.output_dest_path_aws"))
                        else     new Path(conf.getString("spark.output_dest_path"))

    // set spark application name and, if not running on aws, set to run locally with multiple cores
    if (!aws) c.setMaster (conf.getString("spark.master_url"))
              c.setAppName(conf.getString("spark.app_name"  ))
    val sc = new SparkContext(c)                                                    // initialize spark context

    // retrieve resilient distributed dataset (rdd) of portfolio from input path
    val portfolio = sc.textFile(portfolioPath)
                      .map(x => { val v = x.split(','); (v.head, v(1).toFloat) })   // (symbol, investment) tuples
    val funds     = my.Map[String, Float]()
    portfolio.collect.foreach(x => funds(x._1) = x._2)

    // retrieve resilient distributed dataset (rdd) of stock data from input path
    val data    = sc.textFile(dataPath)
    val columns = data.first.split(',')                                             // names of columns
    val symbols = columns.tail.toList                                               // ticker symbols
    val values  = data.filter(r => !(r.split(',') sameElements columns)).map(x => { // convert csv records into
                    val v = x.split(',')                                            //  (date, % changes) tuples
                    (v.head, v.tail.toList.map(_.toFloat))
                  }).collect.toList

    // create parallelized rdd collection by running simulations in parallel, then assess results
    val results = sc.parallelize(seq = 1 to 10000, numSlices = 500)
                    .map(_ => runSession(funds, values, symbols))                   // run parallel simulations
    val gains   = results.map(_.values.sum).sortBy(x => x, numPartitions = 1)       // sum and sort net gains
    val pctiles = Seq(0.05f,0.25f,0.5f,0.75f,0.95f).map(p =>                        // calculate percentiles
                      gains.zipWithIndex.map(_.swap).lookup((p * gains.count).toLong).head)
    val stats   = sc.parallelize(Seq(f"Mean:             $$${gains.mean}%1.2f",     // cumulative stats:
                                     f"Std deviation:    ${gains.stdev}%1.3f",      //  mean,
                                     f"5th  percentile:  $$${pctiles.head}%1.2f",   //  standard deviation,
                                     f"25th percentile:  $$${pctiles(1)}%1.2f",     //  percentiles
                                     f"50th percentile:  $$${pctiles(2)}%1.2f",
                                     f"75th percentile:  $$${pctiles(3)}%1.2f",
                                     f"95th percentile:  $$${pctiles(4)}%1.2f"), numSlices = 1)

    // prepare output directory for statistics
    val fs = outputPath.getFileSystem(sc.hadoopConfiguration)
    fs.mkdirs(outputPath)                                                           // make  output directory
    if (fs.delete(outputPath, true))
      log.info(s"Cleared output directory: $outputPath")                            // clear output directory
    else
      log.trace(s"Unable to clear output directory: $outputPath")

    stats.saveAsTextFile(output)                                                    // write stats to output
    stats.foreach(log.info)                                                         // log   stats

    // move statistics output to root directory
    if (fs.rename(srcPath, destPath) && fs.delete(outputPath, true))
      log.info(s"Results successfully stored at: $destPath")                        // rename output path
    else
      log.trace(s"Unable to access path: $srcPath")
  }//end def main


  // helper method to run Monte Carlo simulation session
  def runSession(portfolio: my.Map[String, Float], values: List[(String, List[Float])],
                 symbols: List[String]): my.Map[String, Float] = {
    shuffle(values).foreach(v => {
      val (date, changes) = v                                                       // extract date, % changes in close
      val gainsOrLosses   = (symbols zip changes)                                   // all gains or losses per day
        .map(x => (x._1, portfolio.getOrElse(x._1, 0f) * x._2 / 100f))              //  = curr investment * % change
      gainsOrLosses.foreach(x => {
        if (portfolio.contains(x._1))                                               // add gains to portfolio
          portfolio(x._1) = portfolio.getOrElse(x._1, 0f) + x._2
      })

      val netGainOrLoss   = gainsOrLosses.map(_._2).sum                             // net gain or loss for day
      if (netGainOrLoss > 0) {                                                      // net gain :
        val possibilities = (symbols zip changes).filter(x => x._2 > 0)             //  prospective stocks
        if (possibilities.nonEmpty) {
          val buy         = shuffle(possibilities).head._1                          //  choose stock to buy
          portfolio(buy)  = portfolio.getOrElse(buy, 0f) + netGainOrLoss            //  add investment to portfolio

          log.info(f"$date (+/−): $$$netGainOrLoss%1.2f, investing in $buy")
        } else log.debug(f"$date (+/−): $$$netGainOrLoss%1.2f")
      } else {                                                                      // net loss :
        val sell          = gainsOrLosses.minBy(_._2)._1                            //  choose stock to sell
        val buy           = (symbols zip changes).maxBy(_._2)._1                    //  choose stock to buy
        if (sell != buy) {
          val amount      = portfolio.getOrElse(sell, 0f)                           //  investment amount
          portfolio.remove(sell)
          portfolio(buy)  = portfolio.getOrElse(buy, 0f) + amount                   //  add investment to portfolio
          log.info(f"$date (+/−): $$$netGainOrLoss%1.2f, selling $$$amount%1.2f in $sell and buying $buy")
        } else log.debug(f"$date (+/−): $$$netGainOrLoss%1.2f")
      }
    })
    portfolio
  }//end runSession
}//end object MonteCarloSim
