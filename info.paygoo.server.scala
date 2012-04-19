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

	abstract class SimpleHttpServer(val defaultMediaType: String = "text/html") extends SimpleHttpServerBase {

		private val mappings = new HashMap[String, () => String]
				
		def mapGET(gethandler: String => String, path: String, mediatype: String = "text/html") {
			mappings += ( mediatype + " " + path ) -> (() => gethandler(mediatype))
		}
		
		def pickWF(mediatype : String) = mediatype match {
			case HTML.mediatype => HTML
			case JSON.mediatype => JSON
			case NTriple.mediatype => NTriple
		}
		
		def handle(exchange: HttpExchange) = {
			val method =  exchange.getRequestMethod()
			method match {
				case "GET" => handleGET(exchange)
				case "POST" => notSupported(exchange)
				case "PUT" => notSupported(exchange)
				case "DELETE" => notSupported(exchange)
				case _ => notSupported(exchange)
			}
		}

		def notSupported(exchange: HttpExchange) = {
			respond(exchange, 501, "501 - Not Implemented\nThe "+ exchange.getRequestMethod() + " method is not supported, yet.\n", Text.mediatype) 
		}
		
		def handleGET(exchange: HttpExchange) = {
			val h = exchange.getRequestHeaders()
			var q = exchange.getRequestURI.toString
			var accept = defaultMediaType
			var conneg = ""

			// heads-up: very naive conneg implementation following
			if (h.containsKey("Accept")) {
				// danger, the following is really a nasty hack: 
				// in case a user agent, such a Web browser, sends 
				// multiple desired types, simply take the first one 
				// and in doubt, serve the default
				// see also http://tools.ietf.org/html/rfc2616#section-14.1
				try {
					conneg = h.getFirst("Accept").split(",")(0)
				} finally {
					if (conneg contains "*/*") accept = defaultMediaType
					else accept = conneg
				}
			}
			
			// a query paramter such as ?json or ?html indicating the desired format overwrites conneg
			if(q contains "?") {
				try {
					q = q.split("\\?")(1)
					q match {
						case "html" => accept = HTML.mediatype
						case "json" => accept = JSON.mediatype
						case "ntriple" => accept = NTriple.mediatype
						case _ => accept = defaultMediaType
					}
				} catch {
					case ex: Exception => respond(exchange, 500, ex.toString)
				}
			}
			
			//essentially map to combintation of media type + path (as a key):
			mappings.get(accept + " " + exchange.getRequestURI.getPath) match {
				case None => respond(exchange, 404, "404 - Not found\n")
				case Some(action) =>
					try {
						respond(exchange, 200, action(), accept) 
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
	class PayGooServer extends SimpleHttpServer(defaultMediaType = JSON.mediatype) {
		
		// setting up the paygoos; this would typically come from a datasource (RDB, CSV or triple store):
		val BASE_URI = "http://localhost:6969"
		val paths : List[String] = List("/bpc0", "/res1","/res2")
		val c = new PayGooContainer(BASE_URI + paths(0), "container 0")
		val r1 = new PayGooResource(BASE_URI + paths(1), "resource 1")
		val r2 = new PayGooResource(BASE_URI + paths(2), "resource 2")
		c.add(r1)
		c.add(r2)
		
		// setting up the HTTP interface for the paygoos:
		val paygoos : List[PayGoo] = List(c, r1, r2)
		val wireformats : List[WireFormat] = List(HTML, JSON, NTriple)

		// setting up the HTTP interface - GET part:
		for (pg <- paygoos) {
			for (wf <- wireformats)  {
				//println("Created:" + pg + " for " + wf)
				mapGET({ wf => pg.ser(format=pickWF(wf)) }, pg.path, wf.mediatype )
			}
		}
		//TODO - add PUT/POST and DELETE support
	}

	object PayGooServer extends App {
		val server = new PayGooServer()
		server.start()
	}
	
}
