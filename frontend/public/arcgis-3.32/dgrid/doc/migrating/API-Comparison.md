# API Comparison Between dojox/grid and dgrid

## dojox/grid properties

### sortInfo

The way in which dgrid represents current sort order is significantly different
than `dojox/grid`.  dgrid stores the current sort options, as they would be passed
via a store's `queryOptions`; these options are retrievable via the sort getter
(e.g. `grid.get("sort")`).

Additionally, store-backed components will reflect any currently-applied sort
information in the object returned by `get("queryOptions")`, since `sort`
becomes part of these options when queries are issued.

### rowSelector and indirect selection

Indirect selection is available in dgrid via the selector column plugin.  This
achieves similar effects to the DataGrid's `_CheckBoxSelector` and
`_RadioButtonSelector` view types, and EnhancedGrid's IndirectSelection plugin.

dgrid does not feature a direct analog to the `rowSelector` property.

### selectionMode

dgrid supports this property via the Selection and CellSelection mixins.
It recognizes the same values supported by `dojox/grid` components
(`none`, `single`, `multiple`, and `extended`, the latter being the default).

### keepSelection

This is roughly the inverse equivalent to the `deselectOnRefresh` property
supported by dgrid's Selection (and CellSelection) mixin.  Both `dojox/grid`
and dgrid default to *not* maintaining selection between refreshes, sorts, etc.

### columnReordering

Setting this `dojox/grid` property to `true` allows reordering of columns
in grids with basic structures via drag'n'drop operations on column header cells.

This feature is available in dgrid via the ColumnReorder extension.

### headerMenu and other context menu scenarios

dgrid does not directly offer context menu functionality via an extension, but
it is easily possible to delegate to the `contextmenu` event of
cells or rows in the grid's body or header, to perform custom logic.  Here are
the basic steps one would need to follow:

* An event handler listening for `contextmenu` events against a particular selector;
  for example:
    * `.dgrid-header:contextmenu` for a general header context menu
    * `.dgrid-row:contextmenu` for a general body context menu
    * `.dgrid-header .field-foo:contextmenu` for a context menu for a specific header cell
    * `.dgrid-content .field-foo:contextmenu` for a context menu for body cells in
      a particular column
* within the event handler:
    * A call to `preventDefault` on the event object, to stop the default
      browser context menu from displaying.
    * A call to `grid.row()` or `grid.cell()` to retrieve information on the
      pertinent row or cell.  If the menu is intended to apply to selected items,
      `grid.selection` can be checked for entries, and then `grid.row()` can be
      called with the IDs found.
    * If `dijit/Menu` is being used, it unfortunately does not provide any
      directly-accessible public API for simply opening the menu around the mouse,
      as it is normally expected to be pre-attached to nodes; however, depending
      on use-case, attaching it to an entire section of the grid may be inappropriate.
      In such a case, the menu can be directly opened within a `contextmenu`
      event handler, by calling `_scheduleOpen(this, null, { x: evt.pageX, y: evt.pageY }`.

### autoHeight

Automatic height can be achieved by adding the `dgrid-autoheight` class to a List
or Grid's `domNode`.  There is no direct programmatic support for this
(i.e. there is no built-in support for automatically sizing to a certain number of rows).

Examples of an auto-height grid can be found in the `autoheight.html` and
`extensions/Pagination.html` test pages.

### autoWidth

Not supported.

### initialWidth

Not supported.  Width (and height) should be dictated via CSS.

### singleClickEdit

The effect of the `singleClickEdit` property can be achieved by specifying
`editOn: "click"` in a column definition passed to the editor column plugin
function.  (Alternatively, `dojox/grid`'s default double-click behavior can be
achieved by specifying `editOn: "dblclick"` instead.)

### loadingMessage, noDataMessage, errorMessage

Store-backed grid instances support `loadingMessage` and `noDataMessage`.
There is currently no direct support for an error message, but when a
store-related error occurs within dgrid's own logic, it will emit a `dgrid-error`
event.  When `grid.save()` is called directly, it will return a promise which
will reject if an error occurs.

### selectable

This is not exposed as a distinct option in dgrid, but is automatically managed
by the `Selection` mixin.  Standard browser selection is disabled when a
`selectionMode` other than `none` is in use.
Otherwise, text selection operates as normal.

### formatterScope

dgrid supports this option as of version 0.3.7.

### updateDelay

dgrid does not support this, as it is generally not applicable, due to the
difference in how dgrid components update from observed store changes.

### escapeHTMLInData

Not supported.  By default, dgrid components will escape HTML in data, as it
should generally be devoid of HTML in most cases, and presence of HTML in data
might suggest a cross-site scripting attempt.

