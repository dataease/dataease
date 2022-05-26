# Pagination

In contrast to the [OnDemandList and OnDemandGrid](../core-components/OnDemandList-and-OnDemandGrid.md) modules,
the Pagination extension implements classic discrete paging controls. It displays a certain
number of results at a given time, and provides a footer area with controls to
switch between pages.

```js
require([
    'dojo/_base/declare',
    'dgrid/Grid',
    'dgrid/extensions/Pagination'
], function (declare, Grid, Pagination) {
    var grid = new (declare([ Grid, Pagination ]))({
        collection: myStore,
        columns: myColumns,
        pagingLinks: 1,
        pagingTextBox: true,
        firstLastArrows: true,
        pageSizeOptions: [10, 15, 25]
    }, 'grid');
    // ...
});
```

**Note:** the Pagination extension should be mixed into List or Grid, **not**
one of the OnDemand constructors, since those contain their own virtual
scrolling logic. Internally, Pagination inherits from the same \_StoreMixin
module inherited by the OnDemand prototypes for common integration with
dstore.

## API

The Pagination extension supports the following additional instance properties and methods.

In addition, the Pagination extension also inherits properties and methods
supported by `dgrid/_StoreMixin`; for details, see the tables for properties and
methods inherited from \_StoreMixin in the documentation for
[OnDemandList and OnDemandGrid](../core-components/OnDemandList-and-OnDemandGrid.md).

### Property Summary

Property | Description
-------- | -----------
`rowsPerPage` | Number of items to show on a given page. Default: `10`
`previousNextArrows` | Whether to show arrows which go to the previous/next pages when clicked. Default: `true`
`firstLastArrows` | Whether to show arrows which jump to the first/last pages when clicked. Default: `false`
`pagingLinks` | If a positive number is specified, renders a sequence of page numbers around the current page, and for the first and last pages.  The number specified indicates how many "neighbors" of the current page are rendered in *each* direction.  If `0` is specified, no page number sequence is rendered. Default: `2`
`pagingTextBox` | Whether to show a textbox in place of the current page indicator, to allow immediately jumping to a specific page. Default: `false`
`pageSizeOptions` | An optional array specifying choices to present for the `rowsPerPage` property in a drop-down. If unspecified or empty, no drop-down is displayed (the default behavior).
`showLoadingMessage` | Boolean specifying whether to blank the content area and show the loading message while loading a new page; if `false`, the previous content will remain present until new content finishes loading.  Default is `true`.

### Method Summary

Method | Description
------ | -----------
`gotoPage(page)` | Loads the indicated page; returns a promise yielding an object containing the `rows` rendered as well as the `results` they represent.  **Note:** Page numbers start at 1.
`refresh(options)` | Clears the grid and re-queries the store for the first page of data.  The Pagination extension returns a promise from `refresh`, which resolves when items in the first page finish rendering.  The promise resolves with the QueryResults that were rendered.  `options` is an optional object which may include a `keepCurrentPage` option; if true, the current page will be reloaded, rather than the first page.  (If the current page no longer exists due to item removal, the last page will be loaded.)

## Events

Like [OnDemandList and OnDemandGrid](../core-components/OnDemandList-and-OnDemandGrid.md), the Pagination extension will emit the following events:

* `dgrid-refresh-complete` when results finish rendering as the result of a
  `refresh` call (also including the initial render)
* `dgrid-error` when an error occurs while attempting to query the store in
  response to user interaction

## Internationalization

The Pagination extension retrieves the strings for various parts of its UI from
resources in `dgrid/extensions/nls`, via the `dojo/i18n!` plugin. These strings
are stored in an `i18nPagination` property on the grid instance, and can be
overridden if desired. The optimal point in the lifecycle to override any i18n
strings would be in `postMixInProperties`, or in `buildRendering` *before*
calling `this.inherited(arguments)`.