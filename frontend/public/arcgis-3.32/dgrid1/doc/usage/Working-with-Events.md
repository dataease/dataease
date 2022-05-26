# Working with Events

## Listening for events with dojo/on and grid.on

The [`dojo/on`](http://dojotoolkit.org/reference-guide/dojo/on.html) module provides a
concise yet powerful API for registering listeners, especially for DOM events.
Listening for events of interest on a dgrid component is very straightforward using `dojo/on`;
furthermore, dgrid components possess an `on` method, which is equivalent to calling `dojo/on`
passing the component's top-level DOM node as the target.

Using the event delegation features of `dojo/on`, it is possible to listen for
all manner of events.  For example, to listen for right-clicks on rows in the
grid's body:

```js
grid.on('.dgrid-content .dgrid-row:contextmenu', function (event) {
    /* ... */
});
```

Or, to listen to clicks on individual header cells:

```js
grid.on('.dgrid-header .dgrid-cell:click', function (event) {
    /* ... */
});
```

In summary, pretty much any combination desired can be achieved by using
event delegation with selectors based on the `dgrid-header`, `dgrid-content`,
`dgrid-row`, and `dgrid-cell` CSS classes as appropriate.

## Obtaining information about events

Commonly, the most important information about an event will be the row or cell
in which it was triggered.  With dgrid components, this information is
retrievable by passing the event object itself to the instance's `row` or
`cell` method.  These methods are particularly powerful, in that they are
capable of returning information about a row or cell, simply given a row/cell
element or any child thereof, or an event which originated from such an element.
These methods are also capable of looking up via a store item or ID; in the case
of `cell`, a second argument indicating column ID would also be passed.

Expanding upon the examples above...

```js
grid.on('.dgrid-content .dgrid-row:contextmenu', function (event) {
    var row = grid.row(evt);
    // row.element == the element with the dgrid-row class
    // row.id == the identity of the item represented by the row
    // row.data == the item represented by the row
});

grid.on('.dgrid-header .dgrid-cell:click', function (event) {
    var cell = grid.cell(evt);
    // cell.element == the element with the dgrid-cell class
    // cell.column == the column definition object for the column the cell is within
    // cell.row == the same object obtained from grid.row(evt)
});
```

## Custom events emitted by dgrid

Event name|Emitted by
-----|-----
`dgrid-refresh-complete`|[`OnDemand*`](../components/core-components/OnDemandList-and-OnDemandGrid.md#events) and [`Pagination`](../components/extensions/Pagination.md#events) modules after grid has rendered (and after calling `grid.refresh`)
`dgrid-error`|[`OnDemand*`](../components/core-components/OnDemandList-and-OnDemandGrid.md#events) and [`Pagination`](../components/extensions/Pagination.md#events) modules when an error occurs
`dgrid-cellfocusin`<br>`dgrid-cellfocusout`|[`Keyboard`](../components/mixins/Keyboard.md#events) module when keyboard navigation occurs
`dgrid-sort`|[`Grid`](../components/core-components/Grid.md#events) module when a column is sorted
`dgrid-columnresize`|[`ColumnResizer`](../components/extensions/ColumnResizer.md#events) module
`dgrid-columnreorder`|[`ColumnReorder`](../components/extensions/ColumnReorder.md#events) module
`dgrid-columnstatechange`|[`ColumnHider`](../components/extensions/ColumnHider.md#events) module
`dgrid-datachange`|[`Editor`](../components/mixins/Editor.md#events) module when an editor field loses focus after being changed
`dgrid-editor-show`|[`Editor`](../components/mixins/Editor.md#events) module before placing a shared editor
`dgrid-editor-hide`|[`Editor`](../components/mixins/Editor.md#events) module before removing an `editOn` editor
`dgrid-select`|[`Selection`](../components/mixins/Selection.md#events) module when one or more rows are selected
`dgrid-deselect`|[`Selection`](../components/mixins/Selection.md#events) module when one or more rows are deselected