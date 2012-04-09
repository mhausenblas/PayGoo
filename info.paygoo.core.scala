import scala.actors.Actor
import scala.actors.Actor._
import scala.util.parsing.json._

package info.paygoo.core {
	
	/** 
	 * Objects that represent a PayGoo's serialisation (or wire format).
	 * A PayGoo MUST support all four media types.
	 * 
	 * @param mediatype as of http://www.iana.org/assignments/media-types/
	 */
	case class WireFormat ( val mediatype: String )
	object HTML extends WireFormat ( mediatype = "text/html" )
	object JSON extends WireFormat ( mediatype = "application/json" )
	object Text extends WireFormat ( mediatype = "text/plain" )
	object Turtle extends WireFormat ( mediatype = "text/turtle" )

	/** 
	 * The core PayGoo class, just has a label. 
	 * 
	 * @param label a human-readable label for the PayGoo
	 */
	abstract class PayGoo ( var label: String ) {
		def ser( format: WireFormat = JSON ) : String
	}

	/** 
	 * A PayGoo resource class. 
	 * 
	 * @param rlabel a human-readable label for the PayGoo resource
	 * @return dunno
	 */
	case class PayGooResource ( rlabel: String ) extends PayGoo ( rlabel ) {
		private var r = Map ( "label" -> rlabel )
		
		override def ser ( format: WireFormat = JSON ) : String = format match {
			case HTML => "<div>" + rlabel + "</div>"
			case JSON => JSONObject(r).toString
			case Text => r("label")
			case Turtle => ":a rdfs:label '" + r("label") + "' ."
		}

		override def toString = "[PayGooResource: label=" + r("label") + "]"
	}

	
	/** 
	 * Testing the resource ... 
	 */
	object PayGooResource extends App {
		val r = new PayGooResource("pg0")
		println(r.ser(format=HTML))
		println(r.ser(format=JSON))
		println(r.ser(format=Text))
		println(r.ser(format=Turtle))
		println(r)
	}
}

