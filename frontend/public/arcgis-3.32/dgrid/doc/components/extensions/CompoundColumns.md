# CompoundColumns

The CompoundColumns extension adds the ability to define a column structure
which includes additional spanning header cells above the actual columns in the
grid.

```js
require([
    "dojo/_base/declare", "dgrid/OnDemandGrid", "dgrid/extensions/CompoundColumns"
], function(declare, OnDemandGrid, CompoundColumns){
    var compoundGrid = new (declare([OnDemandGrid, CompoundColumns]))({
        columns:[
            { label: "Full Name", children: [
                { label: "Given", children: [
                    { field: "firstname", label: "First" },
                    { field: "middlename", label: "Middle", sortable: false }
                ] },
                { field: "lastname", label: "Last" }
            ] },
            { field: "age", label: "Age" }
        ]
    }, "compoundGrid");
    // ...
});
```

For an example of the CompoundColumns extension in use, see
`dgrid/test/extensions/CompoundColumns.html`.

## Usage

CompoundColumns works by defining any compound header cells first, then defining
the more granular cells via a `children` property of each column definition
object. This can be extended to nest any number of levels.

In the above example, the `columns` array would define a structure which
presents 4 columns (first name, middle name, last name, and age), with an
additional label cell spanning above first and middle name, and yet another
label cell spanning above all 3 name fields combined.

Additionally, the headers on the innermost children can be suppressed by
specifying `showChildHeaders: false` on their immediate parent.

### Mixin Order

`CompoundColumns` contains special considerations for interoperating with a few
other dgrid mixins and extensions.  As such, the following should be kept in
mind when using these components together:

* `CompoundColumns` should be mixed in **before** `ColumnSet`,
  because it needs to normalize its column structure before `ColumnSet` performs its logic.
* `CompoundColumns` should be mixed in **after** `ColumnResizer` and `ColumnHider`,
  because it extends methods defined by these other extensions,
  to add considerations specific to its own behavior.