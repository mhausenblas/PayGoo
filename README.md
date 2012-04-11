# PayGoo - Pay As You GO data Objects

An object-orient implementation of '[Linked Data Basic Profile](http://www.w3.org/Submission/2012/SUBM-ldbp-20120326/ "Linked Data Basic Profile 1.0")'-compliant containers/resources.

## Usage

### Bare-bone 

After you got the source somewhere local (via git clone or download facility) you want to compile it:

	scalac -cp lib/scardf-0.6-SNAPSHOT.jar:lib/joda-time-1.6.jar info.paygoo.core.scala

Then you can run the built-in test like so:

	scala -cp lib/scardf-0.6-SNAPSHOT.jar:lib/joda-time-1.6.jar info.paygoo.core.PayGooContainer

... which should yield something like the following:

	[PayGooContainer: id=http://data.example.com/#container | label=a simple container | modified=2012-04-11]

	As HTML:
	<div>About <a href='http://data.example.com/#container'>a simple container</a>, last updated 2012-04-11 containing: <ul><li><a href='http://data.example.com/#res1'>resource 1</a></li><li><a href='http://data.example.com/#res2'>resource 2</a></li></ul></div>

	As JSON:
	{"container" : {"id" : "http:\/\/data.example.com\/#container", "label" : "a simple container", "modified" : "2012-04-11"}, "members" : ["http:\/\/data.example.com\/#res1", "http:\/\/data.example.com\/#res2"]}

	As plain text:
	id=http://data.example.com/#container, label=a simple container, modified=2012-04-11, containing: [id=http://data.example.com/#res1, label=resource 1, modified=2012-04-11id=http://data.example.com/#res2, label=resource 2, modified=2012-04-11]

	As RDF/NTriple:
	<http://data.example.com/#container> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://open-services.net/ns/basicProfile#Container> .
	<http://data.example.com/#container> <http://www.w3.org/2000/01/rdf-schema#member> "http://data.example.com/#res2" .
	<http://data.example.com/#container> <http://purl.org/dc/terms/title> "a simple container" .
	<http://data.example.com/#container> <http://www.w3.org/2000/01/rdf-schema#member> "http://data.example.com/#res1" .
	<http://data.example.com/#container> <http://purl.org/dc/terms/modified> "2012-04-11" .

### RESTful interaction

Now to something more fun. Some RESTful interaction. Compile the server like so:

	scalac -cp lib/scardf-0.6-SNAPSHOT.jar:lib/joda-time-1.6.jar info.paygoo.server.scala

And then run it with:

	scala -cp lib/scardf-0.6-SNAPSHOT.jar:lib/joda-time-1.6.jar info.paygoo.server.PayGooServer

After that, point your Web browser to `http://localhost:6969/bpc0` and you should see an HTML rendering. If you prefer some command-line noodling using curl, here you go:

	$ curl -H "Accept:application/json" http://localhost:6969/bpc0
	
	{"container" : {"id" : "http:\/\/localhost:6969\/bpc0", "label" : "container 0", "modified" : "2012-04-11"}, "members" : ["http:\/\/localhost:6969\/res1", "http:\/\/localhost:6969\/res2"]}

	$ curl http://localhost:6969/bpc0?ntriple
	
	<http://localhost:6969/bpc0> <http://purl.org/dc/terms/title> "container 0" .
	<http://localhost:6969/bpc0> <http://www.w3.org/2000/01/rdf-schema#member> "http://localhost:6969/res1" .
	<http://localhost:6969/bpc0> <http://www.w3.org/2000/01/rdf-schema#member> "http://localhost:6969/res2" .
	<http://localhost:6969/bpc0> <http://purl.org/dc/terms/modified> "2012-04-11" .
	<http://localhost:6969/bpc0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://open-services.net/ns/basicProfile#Container> .

	$ curl -H "Accept:application/json" http://localhost:6969/bpc0?html

	<div>About <a href='http://localhost:6969/bpc0'>container 0</a>, last updated 2012-04-11 containing: <ul><li><a href='http://localhost:6969/res1'>resource 1</a></li><li><a href='http://localhost:6969/res2'>resource 2</a></li></ul></div>
	
You get the idea, right? Either you tell the PayGooServer via [conneg](http://en.wikipedia.org/wiki/Content_negotiation "Content negotiation - Wikipedia, the free encyclopedia") what format you prefer or you can specify it explicitly by appending a query parameter `?xxx` where xxx can be one of the following: `json`, `html` or `ntriple` which overwrites the conneg.
	
	

## Dependencies

* Tested against Scala 2.9.1
* Using [scardf](http://code.google.com/p/scardf/ "Scala RDF API - Google Project Hosting") for RDF parsing and serialisation

## License

Public Domain.