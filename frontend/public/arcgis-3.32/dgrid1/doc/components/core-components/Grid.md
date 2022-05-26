# Grid

Grid extends List to provide tabular display of data items, with different
fields arranged into columns.

```js
require([ 'dgrid/Grid' ], function (Grid) {
    var columns = {
        first: {
            label: "First Name"
        },
        last: {
            label: "Last Name"
        }
    };
    var grid = new Grid({ columns: columns }, 'grid'); // attach to a DOM id
    grid.renderArray(arrayOfData); // render some data
});
```

**Note:** `dgrid/Grid` does not directly support `dstore` stores. `dstore` stores are supported by the following:

* [`dgrid/OnDemandList` and `dgrid/OnDemandGrid`](OnDemandList-and-OnDemandGrid.md)
* [`dgrid/extensions/Pagination`](../extensions/Pagination.md)
* Anything else that extends `dgrid/_StoreMixin`

## APIs

In addition to the methods and properties inherited from [List](List.md), the Grid
component also exposes the following properties and methods.

### Property Summary

Property | Description
-------- | -----------
`formatterScope` | Optional object; if specified, column formatters may be specified as strings instead of functions, in which case they will be searched for as properties within the given `formatterScope` object, and executed in the context of that object.
`hasNeutralSort` | Boolean; controls behavior when clicking the same column heading repeatedly.  `false` (the default) alternates between sorting ascending and descending.  `true` alternates between ascending, descending, and no sort.

### Method Summary

Method | Description
------ | -------------
`cell(target[, columnId])` | Analogous to the `row` method, but at the `cell` level instead.  The `cell` method can look up based on an event or DOM element, or alternatively, a data item (or ID thereof) and the ID of a column. Returns an object containing the following properties: `row` - a Row object (as would be obtained from the `row` method) for the row the cell is within, `column` - the column definition object for the column the cell is within, `element`- the cell's DOM element.
`column(target)` | Returns the column definition object for the given column ID; typically analogous to `cell(...).column`.
`left(cell[, steps])` | Given a cell object (or something that resolves to one via the `cell` method), returns a cell object representing the cell located `steps` cells to the left (where `steps` defaults to `1`), wrapping to previous rows if necessary.
`right(cell[, steps])` | Same as `left()`, but operating towards the right, wrapping to subsequent rows if necessary.
`styleColumn(columnId, css)` | Programmatically adds styles to a column, given its column id, by injecting a rule string into a stylesheet in the document.  Returns a handle with a `remove` function, which can be called to later remove the added style rule.  Styles added via this method will be removed when the instance is destroyed if `cleanAddedRules` is set to `true`.
`updateSortArrow(sort, updateSort)` | Updates the placement of the sort arrow indicator in the appropriate header cell.  Normally called automatically, but can be called manually in the case of custom sort logic where the `dgrid-sort` event is canceled.  `sort` is the new sort order to be reflected by the UI update; `updateSort` is an optional boolean (defaulting to `false`) which, if `true`, will update the internal `_sort` variable to keep it in sync.

By default, the Grid renders a header, containing cells which display the label
of each column. This can be disabled by setting `showHeader: false` in the
arguments object to the Grid; it can also be changed later using
`set("showHeader", ...)`.

## Events

The Grid component emits one custom event, `dgrid-sort`, when a header cell is
clicked to initiate a sort.  This event includes the following properties:

* `grid`: The Grid instance which fired the event
* `parentType`: The original type of event responsible for firing this one
  (`click` or `keydown` within a header cell)
* `sort`: An array of objects with `property` and optionally `descending`
  properties, representing the new sort order to be put into effect

The `dgrid-sort` event bubbles and is cancelable; if canceled, the sort
order will not be set.  This can be useful for instituting custom sort logic
where setting the actual sort on an array or store is undesirable; in this
case, `updateSortArrow` should be called manually if the header is to be
updated.

## Specifying Grid Columns

In the simplest cases, the columns of the grid are defined via the `columns`
property. This property can be a hash (object) or array, containing column
definition objects. When `columns` is an object, each property's key represents
the `id` and `field` of the column, and each value is the column definition object.
When `columns` is an array, the numeric indices become the column IDs; `field`
must be specified within each definition.

Generally, using object notation is slightly more concise and convenient.
However, it's worth noting that doing so relies on the order of enumeration
employed by the JavaScript runtime. Typically this isn't a problem, as it
matches the order in which properties are specified, but one common exception is
in the case of keys coercible to numbers.

### Columns using an object

This is an example of a `Grid` using object column definitions:

