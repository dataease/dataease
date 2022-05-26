# Editor

The Editor mixin provides the ability to render editor controls within cells
for specific columns. When used in conjunction with a store-backed grid such as an
[OnDemandGrid](../core-components/OnDemandList-and-OnDemandGrid.md#ondemandgrid),
edited fields are directly correlated with the dirty state of the grid; changes
can then be saved back to the store based on edits performed in the grid.

```js
require([
    'dojo/_base/declare',
    'dgrid/OnDemandGrid',
    'dgrid/Keyboard',
    'dgrid/Selection',
    'dgrid/Editor'
], function (declare, OnDemandGrid, Keyboard, Selection, Editor) {
    var editGrid = new (declare([ OnDemandGrid, Keyboard, Selection, Editor ]))({
        collection: myStore,
        columns: [
            {
                label: 'Name',
                field: 'name',
                editor: 'text',
                editOn: 'dblclick'
            },
            // ...
        ]
    }, 'editGrid');
});
```

For more examples of Editor in use, see the various `Editor` test pages, as well
as the `GridFromHtml_Editors` test page for declarative examples.

## Method Summary

Method | Description
------ | -----------
`edit(cell)` | Activates/focuses the specified cell (which can be a cell object from `grid.cell()`, or a DOM node or event resolvable to one).

## Additional Column Definition Properties

Property | Description
-------- | -----------
`editor` | The type of component to use for editors in this column; either a string specifying a type of standard HTML input to create, or a Dijit widget constructor to instantiate.  If unspecified, the column will not be editable.
`editOn` | A string containing the event (or multiple events, comma-separated) upon which the editor should activate.  If unspecified, editors are always displayed in this column's cells.
`editorArgs` | An object containing input attributes or widget arguments.  For HTML inputs, the object will have its key/value pairs applied as attributes/properties via `dojo/dom-construct.create`; for widgets, the object will be passed to the widget constructor.
`canEdit(object, value)` | A function returning a boolean value indicating whether or not the cell for this column should be editable in a particular row.  Receives the item for the current row, and the value to be rendered (i.e. the return from the column's `get` function if any, or the value of the `field` specified in the column).
`autoSave` | If `true`, the grid's `save` method will be called as soon as a change is detected in an editor in this column.  Defaults to `false`. **Note:** if an error is encountered as a result of a store operation triggered by `autoSave`, a `dgrid-error` event will be emitted.
`autoSelect` | If `true` and `editor` is `'text'` or a `TextBox`-based widget, the contents of the field will be selected when the editor is activated/focused.
`dismissOnEnter` | By default, pressing enter will store the current value in the grid's dirty data hash.  This can be undesirable particularly for textarea editors; setting this property to `false` will disable the behavior.

## Events

The Editor mixin emits a `dgrid-datachange` event to reflect changes made to the editor input/widget.
When this fires varies depending on whether a native input or Dijit widget is used:

* It fires on the `change` event of native inputs, which at minimum should correspond to the input losing focus,
  but may correspond to more direct interactions for some types of inputs
* It fires on the `blur` event of Dijit widgets, because Dijit's `change` events often fire on a delay which
  would involve additional complexity to handle

The `dgrid-datachange` event includes the following properties:

* `grid`: The Grid instance in which the edit occurred
* `cell`: The `cell` object to which the edit applied, as reported by the
  `grid.cell` method
* `oldValue`: The value before the edit occurred
* `value`: The value after the edit occurred

This event bubbles and is cancelable; if the event is canceled, the modification
will be reverted.

Editors with `editOn` set also emit `dgrid-editor-show` and `dgrid-editor-hide`
events when the editor is shown and hidden, respectively. These events also
include `grid` and `cell` properties and bubble; however, they are not
cancelable. If an editor should not be shown under specific circumstances,
include a `canEdit` function in the column definition.

## Recommendations for the editOn property

If attempting to trigger an editor on focus (to accommodate keyboard and mouse),
it is highly recommended to use dgrid's custom event, `dgrid-cellfocusin`
instead of `focus`, to avoid confusion of events. Note that this requires also
mixing the Keyboard module into the Grid constructor.

If touch input is a concern for activating editors, the easiest solution is to
use the `click` event, which browsers on touch devices normalize to fire on
taps as well. If a different event is desired for desktop browsers, it is
possible to do something like the following:

```js
require([
    'dgrid/OnDemandGrid',
    'dgrid/Editor',
    'dojo/has'
    // ...
], function (OnDemandGrid, Editor, has /*, ... */) {
        var columns = [
            {
                name: 'name',
                label: 'Editable Name',
                editor: 'text',
                editOn: has('touch') ? 'click' : 'dblclick'
            },
            // ...
        ];
    }
);
```

There are also a couple of useful simple gesture implementations available in
the [`util/touch` module](../utilities/touch.md), namely `tap` and `dbltap`.
