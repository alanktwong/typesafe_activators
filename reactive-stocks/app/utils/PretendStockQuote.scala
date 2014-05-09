package utils


import scala.util.Random

class PretendStockQuote extends StockQuote {
	def newPrice(lastPrice: Double): Double= {
		// todo: this trends towards zero
		val random = Random;
		val price = lastPrice * (0.95  + (0.1 * random.nextDouble())); // lastPrice * (0.95 to 1.05)
		price
	}

}