package actors

import akka.actor.{Props, ActorRef, Actor}
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import scala.collection.JavaConverters._
import play.Play;
import play.libs.Json;
import play.mvc.WebSocket;

/**
 * The broker between the WebSocket and the StockActor(s).  The UserActor holds the connection and sends serialized
 * JSON data to the client.
 */

class UserActor(out: WebSocket.Out[JsonNode]) extends Actor {
	val defaultStocks: List[String] = Play.application().configuration().getStringList("default.stocks").asScala.toList

	init
	
	def init: Unit = {
		val stocksActor = StocksActor.stocksActor
		defaultStocks.foreach( stockSymbol => {
			stocksActor.tell(new WatchStock(stockSymbol), self);
		})
	}
	
	def receive = {
		case stockUpdate: StockUpdate => {
			// push the stock to the client
			val stockUpdateMessage = Json.newObject();
			stockUpdateMessage.put("type", "stockupdate");
			stockUpdateMessage.put("symbol", stockUpdate.symbol);
			stockUpdateMessage.put("price", stockUpdate.price.doubleValue);
			out.write(stockUpdateMessage)
		}
		case stockHistory: StockHistory => {
			// push the history to the client
			val stockUpdateMessage = Json.newObject();
			stockUpdateMessage.put("type", "stockhistory");
			stockUpdateMessage.put("symbol", stockHistory.symbol);

			val historyJson = stockUpdateMessage.putArray("history");
			stockHistory.history.foreach( price => {
				historyJson.add(price.doubleValue())
			})
			out.write(stockUpdateMessage);
		}
	}
}

