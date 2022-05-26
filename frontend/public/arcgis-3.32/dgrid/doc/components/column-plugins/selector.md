# selector

Used in conjunction with the [Selection](../mixins/Selection.md) mixin, the selector plugin dedicates
a column to the purpose of rendering a selector component, providing alternate
means for selecting and deselecting rows in a grid.

```js
require([
    "dojo/_base/declare", "dgrid/OnDemandGrid", "dgrid/Selection", "dgrid/selector"
], function(declare, OnDemandGrid, Selection, selector){
    var grid = new (declare([OnDemandGrid, Selection]))({
        store: myStore,
        selectionMode: "single",
        columns: {
            col1: selector({ label: "Select", selectorType: "radio" }),
            col2: "Column 2"
        }
    }, "grid");
    // ...
});
```

## Usage 

A selector column can be used to allow selection even in a grid where
`selectionMode` is set to `none`, in which case the controls in the selector
column will be the only means by which a user may select or deselect rows.

## Additional Column Definition Properties

The selector plugin supports the following additional column definition properties.

Property | Description
-------- | -----------
`disabled(item)` | A function which, provided the item for a particular row, should return `true` if the selector control should be disabled for that row.  Returning `true` will also prevent the row in question from being directly selected via the [Selection](../mixins/Selection.md) mixin.
`selectorType` | Specifies the type of selector component to use.  Defaults to `"checkbox"`, but `"radio"` may also be specified, as a more appropriate choice for grids in single-selection mode.

Alternatively, `selectorType` may be specified as the second argument to the
`selector` function instead of including it within the column definition.