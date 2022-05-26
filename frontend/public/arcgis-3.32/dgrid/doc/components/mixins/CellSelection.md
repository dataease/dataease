# CellSelection

The CellSelection mixin extends upon the functionality of the [Selection](Selection.md) mixin
to provide selection at the cell level instead.

```js
require([
    "dojo/_base/declare", "dgrid/OnDemandGrid", "dgrid/CellSelection"
], function(declare, OnDemandGrid, CellSelection){
    var grid = new (declare([OnDemandGrid, CellSelection]))({
        selectionMode: "single",
        // ...
    });
});
```

## Differences from Selection mixin

* The `selection` object stores a nested hash, where the outer hash is
  keyed by item ID and the inner hashes are keyed by column ID.
* The `dgrid-select` and `dgrid-deselect` events still fire, but include a
  `cells` property containing an array of cell objects, rather than a `rows`
  property.
* Whereas Selection's `select`, `deselect`, and `isSelected` methods look up the
  passed argument via List's `row` method, CellSelection looks it up via Grid's
  `cell` method.
* The `allowSelect` method is passed a cell object instead of a row object.