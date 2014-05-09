package utils


import akka.actor._
import akka.testkit._

import org.specs2.mutable._
import org.specs2.runner._

import scala.concurrent.duration._
import scala.collection.immutable.HashSet

import org.junit.runner._

@RunWith(classOf[JUnitRunner])
class PretendStockQuoteSpec extends Specification with SpecificationLike {
	
	import scala.util.Random
	"Pretend Stock Quote" should {
		val symbol = "ABC"
		"be plus or minus 5 percent of the old price" in {
			// Create a stock actor with a stubbed out stockquote price and watcher
			val stockQuote = new PretendStockQuote;
			val random = Random
			val origPrice = random.nextDouble
			val newPrice = stockQuote.newPrice(origPrice);
			
			newPrice must beGreaterThan(origPrice - (origPrice * 0.05))
			newPrice must beLessThan(origPrice + (origPrice * 0.05))
		}
		
	}


}