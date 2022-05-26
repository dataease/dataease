# SingleQuery

The SingleQuery extension performs a single store request with no range information.  Its implementation is thus
much simpler than OnDemandList and Pagination, and is suitable primarily for small in-memory result sets.

SingleQuery is constructed similarly to Pagination, in that it should be mixed into List or Grid
(*not* OnDemandList or OnDemandGrid).  Since it requests and renders all items at once, it also works well
in conjunction with the `dgrid-autoheight` class so that the grid's height is determined by its data.

```js
require([
    'dojo/_base/declare',
    'dgrid/Grid',
    'dgrid/extensions/SingleQuery'
], function (declare, Grid, SingleQuery) {
    var grid = new (declare([ Grid, SingleQuery ]))({
        className: 'dgrid-autoheight',
        collection: myStore,
        columns: myColumns
    }, 'grid');
    // ...
});
```

**Note:** SingleQuery is *only* recommended for use with reasonably small in-memory stores,
generally with a few hundred items at most.  Anything larger could place unwanted stress on services or the browser.

## API

The SingleQuery extension does not provide any additional APIs, but inherits properties and methods
supported by `dgrid/_StoreMixin`; for details, see the tables for properties and
methods inherited from \_StoreMixin in the documentation for
[OnDemandList and OnDemandGrid](../core-components/OnDemandList-and-OnDemandGrid.md#apis).

## Events

Like OnDemandList and OnDemandGrid, the SingleQuery extension will emit the following events:

* `dgrid-refresh-complete` when results finish rendering as the result of a
  `refresh` call (also including the initial render)
* `dgrid-error` when an error occurs while attempting to query the store in
  response to user interaction
