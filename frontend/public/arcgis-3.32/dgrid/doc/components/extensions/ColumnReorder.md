# ColumnReorder

The ColumnReorder extension adds the ability to reorder the columns of a grid
via drag'n'drop operations on column headers. In the case of more complex grid
structures, note that reordering is only supported between columns within the
same subrow or columnset.

```js
require([
    "dojo/_base/declare", "dgrid/OnDemandGrid", "dgrid/extensions/ColumnReorder"
], function(declare, OnDemandGrid, ColumnReorder, declare){
    var grid = new (declare([OnDemandGrid, ColumnReorder]))({
        columns: {
            col1: {
                label: "Column1",
                reorderable: false
            },
            col2: "Column 2",
            col3: "Column 3"
        }
    }, "grid");
    // ...
});
```

## Additional Column Definition Properties

The ColumnReorder extension supports the following additional column definition properties.

Property | Description
-------- | -----------
`reorderable` | If explicitly set to `false`, the given column's header node will not be treated as a viable DnD item, preventing it from being reordered.  **Note:** this is generally only useful for columns at either end of a structure, since if it is surrounded by neighbors which are reorderable, it will not prevent reordering of the surrounding columns.

## Events

The ColumnReorder extension emits a `dgrid-columnreorder` event whenever the
user triggers a column reorder operation. This event includes the following
properties:

* `grid`: The grid instance in which the reorder operation took place
* `subRow`: The specific subrow in which the reorder operation took place,
  reflecting the new order
* `column`: The column definition object representing the column which was
  reordered

This event bubbles and is cancelable; if canceled, the column structure prior
to the reorder operation will be preserved.