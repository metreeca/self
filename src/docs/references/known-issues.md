---
title: Known Issues
---

# Interoperability

## SPARQL Compliance

Metreeca/Self is expected to interact with any SPARQL 1.1 endpoint, but we are currently testing mainly with RDF4J 2. [Issue reports](https://github.com/metreeca/self/issues) are welcome…

# Performance

Metreeca/Self is currently optimized for interactive activities on local mid-sized datasets: linked data navigation and option filtering will scale comfortably, but performance is likely to degrade when trying to apply other features to large datasets.

## Reverse Links

Introspection of reverse properties to be included in the left-hand column of the path picker dialog may be speeded up by enabling object-centric indexes in the triple store (e.g. _object/subject/predicate_ or _object/predicate/subject_).

## Full-Text and Range Searches

Optimizable by the SPARQL endpoint with dedicated indexing support, but that's outside our control.

## Aggregate Summaries and Filtering

Aggregate numeric summaries and filtering on aggregate values are inherently resource-heavy and possibly beyond optimization ;-(

# User Interface

- Safari isn't yet able to assign meaningful names to exported files: until a workaround is found, the user is expected to rename them manually.
