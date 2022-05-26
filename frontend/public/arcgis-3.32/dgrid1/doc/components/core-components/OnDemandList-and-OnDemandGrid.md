# OnDemandList and OnDemandGrid

## OnDemandList

OnDemandList extends List to provide on-demand lazy loading of data as the user
scrolls through the list. This provides a seamless, intuitive interface for
viewing large sets of data in a scalable manner.

```js
require([
    'dgrid/OnDemandList'
], function (OnDemandList) {
    var list = new OnDemandList({
        collection: myStore, // a dstore collection
        renderRow: function (object, options) {
            // Override renderRow to accommodate store items
            var div = document.createElement('div');
            // createTextNode is recommended for inserting data values,
            // to avoid malicious tag injection
            div.appendChild(document.createTextNode(object.myField));
            return div;
        }
    });
});
```

## OnDemandGrid

This module is simply the composition of [Grid](Grid.md) and OnDemandList. For example:

```js
define([ 'dgrid/OnDemandGrid' ], function (OnDemandGrid) {
    grid = new OnDemandGrid({
        collection: myStore, // a dstore store
        columns: [
            { label: 'Column 1', field: 'col1', sortable: false },
            { label: 'Column 2', field: 'col2' },
            // ...
        ]
    }, 'grid');
    // ...
});
```

## Usage

OnDemandList inherits the \_StoreMixin module, which implements a basis for
interacting with a [dstore store](https://github.com/SitePen/dstore) for querying of
data. At minimum, this implementation expects a store which supports the `get`,
`getIdentity`, `fetch`, `fetchRange`, and `sort` methods, and whose items include
unique identifiers.

OnDemandList requires that a store be specified via the `collection` property, and
will call the `fetchRange` method so as to only retrieve the necessary objects
needed to render the visible rows. As the list or grid is scrolled, additional
`fetchRange` calls will be made to retrieve additional rows, and previous rows
will be pruned from the DOM as they are scrolled far out of view.

When working with a writable store, for best results, the store should return
a collection with a `track` method, which enables the list to keep its
display up to date with any changes that occur in the store after the items are
rendered. The
[`dstore/Trackable`](https://github.com/SitePen/dstore/blob/master/docs/Collection.md#track)
module can be used to add this functionality to a store.

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
`collection` | An instance of a dstore implementation, from which to fetch data.  This may be a store instance, or a filtered collection returned from `store.filter()`.
`getBeforePut` | If `true` (the default), any `save` operations will re-fetch the item from the store via a `get` call, before applying changes represented by dirty data.
`loadingMessage` | An optional message to be displayed in the loading node which appears when a new page of results is requested.
`noDataMessage` | An optional message to be displayed when no results are returned by a query.
`shouldTrackCollection` | Whether this instance should track any trackable collection it is passed; default is `true`.

### Method Summary

Method | Description
------ | -----------
`refresh(options)` | Clears the grid and re-queries the store for data.  If `keepScrollPosition` is `true` on either the instance or the `options` passed to `refresh`, an attempt will be made to preserve the current scroll position.  OnDemandList returns a promise from `refresh`, which resolves when items in view finish rendering.  The promise resolves with the QueryResults that were rendered.

*Inherited from _StoreMixin*

Method | Description
------ | -----------
`set("collection", collection)` | Specifies a new collection for the list to reference.
`set("sort", property, descending)` | \_StoreMixin's version of this defers sorting to the store.
`refreshCell(cell)` | Refreshes the given cell (from `Grid#cell`) based on the store and any present dirty data.  This can be used with `shouldTrackCollection: false` to manually control specific cell updates rather than re-rendering entire rows.  (Only applicable when `Grid` is inherited.)
`revert()` | Clears the dirty data hash without updating the store, and refreshes the component.
`save()` | Relays any dirty data back to the store via `put` calls. Returns a promise which resolves when all `put` calls have completed successfully, or rejects if any of the `put` operations fail.  Upon settling, the grid's `dirty` hash will only contain items for which `put` failed (i.e. it will be empty if they all succeed).
`updateDirty(id, field, value)` | Updates an entry in the component's dirty data hash, to be persisted to the store on the next call to `save()`.

## Events

### dgrid-refresh-complete

OnDemandList emits a `dgrid-refresh-complete` event when
results finish rendering as the result of a `refresh` call (also including the
initial render). The event includes the following properties:

* `grid`: The Grid (or List) instance responsible for firing the event

### dgrid-error

If something should go wrong within OnDemandList's logic, \_StoreMixin is
equipped to emit a `dgrid-error` event, including the following properties:

* `grid`: The Grid (or List) instance on which the error occurred
* `error`: The error which occurred

OnDemandList emits a `dgrid-error` event for any error that occurs while
querying the store for data in reaction to scrolling or a `refresh` call.