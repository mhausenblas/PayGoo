package info.paygoo.server {
	
	// based on http://stackoverflow.com/a/6432180/396567
	
	import java.net.InetSocketAddress
	import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer, Headers}
	import collection.mutable.HashMap
	import scala.actors.Actor
	import scala.actors.Actor._
	import info.paygoo.core._
	
	abstract class SimpleHttpServerBase(val socketAddress: String = "127.0.0.1", val port: Int = 6969, val backlog: Int = 0) extends HttpHandler {
	  private val address = new InetSocketAddress(socketAddress, port)
	  private val server = HttpServer.create(address, backlog)
	  server.createContext("/", this)

	def redirect(url: String) =
		<html>
		  <head>
			  <meta http-equiv="Refresh" content={"0," + url}/>
		  </head>
		  <body>
			You are being redirected to:
			<a href={url}>
			  {url}
			</a>
		  </body>
		</html>

	def respond(exchange: HttpExchange, code: Int = 200, body: String = "", mediatype: String = "text/html") {
		val bytes = body.getBytes
		var h = exchange.getResponseHeaders()	// added this to ...
		h.add("Content-Type", mediatype)		// ... explicitly set the content type
		exchange.sendResponseHeaders(code, bytes.size)
		exchange.getResponseBody.write(bytes)
		exchange.getResponseBody.write("\r\n\r\n".getBytes)
		exchange.getResponseBody.close()
		exchange.close()
	}

	  def start() = server.start()

	  def stop(delay: Int = 1) = server.stop(delay)
	}

	abstract class SimpleHttpServer extends SimpleHttpServerBase {
		private val mappings = new HashMap[String, () => Any]
		
		def get(path: String)(action: => Any) = mappings += path -> (() => action)
		
		def handle(exchange: HttpExchange) = mappings.get(exchange.getRequestURI.getPath) match {
			case None => respond(exchange, 404)
			case Some(action) =>
				try {
					// heads-up: very naive conneg implementation below ...
					val h = exchange.getRequestHeaders()
					if (h.containsKey("Accept")) respond(exchange, 200, action().toString, h.getFirst("Accept")) // if 'Accept' header is present, serve as requested
					else respond(exchange, 200, action().toString) // defaults to text/html
				} catch {
					case ex: Exception => respond(exchange, 500, ex.toString)
				}
		}
	}

	/** 
	 *  Experimental server that implement the Linked Data Basic Profile
	 *  as  of http://www.w3.org/Submission/2012/SUBM-ldbp-20120326/
	 * 
	 * @return dunno
	 */
	class PayGooServer extends SimpleHttpServer {
		val c = new PayGooContainer("http://localhost:6969/bpc0", "container 0")
		val r1 = new PayGooResource("http://localhost:6969/res1", "resource 1")
		val r2 = new PayGooResource("http://localhost:6969/res2", "resource 2")
		c.add(r1)
		c.add(r2)

		get("/") {
			redirect("/bpc0")
		}
		
		get("/bpc0") {
			c.ser(format=HTML)
		}
		
		get("/res1") {
			r1.ser(format=HTML)
		}
		
		get("/res2") {
			r2.ser(format=HTML)
		}
		
	}

	object PayGooServer extends App {
		val server = new PayGooServer()
		server.start()
	}
}
