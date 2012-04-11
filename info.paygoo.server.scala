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
		private val mappings = new HashMap[String, (String) => Any]
		
		def get(path: String, mediatype: String = "text/html")(action: => Any) = mappings += ( mediatype + " " + path ) -> ((mediatype) => action)
		
		def handle(exchange: HttpExchange) = {
			val h = exchange.getRequestHeaders()
			var q = exchange.getRequestURI.toString
			var accept = "text/html"

			// heads-up: very naive conneg implementation following
			if (h.containsKey("Accept")) {
				// danger, the following is really a nasty hack: 
				// in case a user agent, such a Web browser, sends 
				// multiple desired types, simply take the first one.
				// see also http://tools.ietf.org/html/rfc2616#section-14.1
				accept = h.getFirst("Accept").split(",")(0)
			}
			
			// a query paramter such as ?json or ?html indicating the desired format overwrites conneg
			if(q contains "?") {
				try {
					q = q.split("\\?")(1)
					q match {
						case "html" => accept = HTML.mediatype
						case "json" => accept = JSON.mediatype
						case "ntriple" => accept = NTriple.mediatype
						case _ => accept = HTML.mediatype
					}
				} catch {
					case ex: Exception => respond(exchange, 500, ex.toString)
				}
			}
			
			// essentially map to combintation of media type + path (as a key):
			mappings.get(accept + " " + exchange.getRequestURI.getPath) match {
				case None => respond(exchange, 404)
				case Some(action) =>
					try {
						respond(exchange, 200, action(accept).toString, accept) 
					} catch {
						case ex: Exception => respond(exchange, 500, ex.toString)
					}
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
		val BASE_URI = "http://localhost:6969"
		val c = new PayGooContainer(BASE_URI + "/bpc0", "container 0")
		val r1 = new PayGooResource(BASE_URI + "/res1", "resource 1")
		val r2 = new PayGooResource(BASE_URI + "/res2", "resource 2")
		c.add(r1)
		c.add(r2)
		
		get("/bpc0") {
			c.ser(format=HTML)
		}
		get("/bpc0", JSON.mediatype) {
			c.ser(format=JSON)
		}
		get("/bpc0", NTriple.mediatype) {
			c.ser(format=NTriple)
		}
		
		get("/res1") {
			r1.ser(format=HTML)
		}
		get("/res1", JSON.mediatype) {
			r1.ser(format=JSON)
		}
		get("/res1", NTriple.mediatype) {
			r1.ser(format=NTriple)
		}
		
		get("/res2") {
			r2.ser(format=HTML)
		}
		get("/res2", JSON.mediatype) {
			r2.ser(format=JSON)
		}
		get("/res2", NTriple.mediatype) {
			r2.ser(format=NTriple)
		}
		
	}

	object PayGooServer extends App {
		val server = new PayGooServer()
		server.start()
	}
}
