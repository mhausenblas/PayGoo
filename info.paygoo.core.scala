package info.paygoo.core {

	import collection.mutable.HashMap
	import scala.collection.mutable.ArrayBuffer
	import scala.actors.Actor
	import scala.actors.Actor._
	import scala.util.parsing.json._
	import org.scardf._
	import org.joda.time.LocalDate
	import info.paygoo.vocab._
	import java.io.StringReader
	
	/** 
	 * Represents a PayGoo's serialisation (aka wire format).
	 * A PayGoo MUST support all of the following four media types.
	 * 
	 * @param mediatype as of http://www.iana.org/assignments/media-types/
	 */
	case class WireFormat ( val mediatype: String, val target: String )
	object HTML extends WireFormat ( mediatype = "text/html", target = "human" )
	object JSON extends WireFormat ( mediatype = "application/json", target = "machine" )
	object NTriple extends WireFormat ( mediatype = "text/plain", target = "machine" )

	/** 
	 * The core PayGoo class, just has an identifier and a label. 
	 * 
	 * @param id the PayGoo identifier, MUST be a HTTP URI
	 * @param label a human-readable label for the PayGoo
	 */
	abstract class PayGoo (val pgid: String, var label: String ) {
		
		/** 
		 * Returns a serialisation in the specified format, defaults to JSON.
		 * 
		 * @param format the selected wire format, one of {HTML, JSON, NTriple}
		 * @return a string representation in the selected wire format
		 */
		def ser( format: WireFormat = JSON ) : String
		
		
		/** 
		 * Maps a media type string into one of the wire formats
		 * 
		 * @param format the IANA media type as a string
		 * @return an object of type WireFormat
		 */
		def mt2wf( format: String = JSON.mediatype ) : WireFormat = format match {
				case HTML.mediatype => HTML
				case JSON.mediatype => JSON
				case NTriple.mediatype => NTriple
		}
		
		def path : String = {
			val u = new java.net.URL(pgid)
			u.getPath
		}
		
		def baseURI : String = {
			val u = new java.net.URL(pgid)
			
			if(u.getPort == -1) "http://" + u.getHost
			else "http://" + u.getHost + ":" + u.getPort
		}
		
	}

	/** 
	 * The PayGoo resource class, a useful basis for realising a Basic Profile Resource (BPR).
	 */
	case class PayGooResource (rpgid: String, rlabel: String ) extends PayGoo ( rpgid, rlabel ) {
		private var r = Map ( "id" -> rpgid, "label" -> rlabel, "modified" -> new LocalDate().toString)
		private var p = ""
		
		override def ser ( format: WireFormat = JSON ) : String = format match {
			case HTML => "<div>About <a href='" + r("id") + "'>" + r("label") + "</a>, last updated " + r("modified") + "</div>"
			case JSON => JSONObject(r).toString
			case NTriple => val g = Graph.build(	UriRef( r("id").toString ) - (
													RDF.Type -> SchemaOrg.Thing,
													DC.title ->  r("label"),
													DC.modified ->  r("modified")
												)
									) ++ new Serializator(org.scardf.NTriple).readFrom( new StringReader( p ) )
									g.rend
		}
		
		override def toString = "[PayGooResource: id=" + r("id") + " | label=" + r("label") + " | modified=" + r("modified") + "]"
		
		def raw = r
		
		def set ( payload : String,  mediatype: String ) {
			p = payload
		}
	}
	
	/** 
	 * Testing a sample PayGoo resource.
	 */
	object PayGooResource extends App {
		val r = new PayGooResource("http://data.example.com/#res", "a simple resource")
		println(r)
		println("As HTML:")
		println(r.ser(format=HTML))
		println("\nAs JSON:")
		println(r.ser(format=JSON))
		println("\nAs RDF/NTriple:")
		println(r.ser(format=NTriple))
	}
	
	/** 
	 * The PayGoo container class, a useful basis for realising a Basic Profile Container (BPC).
	 */
	case class PayGooContainer (cpgid: String, clabel: String ) extends PayGoo ( cpgid, clabel ) {
		private var c = Map ( "id" -> cpgid, "label" -> clabel, "modified" -> new LocalDate().toString)
		//private var members = ArrayBuffer[PayGooResource]()
		private val members = new HashMap[String, PayGooResource]
		
		override def ser ( format: WireFormat = JSON ) : String = format match {
			case HTML => serContainerHTML
			case JSON => serContainerJSON
			case NTriple => serContainerNTriple
		}
		
		def serContainerHTML : String = {
			var ret : String = "no members"
			
			if ( !members.isEmpty ) {
				ret = "<div>About <a href='" + c("id") + "'>" + c("label") + "</a>, last updated " + c("modified") + " containing: <ul>" 
				for ( (rid, m) <- members)
					ret += "<li><a href='" + m.raw("id") + "'>" + m.raw("label") + "</a></li>"
				ret += "</ul></div>"
			}
			ret
		}
		
		def serContainerJSON : String = {
			var ret : String = "{}"
			
			if ( !members.isEmpty ){
				val m = for ( (rid, m) <- members ) yield m.raw("id")
				var con = Map ( "container" -> JSONObject(c), "members" ->  JSONArray(m.toList) )
				ret = JSONObject(con).toString	
			} 
			ret
		}
				
		def serContainerNTriple : String = {
			var ret : String = ""
			
			if ( !members.isEmpty ){
				val m = for ( (rid, m) <- members ) yield m.raw("id")
				var g = Graph.build(	UriRef( c("id").toString ) - (
														RDF.Type -> LDBP.Container,
														DC.title ->  c("label"),
														DC.modified ->  c("modified")
													)
										)
				for ( (rid, m)  <- members)
					g = g ++  Graph.build( UriRef( c("id").toString ) - (RDFS.member -> m.raw("id").toString ) )
				ret = g.rend
			} 
			ret
		}

		override def toString = "[PayGooContainer: id=" + c("id") + " | label=" + c("label") + " | modified=" + c("modified") + "]"
		
		// adds in a way that it takes a payload (JSON/RDF), creates a new resource in the container and returns its HTTP URI
		def add( payload: String, mediatype: String ) : String = {
			val rid = baseURI + "/" + java.util.UUID.randomUUID().toString
			val r = new PayGooResource(rid, "resource in container " + clabel)
			r.set(payload, mediatype)
			_add(rid, r)
			rid
		}

		
		private def _add (resURI: String, member: PayGooResource ) = {
			members += resURI -> member
		}

		// remove so that it removes resource in the container
		def remove ( resURI: String ) : Unit = {
			members.remove(resURI)
		}
		
		def get ( resURI: String ) : PayGooResource = {
			members(resURI)
		}

	}
	
	
	
	/** 
	 * Testing a sample PayGoo container.
	 */
	object PayGooContainer extends App {
		val c = new PayGooContainer("http://data.example.com/#container", "a simple container")
		val rID = c.add( scala.io.Source.fromFile("test/payload.nt").mkString , "text/plain")
		println(c)
		println("\nAs HTML:")
		println(c.ser(format=HTML))
		println("\nAs JSON:")
		println(c.ser(format=JSON))
		println("\nAs RDF/NTriple:")
		println(c.ser(format=NTriple))
		
		println("\nNew resource:")
		println(c.get(rID).ser(format=NTriple))
		c.remove(rID)
		println("\n ... now removed ...")
		println(c.ser(format=NTriple))
		
	}

	// TODO: implement PayGooDataset, a special PayGooContainer that has no parent and represents an entire dataset

}

