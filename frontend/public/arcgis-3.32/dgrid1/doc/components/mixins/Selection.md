# Selection

The Selection module adds selection capability to a list or grid. The resulting
instance(s) will include a `selection` property representing the selected items.
This mixin will also fire batched `dgrid-select` and `dgrid-deselect` events,
which will possess a `rows` property containing an array of Row objects (with
`id`, `data`, and `element`). For example:

```js
require([
    'dojo/_base/declare',
    'dgrid/OnDemandGrid',
    'dgrid/Selection'
], function (declare, OnDemandGrid, Selection) {
    var grid = new (declare([ OnDemandGrid, Selection ]))({
        selectionMode: 'single',
        // ...
    }, 'grid');
    grid.on('dgrid-select', function (event) {
        // Get the rows that were just selected
        var rows = event.rows;
        // ...

        // Iterate through all currently-selected items
        for (var id in grid.selection) {
            if (grid.selection[id]) {
                // ...
            }
        }
    });
    grid.on('dgrid-deselect', function (event) {
        // Get the rows that were just deselected
        var rows = event.rows;
        // ...
    });
});
```

## Selection State Data

The grid keeps track of which rows are selected in the object stored in the `selection` property. The grid neither loads
from nor persists this information to any underlying data store.

## APIs

The Selection mixin supports the following additional instance properties and methods.

### Property Summary

Property | Description
-------- | -----------
`selection` | The object containing the IDs of the selected objects. The property names correspond to object IDs, and values are `true` or `false` depending on whether an item is selected.
`selectionMode` | String indicating how selection should behave in response to direct user interaction. See Selection Modes below for supported values.
`allowTextSelection` | Optional boolean indicating whether normal text selection within grid cells should be prevented.  By default, text selection is prevented unless `selectionMode` is set to `none`; setting this property to `true` or `false` will enable or disable text selection regardless of `selectionMode`.
`deselectOnRefresh`| Determines whether calls to `refresh` (including sorts) also clear the current selection; `true` by default.
`allowSelectAll`| Determines whether the "select-all" action should be permitted via a checkbox selector column or the Ctrl/Cmd+A keyboard shortcut; defaults to `false`.

### Method Summary

Method | Description
------ | -----------
`allowSelect(row)`| Returns a boolean indicating whether the given `row` should be selectable; designed to be overridden.
`select(row[, toRow])`| Programmatically selects a row or range of rows.
`deselect(row[, toRow])`| Programmatically deselects a row or range of rows.
`selectAll()`| Programmatically selects all rows in the component. Note that only rows that have actually been loaded will be represented in the `selection` object.
`clearSelection()`| Programmatically deselects all rows in the component.
`isSelected(row)`| Returns `true` if the given row is selected.

**Note:** The `select`, `deselect`, and `isSelected` methods can be passed any
type of argument acceptable to List's `row` method; see the [List](../core-components/List.md) APIs for
more information.


## Events

As indicated above, the Selection mixin will emit two custom events:

* `dgrid-select`: Emitted when one or more rows are selected
* `dgrid-deselect`: Emitted when one or more rows are deselected

Note that this means that when a user interaction results in the selection being
changed from one row to another, both events will be fired (with the
`dgrid-deselected` event firing first).

Both of these events expose the following properties (note that `rows` is
*always* an array, even if only one row was (de)selected.) :

* `grid`: The Grid (or List) instance in which the event occurred
* `rows`: Array containing any rows affected by the event
* `parentType`: If the event was triggered by user interaction, this property indicates what type of event originally triggered the event


## Selection Modes

The Selection mixin, as well as the [CellSelection](CellSelection.md) mixin which extends from
it, are capable of running in one of several different selection modes, as
specified by the `selectionMode` property:

Mode | Description
---- | -----------
`extended` | The default mode; allows multiple selection via keyboard modifiers (Shift to select ranges, Ctrl/Cmd to select/deselect multiple separate rows).  Clicks or keypresses without holding Shift or Ctrl/Cmd will select only the current target.
`multiple` | Like `extended`, except that clicks or keypresses without holding Shift or Ctrl/Cmd will add to the existing selection.
`single` | Allows only one row to be selected at a time.
`toggle` | Toggles the selected state of the row being acted upon; useful for touch input.
`none` | Does not allow direct selection of rows via keyboard or mouse events, but still allows selection via direct API calls, or via a [Selector](Selector.md) column.

### Implementing a Custom Selection Mode

Adding support for a custom selection mode is as simple as adding a single method.
For example, to add support for a custom mode named `mymode`, implement a method named `_mymodeSelectionHandler`.
This method receives 2 arguments: the event object, and the target that is being selected (a
dgrid row or cell element, depending on whether Selection or [CellSelection](CellSelection.md)
is being used).  The method should call `this.select` as appropriate based on
the details of the event.