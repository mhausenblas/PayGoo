package info.paygoo.core {

	import scala.collection.mutable.ArrayBuffer
	import scala.actors.Actor
	import scala.actors.Actor._
	import scala.util.parsing.json._
	import org.scardf._
	import org.joda.time.LocalDate
	
	/** 
	 * Represents a PayGoo's serialisation (aka wire format).
	 * A PayGoo MUST support all of the following four media types.
	 * 
	 * @param mediatype as of http://www.iana.org/assignments/media-types/
	 */
	case class WireFormat ( val mediatype: String, val target: String )
	object HTML extends WireFormat ( mediatype = "text/html", target = "human" )
	object JSON extends WireFormat ( mediatype = "application/json", target = "machine" )
	object Text extends WireFormat ( mediatype = "text/plain", target = "human" )
	object NTriple extends WireFormat ( mediatype = "text/plain", target = "machine" )

	/** 
	 * The core PayGoo class, just has a label. 
	 * 
	 * @param id the PayGoo identifier, MUST be a HTTP URI
	 * @param label a human-readable label for the PayGoo
	 */
	abstract class PayGoo (val pgid: String, var label: String ) {
		val s = Vocabulary( "http://schema.org/" )
		val dc = Vocabulary( "http://purl.org/dc/terms/" )
		val bp = Vocabulary( "http://open-services.net/ns/basicProfile#" )
		
		/** 
		 * Returns a serialisation in the specified format, defaults to JSON.
		 * 
		 * @param format the selected wire format, one of {HTML, JSON, Text, NTriple}
		 * @return a string representation in the selected wire format
		 */
		def ser( format: WireFormat = JSON ) : String
	}

	/** 
	 * The PayGoo resource class. 
	 */
	case class PayGooResource (rpgid: String, rlabel: String ) extends PayGoo ( rpgid, rlabel ) {
		private var r = Map ( "id" -> rpgid, "label" -> rlabel, "modified" -> new LocalDate())
		
		override def ser ( format: WireFormat = JSON ) : String = format match {
			case HTML => "<div>About <a href='" + r("id") + "'>" + r("label") + "</a>, last updated " + r("modified") + "</div>"
			case JSON => JSONObject(r).toString
			case Text => "id=" + r("id") + ", label=" + r("label") + ", modified=" + r("modified") 
			case NTriple => val g = Graph.build(	UriRef( r("id").toString ) - (
													RDF.Type -> s.uriref("Thing"),
													dc.uriref("title") ->  r("label"),
													dc.uriref("modified") ->  r("modified")
												)
									)
									g.rend
		}

		override def toString = "[PayGooResource: id=" + r("id") + " | label=" + r("label") + " | modified=" + r("modified") + "]"
	}
	
	/** 
	 * Testing a sample PayGoo resource.
	 */
	object PayGooResource extends App {
		val r = new PayGooResource("http://data.example.com/#res", "a simple resource")
		println("As HTML:")
		println(r.ser(format=HTML))
		println("\nAs JSON:")
		println(r.ser(format=JSON))
		println("\nAs plain text:")
		println(r.ser(format=Text))
		println("\nAs RDF/NTriple:")
		println(r.ser(format=NTriple))
		println(r)
	}
	
	/** 
	 * The PayGoo container class. 
	 */
	case class PayGooContainer (cpgid: String, clabel: String ) extends PayGoo ( cpgid, clabel ) {
		private var c = Map ( "id" -> cpgid, "label" -> clabel, "modified" -> new LocalDate())
		private var members = ArrayBuffer[PayGooResource]()
		
		override def ser ( format: WireFormat = JSON ) : String = format match {
			case HTML => "<div>About <a href='" + c("id") + "'>" + c("label") + "</a>, last updated " + c("modified") + " containing: " + serContainerHTML + "</div>"
			case JSON => serContainerJSON	
			case Text => "id=" + c("id") + ", label=" + c("label") + ", modified=" + c("modified") 
			case NTriple => val g = Graph.build(	UriRef( c("id").toString ) - (
													RDF.Type -> bp.uriref("Container"),
													dc.uriref("title") ->  c("label"),
													dc.uriref("modified") ->  c("modified")
												)
									)
									g.rend
		}
		
		def serContainerHTML : String = {
			var ret : String = "no members"
			
			if ( !members.isEmpty ) {
				ret = ""
				for (m <- members)
					ret += m.ser(format=HTML)
			}
			ret
		}
		
		//TODO: use proper JSON serialiser instead ...
		def serContainerJSON : String = {
			var ret : String = "no members"
			val m = for ( i <- 0 until members.length ) yield members(i).ser()
			var con = Map ( "container" -> JSONObject(c), "members" ->  m )
			
			if ( !members.isEmpty ) ret = JSONObject(con).toString
			ret
		}
		
		

		override def toString = "[PayGooContainer: id=" + c("id") + " | label=" + c("label") + " | modified=" + c("modified") + "]"
		
		def add ( member : PayGooResource ) = {
			members += member
		}

		def remove ( member : PayGooResource ) : Unit = {
			for (i <- 0 until members.length) {
				if (members(i).rpgid == member.rpgid) {
					members.remove(i)
					return
				}
			}
		}
		
		def list = {
			members.toArray
		}
	}
	
	/** 
	 * Testing a sample PayGoo container.
	 */
	object PayGooContainer extends App {
		val c = new PayGooContainer("http://data.example.com/#container", "a simple container")
		val r1 = new PayGooResource("http://data.example.com/#res1", "resource 1")
		val r2 = new PayGooResource("http://data.example.com/#res2", "resource 2")
		
		println("As HTML:")
		println(c.ser(format=HTML))
		println("\nAs JSON:")
		println(c.ser(format=JSON))
		println("\nAs plain text:")
		println(c.ser(format=Text))
		println("\nAs RDF/NTriple:")
		println(c.ser(format=NTriple))
		println(c)

		println("\n\nNow adding " + r1.label + " and " + r2.label)
		c.add(r1)
		c.add(r2)
		println("\nAs HTML:")
		println(c.ser(format=HTML))
		println("\nAs JSON:")
		println(c.ser(format=JSON))
		
		println("\n\nNow removing " + r1.label)
		c.remove(r1)
		println("\nAs HTML:")
		println(c.ser(format=HTML))
		println("\nAs JSON:")
		println(c.ser(format=JSON))
		

	}

	// TODO: Implement PayGooDataset
	

}

