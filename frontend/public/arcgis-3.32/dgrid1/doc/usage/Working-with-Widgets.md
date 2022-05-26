# Working with Widgets

## Layout widgets and the DijitRegistry extension

When creating dgrid instances within Dijit layout widgets, it is recommended to
construct these instances with the DijitRegistry extension mixed in.
This enables functions like `registry.findWidgets` (also used by
`_WidgetBase.getChildren`) to report dgrid instances as well.

## Destroying rendered widgets

When creating custom `renderRow` or `renderCell` functions which populate rows
or cells with new Dijit widget instances, it is important to keep in mind that
these widgets should ideally be maintained and destroyed when appropriate.

While the first inclination might be to simply handle this in the list or grid
component's `destroy` method, it's important to realize that this will not cover
cases where rows are destroyed when scrolled out of view (for components like
OnDemandList and OnDemandGrid), resulting in leaked widget references. A more
appropriate time to destroy rendered widgets would be in `removeRow`, which is
called whenever a row is undrawn, including scroll operations in on-demand
components, as well as when `destroy` is called.

For example, the [Editor](../components/mixins/Editor.md) mixin performs cleanup in this fashion.
During initial rendering of an editor widget, a reference to the created widget
is stored, which is then accessed and destroyed when `removeRow` is called:

```js
removeRow: function (rowElement) {
    // destroy our widget during the row removal operation
    var cellElement = grid.cell(rowElement, column.id).element,
        widget = cellElement.widget;
    if (widget) {
        widget.destroyRecursive();
    }

    this.inherited(arguments);
}
```