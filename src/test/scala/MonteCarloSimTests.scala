/* CS441 HW3: Spark Monte Carlo Simulation
 * Name:   Shyam Patel
 * NetID:  spate54
 * Date:   Nov 24, 2019
 */

import com.typesafe.config.ConfigFactory.load
import org.scalatest.{FlatSpec, Matchers}
import scala.collection.{mutable => my}

// MonteCarloSimTests class consisting of 10 unit tests based on the ScalaTest testing framework
class MonteCarloSimTests extends FlatSpec with Matchers {
  // TESTS 1-9: read from app configuration
  "Configuration" should "have application name" in {
    load.getString("spark.app_name") should be ("Monte Carlo Simulation")
  }
  it should "specify running Spark locally with as many worker threads as logical cores" in {
    load.getString("spark.master_url") should be ("local[*]")
  }
  it should "specify local path for portfolio data" in {
    load.getString("spark.portfolio_path") should be ("src/main/resources/portfolio.csv")
  }
  it should "specify AWS path for portfolio data" in {
    load.getString("spark.portfolio_path_aws") should be ("/portfolio.csv")
  }
  it should "specify local path for change data" in {
    load.getString("spark.data_path") should be ("src/main/data/change.csv")
  }
  it should "specify AWS path for change data" in {
    load.getString("spark.data_path_aws") should be ("/change.csv")
  }
  it should "specify local and AWS paths for output directory" in {
    load.getString("spark.output_path") should be ("output")
    load.getString("spark.output_path_aws") should be ("/output")
  }
  it should "specify local and AWS paths for output source" in {
    load.getString("spark.output_src_path") should be ("output/part-00000")
    load.getString("spark.output_src_path_aws") should be ("/output/part-00000")
  }
  it should "specify local and AWS paths for output destination" in {
    load.getString("spark.output_dest_path") should be ("results.txt")
    load.getString("spark.output_dest_path_aws") should be ("/results.txt")
  }

  // TESTS 10-14: test Monte Carlo simulation session
  "Simulation" should "buy stocks" in {
    val portfolio     = my.Map[String, Float]()
    val values        = List(("2019-11-22", List(0.0049763f)),
                             ("2019-11-21", List(0.0013211f)),
                             ("2019-11-20", List(0.0081746f)))
    val symbols       = List("GOOGL")
    val results       = MonteCarloSim.runSession(portfolio, values, symbols)

    results.foreach(x => math.ceil(x._2).toInt should be > 0)
  }
  it should "sell stocks" in {
    val portfolio     = my.Map[String, Float]()
    portfolio("AMZN") = 200
    val values        = List(("2019-11-22", List(-0.0063468f, 0.0044965f)),
                             ("2019-11-21", List(-0.0061986f, 0.0021264f)),
                             ("2019-11-20", List(-0.0041419f, 0.0090808f)))
    val symbols       = List("AMZN", "AAPL")
    val results       = MonteCarloSim.runSession(portfolio, values, symbols)

   results.foreach(x => x._1 should be ("AAPL"))
  }
  it should "make sensible investments" in {
    val portfolio     = my.Map[String, Float]()
    portfolio("MSFT") = 500
    val values        = List(("2019-11-22", List(0.0363468f, -0.0044965f)),
                             ("2019-11-21", List(0.0461986f, -0.0021264f)),
                             ("2019-11-20", List(0.0541419f, -0.0090808f)))
    val symbols       = List("AAPL", "MSFT")
    val results       = MonteCarloSim.runSession(portfolio, values, symbols)

    results.foreach(x => x._1 should be ("AAPL"))
    math.ceil(results.values.sum).toInt should be > 500
  }
  it should "update gains in portfolio" in {
    val portfolio     = my.Map[String, Float]()
    portfolio("AAPL") = 100
    val values        = List(("2019-11-22", List(0.0363468f)))
    val symbols       = List("AAPL")
    val results       = MonteCarloSim.runSession(portfolio, values, symbols)

    math.ceil(results.values.sum).toInt shouldBe 101
  }
  it should "not return unfounded values" in {
    val portfolio     = my.Map[String, Float]()
    val values        = List.empty
    val symbols       = List.empty
    val results       = MonteCarloSim.runSession(portfolio, values, symbols)

    results.size shouldBe 0
  }
}//end MonteCarloSimTests