```js
require([ 'dgrid/Grid' ], function (Grid) {
    var columns = {
        first: {
            label: 'First Name'
        },
        last: {
            label: 'Last Name'
        },
        age: {
            label: 'Age',
            get: function(object){
                return (new Date() - object.birthDate) / 31536000000;
            }
        }
    };
    var grid = new Grid({ columns: columns }, 'grid'); // attach to a DOM id
    grid.renderArray(arrayOfData); // render some data
    // ...
});
```

### Columns using an array

Alternatively, the same columns as above could be defined in an array, as
follows:

```js
var columns = [
    {
        label: 'First Name',
        field: 'first'
    },
    {
        label: 'Last Name',
        field: 'last'
    },
    {
        label: 'Age',
        field: 'age',
        get: function(object){
            return (new Date() - object.birthDate) / 31536000000;
        }
    }
];
```

### Column shorthand

A column definition may also be specified simply as a string, in which case the
value of the string is interpreted as the label of the column. Thus, the
simplest column structures can be more succinctly written:

```js
var columns = {
    first: 'First Name',
    last: 'Last Name',
    // ...
};
```

### Sub-rows

The Grid component also supports structures with multiple "sub-rows"; that is, it
supports the idea of rendering multiple rows for each data item. Specification
of multiple sub-rows is very much like specifying columns, except that one uses
the `subRows` property instead of `columns`, and it receives an array of columns
objects/arrays. Both the `columns` and `subRows` properties can be later reset
by using the central `set` method.

```js
require([
    'dgrid/Grid'
],function (Grid) {
    var grid = new Grid({
        subRows: [
            [
                { field: 'id', label: 'ID' },
                { field: 'name', label: 'Name' }
            ],
            [
                { field: 'description', label: 'Description', colSpan: 2 }
            ]
        ],
        // ...
    }, 'grid');
});
```

#### Column and row spanning with sub-rows

When defining column structures with multiple sub-rows (via `subRows` or the
[`ColumnSet`](../mixins/ColumnSet.md) mixin), the `colSpan` and `rowSpan` properties (documented below)
can be specified in column definitions.  See the
[`complex_column.html`](../../../test/complex_column.html) test page for examples of these properties in action.

## Column Definition Properties

In any of the above formats, each individual column definition object may have
the following properties (all are optional):

Property | Description
-------- | -------------
`field` | The property from the object in the list to display in the body of the grid (unless otherwise overridden via the `get` function, explained below). In cases where `columns` is passed an object, the key of each property represents the field name, and thus this property is normally omitted.
`id` | The id of the column; normally this is determined automatically from the keys or indices in the `columns` object or array.
`label` | The label to show in the header of the grid. Defaults to the value of `field`.
`className` | CSS class(es) to assign to the cells in the column.  The value of this property may be a string to apply equally to all cells in the column, or a function which is passed the item for each row (or `undefined` for the header row) and should return a string.  In either case, multiple classes may be specified space-delimited.  In addition, a class in the format `field-<field>` is added if the column has a `field` defined.
`colSpan` | A number specifying how many columns the cell should span, when multiple sub-rows are defined.
`rowSpan` | A number specifying how many rows the cell should span, when multiple sub-rows are defined.
`sortable` | Indicates whether or not the grid should allow sorting by values in this field, by clicking on the column's header cell. Defaults to `true`. Note that it is always possible to programmatically sort a Grid by a given field by calling `set("sort", property, descending)` regardless of`sortable` status or even visible presence in the Grid altogether.
`get(item)` | An optional function that, given a data item, will return the value to render in the cell.
`set(item)` | An optional function that, given a modified data item, will return the value to set for the respective field on that item upon a call to `save()`. If no value is returned, the value as set in the passed item will be used.  (Modifying the passed item directly is thus also an option.)
`formatter(value, object)` | An optional function that will return a string of HTML for rendering.  The function is passed the value that would normally be rendered, and the object from the collection.  If `formatterScope` is used, this can be a string instead of a function, in which case a function will be looked up on the `formatterScope` object using the given string. (Note: if a custom `renderCell` is specified, `formatter` will be ignored unless the custom `renderCell` accounts for it.)
`renderCell(object, value, node)` | An optional function that will be called to render the value into the target cell. `object` refers to the record from the grid's collection for the row, and `value` refers to the specific value for the current cell (which may have been modified by the column definition's `get` function). `node` refers to the table cell that will be placed in the grid; if `renderCell` returns a node, that returned node will be placed inside the table cell. (Note: if a custom `renderCell` is specified, `formatter` will be ignored unless the custom `renderCell` accounts for it.)
`renderHeaderCell(node)` | An optional function that will be called to render the column's header cell. Like `renderCell`, this may either operate on the node directly, or return a new node to be placed within it.