The `formatter` or `renderCell` functions in the column definition may be
overridden to explicitly render data as received, in cases where that is truly
desired.

## Column Definitions

Whereas `dojox/grid` always expects cell definitions to be specified via a
`structure` property, dgrid expects one of the following properties to be specified:

* `columns`: an array or object hash, for simple single-row grid configurations
* `subRows`: an array of arrays, for grid configurations with multiple sub-rows per item
* `columnSets` (only when the ColumnSet mixin is in use): a nested array for
  grid configurations containing distinct horizontal regions of one or more rows
  (analogous to multiple views in a `dojox/grid` instance)

The following subsections outline how features of `dojox/grid` cell definitions
are available in dgrid column definitions.

### field

Supported by dgrid, including the special `"_item"` value supported by
`dojox/grid` in Dojo >= 1.4.

Also note that dgrid also supports specifying `columns` as an object hash instead
of an array, in which case the key of each property is interpreted as the `field`.

### fields

Not supported by dgrid.  If a compound value is desired, define a custom `get`
function in a column definition.

### width

Use CSS with `.field-<fieldname>` selectors.  (Note that if any value is
specified via the `className` property of the column definition object, it
takes the place of `.field-<fieldname>`.)

### cellType, widgetClass

The editor column plugin provides capabilities equivalent to these properties.
It accepts an `editor` property, which can be either a widget constructor or a
string indicating a native HTML input type.

### options

Not directly applicable; in `dojox/grid` this applies only to cell definitions
where `cellType` is set to `dojox.grid.cells.Select`.

The editor column plugin does not currently offer support for standard HTML
select components; however, similar behavior can be achieved using the
`dijit/form/Select` widget as the `editor`, and specifying `options` for the
widget within the `editorArgs` property of the column definition object.

### editable

In dgrid, cells are uneditable by default, and are made editable by invoking the
editor column plugin.

### draggable

Only applicable to `dojox/grid` instances with `columnReordering` set to `true`,
the `draggable` property determines whether a particular column can be
reordered via drag'n'drop.

The ColumnReorder dgrid extension provides an equivalent via the `reorderable`
column definition property.  It defaults to `true`, but if set explicitly to
`false`, the given column's header node will not be registered as a DnD item.

### formatter

dgrid supports formatter functions, but doesn't support returning a widget from
them.

dgrid also has `renderCell`, which is expected to return a DOM node.  This could
ostensibly be used for displaying widgets (and the editor column plugin does
exactly this).

Note that for cell editing purposes, use of the editor column plugin is highly
encouraged.

### get

dgrid supports the `get` function on column definitions; however, note that it
only receives one parameter: the object for the item represented by the current
row being rendered.  (dgrid generally has no concept of row index, since
row identities are generally far more meaningful.)

### hidden

The `hidden` property on column definitions is only supported by the
ColumnHider extension.  Otherwise, columns would ordinarily be suppressed simply
by excluding them from the `columns`, `subRows` or `columnSets` property outright.

## DataGrid methods

### getItem(rowIndex), getItemIndex(item)

These are somewhat inapplicable, since again, dgrid components do not put any
emphasis on index in terms of order of appearance in the component.

On the other hand, when dealing with events on nodes in a list or grid, it is
possible to retrieve the associated item via the `data` property of the object
returned by the `row` or `cell` functions.  These functions can look up based on
a variety of argument types, including a child node of the target row/cell, or
an event object which fired on such a node.

### setStore

Store-backed dgrid components support this via
`set("store", store[, query[, queryOptions]])`.

### setQuery

Store-backed dgrid components support this via
`set("query", query[, queryOptions])`.

### setItems

While it is unclear what exact purpose this serves in `dojox/grid/DataGrid`, and
whether or not it is truly intended to be public, it is probably analogous to
calling `renderArray` directly on a dgrid component.  Note, however, that
generally `renderArray` is not expected to be called directly on store-backed
instances.

### filter

This is likely closest in behavior to the `refresh` method on dgrid components.

### sort

dgrid components have a sort setter (e.g. `grid.set("sort", ...)` which takes
two arguments:

* the name of the field to sort by
* optionally, a flag as to whether to sort descending (defaults to `false`)

Like the `sort` method in `dojox/grid` components, this can even be called to
sort columns that ordinarily wouldn't be sortable via the UI.

### canSort

`dojox/grid` components refer to this method when a sort request is initiated
via the UI.  dgrid does not have such a method; instead, it relies on the
`sortable` property of each column definition (which defaults to `true`).

### removeSelectedRows

dgrid has no direct analog to this method, but the same effect can be achieved
on a store-backed, Selection-enabled list or grid instance as follows:

```js
for(var id in grid.selection){
    grid.store.remove(id);
}
```