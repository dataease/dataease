# Selector

Building on the [Selection](Selection.md) mixin, the Selector mixin can be used to
dedicate one or more columns to the purpose of rendering a selector component,
providing alternate means for selecting and deselecting rows in a grid.

```js
require([
    'dojo/_base/declare',
    'dgrid/OnDemandGrid',
    'dgrid/Selector'
], function (declare, OnDemandGrid, Selector) {
    var grid = new (declare([ OnDemandGrid, Selector ]))({
        collection: myStore,
        selectionMode: 'single',
        columns: {
            col1: { label: 'Select', selector: 'radio' },
            col2: 'Column 2'
        }
    }, 'grid');
    // ...
});
```

The selector column does not read from or write to the data store, so the name of the field associated with a
selector column can be chosen without regard to which fields are actually present in the data.

A selector column can be used to allow selection even in a grid where `selectionMode` is set to `none`, in which case
the controls in the selector column will be the only means by which a user may select or deselect rows.

## Additional Column Definition Properties

Property | Description
-------- | -----------
`selector` | If present, specifies the type of selector component to render in each cell of the column.  The value may be `"checkbox"` for a checkbox that allows multiple rows to be selected, `"radio"`, or a function as described below.

When `selector` is set to a function, it is expected to return an `input` DOM node,
and receives the following parameters:

Parameter | Description
--------- | -----------
`column` | The column definition object for the selector column.
`selected` | Boolean indicating whether the current row is selected.
`cell` | The cell's DOM node.
`object` | The data object that the current row represents.

```js
columns: {
    col1: {
        label: 'Select',
        selector: function (column, selected, cell, object) {
            var inputNode;
            // ... render an input component ...
            return inputNode;
        }
    },
    // ...
}
```

If you would like to augment the default input component, call the grid's `_defaultRenderSelectorInput` function to
construct the component first and then make your modifications to the returned DOM node:

```js
selector: function (column, selected, cell, object) {
    var inputNode = column.grid._defaultRenderSelectorInput(column, selected, cell, object);
    domClass.add(inputNode, 'mySelectorClass');
    return inputNode;
}
```
