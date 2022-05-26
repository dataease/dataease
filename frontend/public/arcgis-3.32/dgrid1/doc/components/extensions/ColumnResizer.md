# ColumnResizer

The ColumnResizer extension can be used to add column resizing functionality (accessible via mouse or touch drag).

```js
require([
    'dojo/_base/declare',
    'dgrid/OnDemandGrid',
    'dgrid/extensions/ColumnResizer'
], function (declare, OnDemandGrid, ColumnResizer) {
    var grid = new (declare([ OnDemandGrid, ColumnResizer ]))({
        columns: {
            col1: {
                label: 'Column1',
                resizable: false
            },
            col2: 'Column 2',
            col3: 'Column 3'
        }
    }, 'grid');
    // ...
});
```

## APIs

The ColumnResizer extension supports the following additional instance properties.

### Property Summary

Property | Description
-------- | -----------
`adjustLastColumn` | If `true` (the default), adjusts the last column's width to "auto" at times where the browser would otherwise stretch all columns to span the grid.
`minWidth` | Minimum width of each column in the grid, in px; default is `40`.

### Method Summary

Method | Description
------ | -----------
`resizeColumnWidth(columnId, width)` | Resizes the width of the column with id `columnId` to be `width` pixels wide.

## Additional Column Definition Properties

The ColumnResizer extension supports the following additional column definition properties.

Property | Description
-------- | -----------
`resizable` | If `false`, prevents the given column from being resized; default is `true`.
`width` | Optional number specifying an initial width (in pixels) for the column.  This is offered as a convenience for dynamically restoring persisted column widths; in most cases, column styling should still be done separately via CSS.

## Events

The ColumnResizer extension will emit a`dgrid-columnresize` event when a column
is resized, which includes the following properties:

* `grid`: The Grid instance in which this event occurred
* `columnId`: The ID of the resized column, which can be used to retrieve the
  column from `grid.columns`
* `width`: The new width of the column
* `parentType`: If the event was triggered by user interaction, this property
  indicates what type of event originally triggered the event

The `dgrid-columnresize` event is cancelable; if canceled, the prior size of
the column will be preserved.