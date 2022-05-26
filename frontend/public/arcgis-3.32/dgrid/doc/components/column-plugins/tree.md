# tree

The tree plugin enables expansion of rows to display children. 

```js
require([
    "dojo/_base/declare", "dgrid/OnDemandGrid", "dgrid/tree", "dgrid/Keyboard", "dgrid/Selection"
], function(declare, OnDemandGrid, tree, Keyboard, Selection){
    var treeGrid = new (declare([OnDemandGrid, Keyboard, Selection]))({
        store: myStore,
        columns: [
            tree({ label: "Name", field: "name" }),
            { label:"Type", field: "type", sortable: false},
            { label:"Population", field: "population" },
            { label:"Timezone", field: "timezone" }
        ]
    }, "treeGrid");
});
```

## Store Considerations

The tree plugin expects to operate on a store-backed grid, such as an
[OnDemandGrid](../core-components/OnDemandList-and-OnDemandGrid.md#ondemandgrid) or a grid with the [Pagination](../extensions/Pagination.md)
extension mixed in.

The store connected to the grid is expected to provide a `getChildren(object, options)`
method to return the children for a given item. Note that for best results,
`getChildren` should return results with an `observe` function as well
(like `query`), so that changes to children can also be reflected as they occur.

When calling `getChildren`, dgrid will include an `originalQuery` property in
the `options` argument, containing the value from the grid's `query` property.
This allows `getChildren` implementations to perform the same filtering on
child levels if desired.

The following is a simple example of what a `getChildren` implementation could
look like in an extension to `dojo/store/Memory`, where hierarchy is indicated
by a `parent` property on child items:

```js
getChildren: function(parent, options){
    // Support persisting the original query via options.originalQuery
    // so that child levels will filter the same way as the root level
    return this.query(
        lang.mixin({}, options && options.originalQuery || null,
            { parent: parent.id }),
        options);
}
```

The store may also (optionally) provide a `mayHaveChildren(object)` method which
returns a boolean indicating whether or not the row can be expanded. If this
is not provided, all items will be rendered with expand icons.

## Additional Column Definition Properties

The tree plugin supports the following additional column definition properties.

Property | Description
-------- | -----------
`shouldExpand(row, level, previouslyExpanded)` | Optional function which returns a boolean indicating whether the given row should be expanded when rendered.  The default implementation simply returns the value of `previouslyExpanded`, which denotes whether the row in question was previously expanded before being re-rendered.
`renderExpando()` | Optional function which can be overridden to customize the logic for rendering the expando icon beside each tree cell's content.
`collapseOnRefresh` | Boolean indicating whether to collapse all parents (essentially "forgetting" expanded state) whenever the grid is refreshed; the default is `false`.
`enableTransitions` | Boolean indicating whether to perform CSS transitions when expanding/collapsing; the default is `true`.  Note that this does not apply to browsers which do not support CSS transitions (e.g. IE < 10).
`indentWidth` | Number of pixels to indent each nested level of children; the default is `9`.
`allowDuplicates` | If this is set to `true`, a single object (and single id) can be used in multiple places in a tree structure. However, enabling this means that `grid.row(id)` will only return top-level objects (since it can't disambiguate other levels). The default is `false`.

## Additional Grid APIs

When the tree plugin is applied to a column in a grid, the grid is augmented with
the following method.

Method | Description
------ | -----------
`expand(row, expand)` | Expands or collapses the row indicated by the given Row object (from `grid.row(target)`) or a `dgrid-row` element. The optional `expand` argument specifies whether the row should be expanded (`true`) or collapsed (`false`); if unspecified, the method toggles the current expanded state of the row.  Returns a promise which resolves after data for the children has been retrieved.