---
title: How To Embed Into a HTML Page
---

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

# Embedding Options

## Content Options

Content options control what an embedded Metreeca/Self instance presents to the user.

`document = <Document URL>`

: Presents a complex pre-configured report available as an exported document at the given URL (see [Exporting Reports](../tutorials/search-and-analysis/index.md#exporting-reports)). Make sure the hosting page is able to retrieve the document, as the [same-origin policy](https://developer.mozilla.org/en-US/docs/Web/Security/Same-origin_policy) applies.

`endpoint = <SPARQL Endpoint URL>`

: Connects to the SPARQL endpoint at the given URL and presents the available key collections (see [Identify Key Collections](../how-to/improve-user-experience.md#identify-key-collections)), unless either the `collection` or the `resource` option is also specified.

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

# Host Page Integration

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
