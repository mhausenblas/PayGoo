# PayGoo - Pay As You GO data Objects

An object-orient implementation of '[Linked Data Basic Profile](http://www.w3.org/Submission/2012/SUBM-ldbp-20120326/ "Linked Data Basic Profile 1.0")'-compliant containers/resources.

## Usage

After you got the source somewhere local (via git clone or download facility) you want to compile it:

	scalac -cp lib/scardf-0.6-SNAPSHOT.jar:lib/joda-time-1.6.jar info.paygoo.core.scala

Then you can run the built-in test like so:

	scala -cp lib/scardf-0.6-SNAPSHOT.jar:lib/joda-time-1.6.jar info.paygoo.core.PayGooContainer

... which should yield something like the following:

	As HTML:
	<div>About <a href='http://data.example.com/#container'>a simple container</a>, last updated 2012-04-10 containing: no members</div>

	As JSON:
	{}

	As plain text:
	id=http://data.example.com/#container, label=a simple container, modified=2012-04-10

	As RDF/NTriple:
	<http://data.example.com/#container> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://open-services.net/ns/basicProfile#Container> .
	<http://data.example.com/#container> <http://purl.org/dc/terms/title> "a simple container" .
	<http://data.example.com/#container> <http://purl.org/dc/terms/modified> "2012-04-10" .

	[PayGooContainer: id=http://data.example.com/#container | label=a simple container | modified=2012-04-10]


	Now adding resource 1 and resource 2

	As HTML:
	<div>About <a href='http://data.example.com/#container'>a simple container</a>, last updated 2012-04-10 containing: <div>About <a href='http://data.example.com/#res1'>resource 1</a>, last updated 2012-04-10</div><div>About <a href='http://data.example.com/#res2'>resource 2</a>, last updated 2012-04-10</div></div>

	As JSON:
	{"container" : {"id" : "http:\/\/data.example.com\/#container", "label" : "a simple container", "modified" : "2012-04-10"}, "members" : ["http:\/\/data.example.com\/#res1", "http:\/\/data.example.com\/#res2"]}


	Now removing resource 1

	As HTML:
	<div>About <a href='http://data.example.com/#container'>a simple container</a>, last updated 2012-04-10 containing: <div>About <a href='http://data.example.com/#res2'>resource 2</a>, last updated 2012-04-10</div></div>

	As JSON:
	{"container" : {"id" : "http:\/\/data.example.com\/#container", "label" : "a simple container", "modified" : "2012-04-10"}, "members" : ["http:\/\/data.example.com\/#res2"]}
	
## Dependencies

* Tested against Scala 2.9.1
* Using [scardf](http://code.google.com/p/scardf/ "Scala RDF API - Google Project Hosting") for RDF parsing and serialisation
* Using Scalatra for HTTP interfacing

## License

Public Domain.