---
title: 		    How To Improve User Experience
excerpt:	    Usability guidelines and other configuration tips
redirect_from:  /how-tos/improve-user-experience
---

Metreeca/Self will search and navigate generic RDF data from any SPARQL 1.1 endpoint, but user experience can be improved by sticking to the following guidelines.

# Annotate Content

## Provide Labels

Provide human-readable labels for every graph entity by annotating them as:

    <uri> rdfs:label "label" . 
    <uri> rdfs:label "label"@en .

Metreeca/Self will scan for available generic and english-tagged labels, in that order.

If no labels are provided, Metreeca/Self will try to infer human-readable labels for URIs according to well-known slash/hash patterns (e.g. 'property' for [http://example.com/schema\#property](http://example.com/)).

## Provide Comments

Where relevant, provide human-readable descriptions for graph entities by annotating them as:

    <uri> rdfs:comment "comment" . 
    <uri> rdfs:comment "comment"@en .

Metreeca/Self will scan for available generic and english-tagged notes, in that order.

## Provide Pictures

Where relevant, provide visual depictions for graph entities by annotating them as:

    <uri> db:thumbnail <picture-uri>/"picture-uri".   
    <uri> schema:image <picture-uri>/"picture-uri".    
    <uri> foaf:logo <picture-uri>/"picture-uri".
    <uri> foaf:depiction <picture-uri>/"picture-uri".

Metreeca/Self will scan for available pictures in this order.

## Provide Coordinates

Where relevant, provide geographic coordinates for graph entities by annotating them using the [Basic Geo (WGS84 lat/long) Vocabulary](http://www.w3.org/2003/01/geo/#vocabulary) as:

    <uri> geo:lat "latitude"; geo:long "longitude" .

## Include Ontologies

Upload relevant ontologies to the endpoint: Metreeca/Self will use human-readable labels and notes for classes and predicates to configure fields and facets names and linked data pages.

# Assist Navigation

## Identify Key Collections

When opening a new report, Metreeca/Self will present the user with a springboard page, listing collections identified by introspecting *rdf:type* info from the endpoint.

To fine-tune what's presented to the user or to improve performance on large datasets, you may explicitly identify RDF classes as key collections for your *dataset* by annotating them as:

    <{dataset}> <http://rdfs.org/ns/void#rootResource> <{class}> .

## Include Inverse Links

Metreeca/Self supports the creation of property paths involving inverse steps (*\^&lt;predicate&gt;*), but they may be very confusing even for experienced users: wherever feasible, explicitly document bidirectional relationships between graph entities using pairs of inverse predicates.

## Avoid Blank Nodes

Until a consistent and efficient way to skolemize SPARQL results sets is found, blank nodes should be avoided, as Metreeca/Self won't be able to navigate past them.

Wherever feasible, replace blank nodes with named resources either identified by dereferenceable *http:* URIs or tagged within a suitable URI namespace (e.g. [RFC 4151 - The 'tag' URI Scheme](http://tools.ietf.org/html/rfc4151), [RFC 4122 - A Universally Unique IDentifier (UUID) URN Namespace](http://tools.ietf.org/html/rfc4122)).

In any case, provide human-readable labels summarizing the information hidden beyond blank nodes or embedded within structured intermediate resources.
