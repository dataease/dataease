# OnDemandList and OnDemandGrid

## OnDemandList

OnDemandList extends List to provide on-demand lazy loading of data as the user
scrolls through the list. This provides a seamless, intuitive interface for
viewing large sets of data in scalable manner.

```js
require(["dgrid/OnDemandList", "put-selector/put"], function(OnDemandList, put){
    var list = new OnDemandList({
        store: myStore, // a Dojo object store
        renderRow: function(object, options){
            // Override renderRow to accommodate store items
            return put("div", object.myField);
        }
    });
});
```

## OnDemandGrid

This module is simply the composition of [Grid](Grid.md) and OnDemandList. For example:

```js
define(["dgrid/OnDemandGrid"], function(OnDemandGrid){
    grid = new OnDemandGrid({
        store: myStore, // a Dojo object store
        columns: [
            {label: "Column 1", field: "col1", sortable: false},
            {label: "Column 2", field: "col2"},
            // ...
        ]
    }, "grid");
    // ...
});
```

## Usage

OnDemandList inherits the \_StoreMixin module, which implements a basis for
interacting with a [Dojo object
store](http://dojotoolkit.org/reference-guide/dojo/store.html) for querying of
data. At minimum, this implementation expects a store which supports the `get`,
`getIdentity`, and `query` methods, and whose items include unique identifiers.

OnDemandList requires that a store be specified via the `store` property, and
will call the `query` method on the store to retrieve the data to be rendered.
OnDemandList will call `query` with `start` and `count` options so as to only
retrieve the necessary objects needed to render the visible rows. As the list or
grid is scrolled, more `query` calls will be made to retrieve additional rows,
and previous rows will be pruned from the DOM as they are scrolled well out of
view.

When working with a writable store, for best results, the store should return
query results with an `observe` method, which enables the list to keep its
display up to date with any changes that occur in the store after the items are
rendered. The
[`dojo/store/Observable`](http://dojotoolkit.org/reference-guide/dojo/store/Observable.html)
module can prove useful for adding this functionality.

## APIs

### Property Summary

Property | Description
-------- | -----------
`minRowsPerPage` | The minimum number of items that will be requested at one time while scrolling; default is `25`.
`maxRowsPerPage` | The maximum number of items that will be requested at one time while scrolling; default is `250`.
`maxEmptySpace` | The maximum size (in pixels) of unrendered space below or above the rendered portion of the component; default is `Infinity`, which indicates that the size of unrendered space should approximate the total space which would be occupied by all items in the result set.
`bufferRows` | The number of rows to keep rendered beyond each end of the currently visible area of the component; default is `10`.
`farOffRemoval` | The minimum distance (in pixels) which must exist between the currently visible area and previously-rendered rows before they are removed from the DOM; default is `2000`, but this can be adjusted based on a known maximum height in cases where keeping fewer nodes in the DOM is preferable.
`queryRowsOverlap` | Specifies the number of items to "overlap" between queries, which helps ensure consistency of observed updates to items at page boundaries. The default is `0`.
`pagingMethod` | Specifies the method from the `dgrid/util/misc` module to use for throttling the scroll handler; defaults to `"debounce"` to wait until scrolling stops, but can also be set to `"throttleDelayed"` to load even as scrolling continues.
`pagingDelay` | Specifies the number of milliseconds to debounce or throttle scroll handler calls (and thus also potential store requests); default is `15`.
`keepScrollPosition` | Whether to attempt preserving scroll position by default on all `refresh` operations (including sort); defaults to `false`.  This can also be set per-refresh by passing an object with a `keepScrollPosition` property to the `refresh` function.

*Inherited from _StoreMixin*

Property | Description
-------- | -----------
`noDataMessage` | An optional message to be displayed when no results are returned by a query.
`loadingMessage` | An optional message to be displayed in the loading node which appears when a new page of results is requested.
`getBeforePut` | If `true` (the default), any `save` operations will re-fetch the item from the store via a `get` call, before applying changes represented by dirty data.
`query` | An object to be passed when issuing store queries, which may contain filter criteria.
`queryOptions` | An object to be passed along with `query` when issuing store queries.  Note that the standard `start`, `count`, and `sort` properties are already managed by OnDemandList itself.
`store` | An instance of a `dojo/store` implementation, from which to fetch data.

### Method Summary 

Method | Description
------ | -----------
`refresh(options)` | Clears the grid and re-queries the store for data.  If `keepScrollPosition` is `true` on either the instance or the `options` passed to `refresh`, an attempt will be made to preserve the current scroll position.  OnDemandList returns a promise from `refresh`, which resolves when items in view finish rendering.  The promise resolves with the QueryResults that were rendered.

*Inherited from _StoreMixin*

Method | Description
------ | -----------
`set("query", query[, queryOptions])` | Specifies a new `query` object (and optionally, also `queryOptions`) to be used by the list when issuing queries to the store.
`set("store", store[, query[, queryOptions]])` | Specifies a new store (and optionally, also `query` and `queryOptions`) for the list to reference.
`set("sort", property, descending)` | \_StoreMixin's version of this defers sorting to the store.
`updateDirty(id, field, value)` | Updates an entry in the component's dirty data hash, to be persisted to the store on the next call to `save()`.
`save()` | Instructs the list to relay any dirty data back to the store. Returns a promise which resolves when all necessary put operations have completed successfully (even if the store operates synchronously).
`revert()` | Clears the dirty data hash without updating the store, and refreshes the component.

## Events

### dgrid-refresh-complete

As of dgrid 0.3.5, OnDemandList emits a `dgrid-refresh-complete` event when
results finish rendering as the result of a `refresh` call (also including the
initial render). The event includes the following properties:

* `grid`: The Grid (or List) instance responsible for firing the event
* `results`: The QueryResults returned by the store query

### dgrid-error

If something should go wrong within OnDemandList's logic, \_StoreMixin is
equipped to emit a `dgrid-error` event, including the following properties:

* `grid`: The Grid (or List) instance on which the error occurred
* `error`: The error which occurred

OnDemandList emits a `dgrid-error` event for any error that occurs while
querying the store for data in reaction to scrolling or a `refresh` call.