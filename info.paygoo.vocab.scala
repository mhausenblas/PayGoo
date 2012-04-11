package info.paygoo.vocab {

	import org.scardf._
	
	/** 
	 * The vocabulary terms as of the 'Linked Data Basic Profile 1.0',
	 * see http://www.w3.org/Submission/2012/02/ for an overview and
	 * bp-vocab.ttl here for the definition of the terms.
	 *
	 */
	object LDBP extends Vocabulary( "http://open-services.net/ns/basicProfile#" ) {
		val Container = LDBP \ "Container"
		val Page = LDBP \ "Page"
		val membershipPredicate = prop("membershipPredicate")
		val membershipSubject = prop("membershipSubject")
		val pageOf = prop("pageOf")
		val nextPage = prop("nextPage")
		val nexcontainerSortPredicatestPage = prop("containerSortPredicates")
	}

	/** 
	 * Dublin core terms as of http://dublincore.org/documents/dcmi-terms/ 
	 * 
	 */
	object DC extends Vocabulary( "http://purl.org/dc/terms/" ) {
		val title = propStr( "title" )
		val modified = propStr( "modified" )
		val description = propStr( "description" )
	}
	
	/** 
	 * Schema.org terms as of http://schema.org/docs/full.html
	 * 
	 * @param http well isn't it obvious
	 * @return dunno
	 */
	object SchemaOrg extends Vocabulary( "http://schema.org/" ) {
		val Thing = SchemaOrg \ "Thing"
	}
	
}
