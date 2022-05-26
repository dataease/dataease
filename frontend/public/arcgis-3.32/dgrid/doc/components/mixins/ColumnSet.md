# ColumnSet

The ColumnSet module provides functionality which divides a grid's columns into
multiple distinct sets, each of which manage their columns' horizontal scrolling
independently. This makes it possible to keep certain columns in view even while
others are scrolled out of viewing range.

When mixing in ColumnSet, instead of specifying `columns` or `subRows`, one
specifies `columnSets`, which is essentially an array of `subRows`. For example,
in pseudocode:

```js
require([
    "dojo/_base/declare", "dgrid/OnDemandGrid", "dgrid/ColumnSet"
], function(declare, OnDemandGrid, ColumnSet){
    var grid = new (declare([OnDemandGrid, ColumnSet]))({
        columnSets: [
            // left columnset
            [
                [
                    { /* columnset 1, subrow 1, column 1 */ },
                    { /* columnset 1, subrow 1, column 2 */ }
                ],
                [
                    { /* columnset 1, subrow 2, column 1 */ },
                    { /* columnset 1, subrow 2, column 2 */ }
                ]
            ],
            // right columnset
            [
                [
                    { /* columnset 2, subrow 1, column 1 */ },
                    { /* columnset 2, subrow 1, column 2 */ }
                ],
                [
                    { /* columnset 2, subrow 2, column 1 */ },
                    { /* columnset 2, subrow 2, column 2 */ }
                ]
            ]
        ],
        // ...
    }, "grid");
});
```

More concrete examples of ColumnSet usage can be found in the
`complex_column.html` test page.

## APIs

The ColumnSet mixin supports the following additional instance methods.

### Method Summary
Method | Description
------ | -----------
`styleColumnSet(columnsetId, css)` | Programmatically adds styles to a columnset, by injecting a rule into a stylesheet in the document.  Returns a handle with a `remove` function, which can be called to later remove the added style rule.  Styles added via this method will be removed when the instance is destroyed if `cleanAddedRules` is set to `true`.