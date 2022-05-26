# Limitations

## Use with the Legacy Loader API

The dgrid package was designed primarily with AMD in mind; as such, it has been
tested primarily using the `require` and `define` APIs available in Dojo 1.7.

When using the legacy `dojo.require` method instead, loading `dgrid/List` will
not work without first loading `dgrid.css`.  This is because otherwise the
`xstyle/css` plugin will resolve asynchronously, which is not suported by the
legacy loader.  To use `dgrid/List` with `dojo.require`, make sure you have
`<link rel="stylesheet" href="path/to/dgrid.css">` in your `<head>` before
loading `dgrid/List`.

This also applies for stylesheets loaded by specific mixins (such as `dgrid/ColumnSet`)
or extensions (such as `dgrid/extensions/ColumnResizer`).

## Reuse of Column Definitions

Reusing a single column definition object between multiple grids (e.g.
`var cols = {...}, gridA = new Grid({ columns: cols }), gridB = new Grid({ columns: cols })`)
is *not* supported, and will not function properly. Always create a fresh `columns`
object for every grid you instantiate.  This includes performing unique invocations
of column plugin functions for each grid.

If multiple grids in a single page are likely to use the same column structure,
in order to avoid code repetition, you can create a function which returns
the structure, but creates it every time it is called.  For example:

```js
function getColumns(){
    return {
        col1: editor({ label: "Column 1" }, "text", "dblclick"),
        col2: { label: "Column 2", sortable: false },
        // ...
    };
}

var grid = new Grid({
    columns: getColumns(),
    // ...
}, "grid");
var secondGrid = new Grid({
    columns: getColumns(),
    // ...
}, "secondGrid");
```

## Column Definitions: Coexistence of formatter and renderCell

The default `renderCell` logic, as well as the logic within the `editor` and
`tree` column plugins, is specifically written in such a way as to honor any
`formatter` that is defined on the column definition.

If a custom `renderCell` function is specified, however, it will override the
default logic which is responsible for honoring `formatter`, and thus the custom
`renderCell` logic will take precedence.

## Note on Observable Stores

dgrid 0.3.13 contains updates to remediate issues with `dojo/store/Observable`
dropping items at the boundaries of paged result sets when they are modified,
and ignoring items added at the end of the grid.

After those fixes, however, you may encounter another quirk where items are
always moved to the end of the grid when they are updated.  This issue happens
specifically if you have not specified a `sort` order, and can be avoided by
specifying one.