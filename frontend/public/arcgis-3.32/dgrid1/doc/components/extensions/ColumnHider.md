# ColumnHider

The ColumnHider extension adds the ability to dynamically hide or show columns
in a grid without the need to fully reset its layout. User interaction is
enabled via a menu accessible from the top right corner of the grid
(represented by a "+" mark); it will open on click, presenting checkboxes for
each column in the grid. These can be checked or unchecked to show or hide
individual columns, respectively.

```js
require([
    'dojo/_base/declare',
    'dgrid/OnDemandGrid',
    'dgrid/extensions/ColumnHider'
], function (declare, OnDemandGrid, ColumnHider) {
    var grid = new (declare([ OnDemandGrid, ColumnHider ]))({
        columns: {
            col1: {
                label: 'Column 1',
                hidden: true
            },
            col2: {
                label: 'Column 2',
                unhidable: true
            },
            col3: 'Column 3'
        }
    }, 'grid');
    // ...
});
```

**Note:** This extension is only fully supported for cases of simple, single-row
column layouts.  It can also be used with the [CompoundColumns](./CompoundColumns.md) extension.

## API

### Method Summary

Method | Description
------ | -----------
`toggleColumnHiddenState(id, hidden)` | Toggles or sets the hidden state of the column with the specified `id`.  Hides the column if `hidden` is `true`, shows it if `false`, and toggles the current state if unspecified.

## Additional Column Definition Properties

The ColumnHider extension supports the following additional column definition properties.

Property | Description
-------- | -----------
`hidden` | If `true`, the column in question will be initially hidden, but can be shown by opening the menu and checking its box.
`unhidable` | If `true`, the column in question will not be listed in the menu, denying access to toggle its appearance.  This can be particularly useful for a selector column which should always be shown, for example.

## Events

The ColumnHider extension will emit a`dgrid-columnstatechange` event when a
column is hidden or shown via the popup. It includes the following properties:

* `grid`: The Grid instance in which this event occurred
* `column`: The column definition object representing the column that was
  hidden or shown
* `hidden`: Boolean representing the new state of the column: `true` if
  hidden, `false` if shown
