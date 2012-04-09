import scala.actors.Actor
import scala.actors.Actor._
import scala.util.parsing.json._
import org.scardf._
import org.joda.time.LocalDate

package info.paygoo.core {
	
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
	 * A PayGoo resource class. 
	 * 
	 * @param rlabel a human-readable label for the PayGoo resource
	 * @return dunno
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
		val r = new PayGooResource("http://data.example.com/#it", "pg0")
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
}

