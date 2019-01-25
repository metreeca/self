---
title: 		System Handbook
excerpt:	System requirements, installation and other system-related info
---

# System Requirements

## Browser

Metreeca/Self is a client-side web application: to run it you need the latest version of a supported web browser ([Firefox](http://www.mozilla.org/firefox/new/), [Chrome](https://www.google.com/chrome/), [Safari](https://www.apple.com/safari/), [Edge](http://microsoft.com/en-us/windows/microsoft-edge), [Explorer](http://windows.microsoft.com/en-us/internet-explorer/download-ie)); unsupported browsers may work as well, but they are not tested and your mileage may vary…

## SPARQL Endpoint

Metreeca/Self requires the SPARQL endpoint to fully support [CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) requests.

In order to support access to HTTP authenticated endpoints, it also requires the CORS implementation to reply with an origin specific `Access-Control-Allow-Origin` header, that is:

```
Access-Control-Allow-Origin: http://example.org/
```

rather than:

```
Access-Control-Allow-Origin: *
```

If these these requirements are not met, consider routing SPARQL requests from Metreeca/Self through a compliant CORS-enabled proxy.

# Installation

Just copy Metreeca/Self assets to a web server for static deployment.

# Embedding

Metreeca/Self may be embedded within host HTML pages to provide faceted search, linked data navigation and data-driven infographics for third-party sites.

To get started with embedded Metreeca/Self:

1. include the `driver.min.js` script from your Metreeca/Self installation (e.g. <https://demo.metreeca.com/apps/self/driver.min.js>) anywhere in your page;
2. embed one or more Metreeca/Self instances using `iframe` elements annotated with `data-*` attributes defining content and layout.

```html
<script type="application/javascript"
 src="https://demo.metreeca.com/apps/self/driver.min.js"></script>
```

```html
<iframe [name="<instance name>"]
 src="https://demo.metreeca.com/apps/self/"
 [data-<content option>="<value>"]*
 [data-<layout option>="<value>"]*
></iframe>
```

Basic, cut-and-paste ready samples are available at <https://demo.metreeca.com/apps/self/samples/>.

## Content Options

Content options control what an embedded Metreeca/Self instance presents to the user.

`document = <Document URL>`

: Presents a complex pre-configured report available as an exported document at the given URL (see [Exporting Reports](../tutorials/search-and-analysis#exporting-reports)). Make sure the hosting page is able to retrieve the document, as the [same-origin policy](https://developer.mozilla.org/en-US/docs/Web/Security/Same-origin_policy) applies.

`endpoint = <SPARQL Endpoint URL>`

: Connects to the SPARQL endpoint at the given URL and presents the available key collections (see [Identify Key Collections](../how-tos/improve-user-experience#identify-key-collections)), unless either the `collection` or the `resource` option is also specified.

`collection = <RDF Type IRI>`

: Presents a list of resources with the given RDF type extracted from the endpoint specified by the `endpoint` option.

`resource = <RDF Resource IRI>`

: Presents linked data information about the given RDF resource extracted from the endpoint specified by the `endpoint` option.

`label = <RDF Resource IRI>`

: Provides a human-readable label for the field holding the presented information, to be used in the fields area of the user interface and in browser history entries.

## Layout Options

Layout options control how the content is visually presented to the user.

Unless otherwise specified, layout options default to the first listed value.

 `mode = catalog | navigator | visualizer`

: Controls the overall layout of the interface and selectively enables required control widgets. Available modes are optimized respectively for faceted catalog search, linked data navigation and infographics visualization.

`toolbar = bottom | none`

: Controls the positioning of the main toolbar. 
​ Defaults to `none`, if `mode` is set to `visualizer`.

`fields = top | none`

: Controls the positioning of the fields area. 
​ Defaults to *none*, if `mode` is set either to `navigator` or `visualizer`.

 `facets = right | left | none`

: Controls the positioning of the facets area. 
​ Defaults to `none`, if `mode` is set either to `navigator` or `visualizer`.

## Markup Integration

The host page may enable the user to change the content of an embedded Metreeca/Self instance by including `anchor` elements annotated with `data-*` attributes defining the required content.

```html
<a target="<instance name>" 
 [data-<default>="true"]
 [data-<content option="<value>"]*
>label</a>
```

If the `data-default` attribute is set to `true`, the content options for the anchor define also the initial content of the target Metreeca/Self instance.

## Javascript Integration

The host page may programmatically change the content of an embedded Metreeca/Self instance by invoking the *metreeca* JavaScript driver function, passing the name of the target instance and either the URL of a complex pre-configured report or a JavaScript object defining the required content using the same [content options](#content-options) previously introduced in relation to *data-\** attributes.

```javascript
metreeca("<instance name>",
 <document URL> | { [‹content option›: "<value>"]* });
```

# Known Issues

## Interoperability

### SPARQL Compliance

Metreeca/Self is expected to interact with any SPARQL 1.1 endpoint, but we are currently testing mainly with RDF4J 2. [Issue reports](https://github.com/metreeca/self/issues) are welcome…

## Performance

Metreeca/Self is currently optimized for interactive activities on local mid-sized datasets: linked data navigation and option filtering will scale comfortably, but performance is likely to degrade when trying to apply other features to large datasets.

### Reverse Links

Introspection of reverse properties to be included in the left-hand column of the path picker dialog may be speeded up by enabling object-centric indexes in the triple store (e.g. _object/subject/predicate_ or _object/predicate/subject_).

### Full-Text and Range Searches

Optimizable by the SPARQL endpoint with dedicated indexing support, but that's outside our control.

### Aggregate Summaries and Filtering

Aggregate numeric summaries and filtering on aggregate values are inherently resource-heavy and possibly beyond optimization ;-(

## User Interface

- Safari isn't yet able to assign meaningful names to exported files: until a workaround is found, the user is expected to rename them manually.
