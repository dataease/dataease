# List

This provides the basic facilities for taking an array of objects and rendering
them as rows of HTML in a scrollable area. This will automatically include touch
scrolling capabilities (via the `TouchScroll` module) on mobile devices.

The List module can be used to render an array of data as follows:

```js
require(["dgrid/List"], function(List){
    // attach to a DOM element indicated by its ID
    var list = new List({}, "list");
    // render some data
    list.renderArray(arrayOfData);
});
```

## APIs

The base List class (inherited by all other classes) exposes specific methods
and maintains the various key DOM / property references.

### Constructor

As seen in the example above, the `List` constructor (as well any derivative
dgrid constructors, e.g. `Grid`, `OnDemandGrid`, etc.) can accept 2 properties:
an object containing initial properties to set on the instance, and a DOM node
or string referencing a DOM node's ID (the `srcNodeRef`).
If a `srcNodeRef` is provided, the given DOM node will become the instance's
`domNode`; otherwise the instance will need to be placed into the document manually.

Each dgrid instance's `domNode` is given a unique ID, based on one of the
following criteria:

* If a `srcNodeRef` was provided and has an ID, this ID will be used
* If an `id` was specified in the arguments object, that will be used
* Otherwise, an ID will be automatically generated, in the form `dgrid_<number>`

It is important to note that DOM node IDs are not allowed to contain spaces;
therefore, one should never include spaces in the `id` property.

### Property Summary

Property | Description
-------- | -----------
`domNode` | The top-level DOM node of the component (much like the `domNode` property of Dijit widgets).
`headerNode` | The DOM node representing the header region; mainly applicable to grid components.
`bodyNode` | The DOM node representing the body region (the area which will show rows for each item).
`contentNode` | The DOM node immediately under the `bodyNode`, which may potentially be scrolled to accommodate more content than the component's height will allow to fit.
`footerNode` | A DOM node appearing below the `bodyNode`; initially empty and not displayed by default.
`addUiClasses` | Boolean indicating whether to add `ui-` classes (corresponding to jQuery UI) to nodes within the grid.  The default is `true`.
`className` | A class (or classes, space- or period-delimited) to be set on the instance's `domNode` in addition to the defaults; if an existing DOM node is being passed to the constructor, extra classes may alternatively be specified via its `class` attribute or `className` property
`cleanAddedRules` | Whether to clean up CSS rules added via the `addCssRule` method when the instance is destroyed; defaults to `true`.
`cleanEmptyObservers` | Whether to clean up observers for result sets that are or become empty; defaults to `true` but is set to `false` by the `tree` plugin due to special considerations.
`shouldObserveStore` | Whether this instance should observe any observable store it is passed; defaults to `true`.
`showHeader` | Whether to display the header area; normally `false` for lists and `true` for grids.  Can be reset later via `set("showHeader", ...)`.
`showFooter` | Whether to display the footer area; `false` by default, but enabled and used by some extensions (e.g. Pagination).  Can be reset later via `set("showFooter", ...)`.
`sort` | How to initially sort the grid; may be either a string referencing a field name, or an array of objects with `attribute` and `descending` properties.  See also `set("sort", ...)` below.
`useTouchScroll` | Whether to use the `TouchScroll` module if touch support is available on the device, or defer to native capabilities if they exist; `true` by default for backwards compatibility.  Setting this to `false` is particularly useful for devices which support both touch and mouse input.

### Method Summary

Method | Description
------ | -------------
`get(property)` | Returns the value of a given property. Supports custom getter implementations via the pattern `_getProperty` (which would map to `get("property")`).
`set(property, value)` | Sets the value of a given property. Supports custom setter implementations via the pattern `_setProperty` (which would map to `set("property", ...)`).
`set(object)` | Alternate means of invoking `set`, equivalent to calling `set(property, value)` for each key/value pair in the provided object.
`row(target)` | This will look up the requested row and return a Row object. The single parameter may be a DOM event, DOM node, or in the case of store-backed components, a data object or its ID. The returned Row object has the following properties: `id` - the data object's id, `data`- the data object represented by the row, `element`- the row's DOM element
`on(event, listener)` | Basic event listener functionality; simply delegates to the top-level DOM element of the List, using standard `dojo/on` behavior.
`up(row[, steps])` | Given a row object (or something that resolves to one via the `row` method), returns a row object representing the row located `steps` rows above (where `steps` defaults to `1`).
`down(row[, steps])` | Same as `up()`, but operating downward.
`renderArray(array, beforeNode, options)` | This can be called to render an array directly into the list.  The `beforeNode` parameter can be used to render at a specific point in the list.  Note that when using store-backed components, this is called automatically.
`renderRow(item, options)` | This method can be overridden to provide custom rendering logic for rows.  (The [Grid](Grid.md) module actually overrides this method.) `item` refers to the record from the array or store for the row.
`removeRow(rowElement, justCleanup)` | This method can be extended/aspected to perform cleanup logic when an individual row is removed.
`set("sort", property, descending)` | This can be called to sort the List by a given property; if `true` is passed for `descending`, the sort will be in descending order. Multiple sort criteria can be specified in the format expected by stores' `queryOptions` (an array of objects with `attribute` and `descending` properties); this is also the format `get("sort")` will return in. The [Grid](Grid.md) and [OnDemandList)(OnDemandList-and-OnDemandGrid.md) modules further extend sort functionality.
`scrollTo(options)` | scrolls to a given point in the grid.  Accepts `x` and `y` properties; if one is not given, position along that axis is not modified.
`getScrollPosition()` | returns the current position that the grid is scrolled to, in the form of an object containing `x` and `y` properties.
`addCssRule(selector, css)` | Programmatically adds styles for a given CSS selector by injecting the given rule string into a stylesheet in the document.  Returns a handle with a`remove` function, which can be called to later remove the added style rule.  Styles added via this method will be removed when the instance is destroyed if `cleanAddedRules` is set to `true`.