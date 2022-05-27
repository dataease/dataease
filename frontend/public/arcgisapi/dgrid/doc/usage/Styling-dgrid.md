# Styling dgrid

dgrid components are designed to be highly CSS-driven for optimal performance and
organization, so visual styling should be controlled through CSS.

## Getting Started

The first thing to do when using dgrid components is to add `dgrid.css` to your page via a `link` tag or `@import`.

```html
<link rel="stylesheet" href="dgrid/css/dgrid.css">
```

You may also need to include `dojo/resources/dnd.css` (or equivalent custom styles)
if you are using the `ColumnReorder` or `DnD` extensions.

### Notes on Structural Styles

As of dgrid 1.0, dgrid no longer uses xstyle to automatically load structural CSS on a per-module basis.
`dgrid.css` now includes all structural styles, so that is the only stylesheet that needs to be referenced.

The structural styles are now organized using [Stylus](http://stylus-lang.com/), to keep each component's
structural styles separate while still generating a single CSS file.  If you are also using Stylus in your own
project, it is feasible to directly reference the `styl` files for only the specific dgrid components you need,
rather than all of the files referenced in `dgrid.styl`.  (You will always need `base.styl` at minimum.)

## Skins

dgrid includes a number of skins out of the box, in the `dgrid/css/skins` folder.  The `skin.html` test page under
`dgrid/test` provides a demonstration of all included skins.

There are two steps to using a dgrid skin:

1. Add its CSS via a `link` tag or `@import`
2. Include its namespace class at a level above your grids in the DOM (e.g. `<body class="claro">`)

For details on customizing skins, see [Customizing Skins](./Customizing-Skins.md).

## Styling Grid Columns

The Grid module creates classes based on the field names and column IDs of the columns
defined, following the conventions `field-<field-name>` and `dgrid-column-<column-id>`.
You can also explicitly define one or more classes via the `className` property,
in which case it is applied in addition to `field-<field-name>`.

For example, you could define a grid and style it like so:

```html
<style>
    .field-age {
        width: 80px;
    }
    .field-first {
        font-weight: bold;
    }
</style>
<script>
    require([ 'dgrid/Grid' ], function (Grid) {
        var grid = new Grid({
            columns: {
                age: 'Age',
                first: 'First Name',
                // ...
            }
        }, 'grid');
        grid.renderArray(someData);
    });
</script>
```

## Styling Grid Rows

While the column definition provides mechanisms for adding classes to individual
columns, there is no similar construct at the row level.  However, it is possible
to add classes to rows by aspecting `renderRow`.

However, you can achieve this using `dojo/aspect.after` - when its
`receiveArguments` argument is `false` (the default), it passes the original
arguments object as a second parameter to the advising function, allowing access
to the original object:

```js
aspect.after(grid, 'renderRow', function(row, args) {
  // Add classes to `row` based on `args[0]` here
  return row;
});
```

## The `dgrid-autoheight` class

There are various cases where it is desirable for a grid to adjust its height according to its contents.  dgrid
contains styles for this under the `.dgrid-autoheight` selector.  Making a grid's height size to fit is as simple
as adding the `dgrid-autoheight` class to the DOM node used to create the grid, or passing it via the `className`
property to the instance.

**Note:** Using the `dgrid-autoheight` class with `OnDemandList` is **not** recommended, as `OnDemandList` will
end up ultimately rendering all of the grid's rows immediately, page by page.  `dgrid-autoheight` works well with
`Pagination`.  If you are really interested in rendering all store data at once into an auto-height list or grid,
have a look at our [tutorial](http://dgrid.io/tutorials/1.0/single_query) on the subject.
