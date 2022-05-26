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
        "dgrid/GridFromHtml"
        /* ... */
    ], function(GridFromHtml /* , ... */){
        var grid = new GridFromHtml({}, "htmlgrid");
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
include the surrounding curly braces around the object - this allows
alternatively specifying a column plugin instead of just a straight-up object.
(Note, however, that referencing column plugins requires that they be exposed in
the global scope, perhaps under a namespace.) Examples of creating grids from
HTML can be found in the `GridFromHtml.html` and `complex_columns.html` test
pages.

It is also possible to specify columnsets (for the `ColumnSet` mixin) via
HTML tables by using the `GridWithColumnSetsFromHtml` module. ColumnSets are
expressed in HTML via `colgroup` tags. See the `complex_columns.html` test page
for an example of this as well.

## Dojo 1.7 Note: Using GridFromHtml with the Dojo Parser

Using the parser in Dojo 1.7 with modules designed purely in the AMD format can
be a bit unwieldy, since the parser still expects `data-dojo-type` values to
reference variables accessible from the global context. While existing Dojo 1.x
components currently continue to expose globals, dgrid does not do so by
default, since it is written purely in AMD format. Thus, when intending to parse
over dgrid components, it is necessary to expose the components via a global
namespace first. For example:

```js
require(["dgrid/GridFromHtml", "dojo/parser", ..., "dojo/domReady!"],
function(GridFromHtml, parser, ...) {
    window.dgrid = { GridFromHtml: GridFromHtml };
    parser.parse();
});
```

This can be seen in practice in some of the test pages, such as
`GridFromHtml.html` and `dijit_layout.html`.

This applies to Dojo 1.7 only; in Dojo 1.8 this becomes easier thanks to
`data-dojo-type` supporting module IDs (see
[Dojo ticket #13778](http://bugs.dojotoolkit.org/ticket/13778)). Additionally,
`data-dojo-mixins` can be used to declaratively compose dgrid instances
including mixins.