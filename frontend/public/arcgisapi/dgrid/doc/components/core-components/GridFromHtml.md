# GridFromHtml

Some developers prefer specifying column layouts in an actual table structure
because it is more convenient or semantically clearer. dgrid supports this via
the GridFromHtml module.

```html
<table id="htmlgrid">
    <thead>
        <tr>
            <th data-dgrid-column="{ field: 'id' }">ID</th>
            <th data-dgrid-column="{ field: 'name', formatter: nameFormatter }">Name</th>
        </tr>
    </thead>
</table>

<script>
    require([
        'dgrid/GridFromHtml'
        /* ... */
    ], function (GridFromHtml /* , ... */) {
        var grid = new GridFromHtml({}, 'htmlgrid');
        grid.renderArray(someData);
    });
</script>
```

GridFromHtml can also be used for store-based grids by additionally
mixing in [OnDemandList](OnDemandList-and-OnDemandGrid.md) or [Pagination](../extensions/Pagination.md).

## Usage

When using this module, a `table` element should be specified as the source node
for the grid instance; it then scans for `th` nodes within rows (typically placed
inside the `thead`) to determine columns. Column properties are specified within
the `th`, primarily via the `data-dgrid-column` attribute, which should contain a
JavaScript object. Properties which coincide with standard HTML attributes can
also be specified as such, e.g. `class`, `rowspan`, and `colspan`. The innerHTML
of the `th` is interpreted as the column's `label` by default.

Note that *unlike* `data-dojo-props`, `data-dgrid-column` requires that you
include the surrounding curly braces around the object.
Examples of creating grids from
HTML can be found in the `GridFromHtml.html` and `complex_column.html` test
pages.

It is also possible to specify columnsets (for the `ColumnSet` mixin) via
HTML tables by using the `GridWithColumnSetsFromHtml` module. ColumnSets are
expressed in HTML via `colgroup` tags. See the `complex_columns.html` test page
for an example of this as well.

Note that when creating dgrid instances declaratively, you must always use module
ID notation with the parser, as dgrid does not expose a global namespace.
Additionally, `data-dojo-mixins` can be used to declaratively compose dgrid instances
including mixins/extensions.