import scala.actors.Actor
import scala.actors.Actor._
import scala.util.parsing.json._
import org.scardf._
import org.joda.time.LocalDate

package info.paygoo.core {
	
	/** 
	 * Represent a PayGoo's serialisation (aka wire format).
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
	 * @param label a human-readable label for the PayGoo
	 */
	abstract class PayGoo (val base: String, var label: String ) {
		val s = Vocabulary( "http://schema.org/" )
		val dc = Vocabulary( "http://purl.org/dc/terms/" )
		val bp = Vocabulary( "http://open-services.net/ns/basicProfile#" )
		def ser( format: WireFormat = JSON ) : String
	}

	/** 
	 * A PayGoo resource class. 
	 * 
	 * @param rlabel a human-readable label for the PayGoo resource
	 * @return dunno
	 */
	case class PayGooResource (rbase: String, rlabel: String ) extends PayGoo ( rbase, rlabel ) {
		private var r = Map ( "base" -> rbase, "label" -> rlabel, "date" -> new LocalDate())
		private val rself = r("base") + "it"
		
		override def ser ( format: WireFormat = JSON ) : String = format match {
			case HTML => "<div>About <a href='" + rself + "'>" + r("label") + "</a>, last updated " + r("date") + "</div>"
			case JSON => JSONObject(r).toString
			case Text => "base=" + r("base") + ", label=" + r("label") + ", date=" + r("date") 
			case NTriple => val g = Graph.build(	UriRef( rself ) - (
													RDF.Type -> s.uriref("Thing"),
													dc.uriref("title") ->  r("label"),
													dc.uriref("date") ->  r("date")
												)
									)
									g.rend
		}

		override def toString = "[PayGooResource: base=" + r("base") + " | label=" + r("label") + " | date=" + r("date") + "]"
	}

	
	/** 
	 * Testing the resource ... 
	 */
	object PayGooResource extends App {
		val r = new PayGooResource("http://data.example.com/test#", "pg0")
		println(r.ser(format=HTML))
		println(r.ser(format=JSON))
		println(r.ser(format=Text))
		println(r.ser(format=NTriple))
		println(r)
	}
}

