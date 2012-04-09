# PayGoo - Pay As You GO data Objects

An object-orient implementation of '[Linked Data Basic Profile](http://www.w3.org/Submission/2012/SUBM-ldbp-20120326/ "Linked Data Basic Profile 1.0")'-compliant containers/resources.

## Usage

After you got the source somewhere local (via git clone or download facility) you want to compile it:

	scalac -cp lib/scardf-0.6-SNAPSHOT.jar:lib/joda-time-1.6.jar info.paygoo.core.scala

Then you can run the built-in test:

	scala -cp lib/scardf-0.6-SNAPSHOT.jar:lib/joda-time-1.6.jar info.paygoo.core.PayGooResource

...which should yield something like the following:

	<div>About &lt;a href=&quot;http://data.example.com/test#it&quot;&gt;pg0&lt;/a&gt;, last updated 2012-04-09</div>
	{"base" : "http:\/\/data.example.com\/test#", "label" : "pg0", "date" : 2012-04-09}
	base=http://data.example.com/test#, label=pg0, date=2012-04-09
	<http://data.example.com/test#it> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/Thing> .
	<http://data.example.com/test#it> <http://purl.org/dc/terms/title> "pg0" .
	<http://data.example.com/test#it> <http://purl.org/dc/terms/date> "2012-04-09"^^<http://www.w3.org/2001/XMLSchema#date> .

	[PayGooResource: base=http://data.example.com/test# | label=pg0 | date=2012-04-09]

## Dependencies

* Scala 2.9.1
* [scardf](http://code.google.com/p/scardf/ "Scala RDF API - Google Project Hosting")
* [spray](spray.cc "A suite of lightweight Scala libraries for building and consuming RESTful web services on top of Akka")

## License

Public Domain.