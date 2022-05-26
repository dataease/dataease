# Usage Comparison Between dojox/grid and dgrid

## Simple programmatic usage

Given the following programmatic example using `dojox/grid`...

```js
require([
    'dojox/grid/DataGrid',
    'dojo/store/Memory',
    'dojo/data/ObjectStore',
    'dojo/domReady!'
], function (DataGrid, Memory, ObjectStore) {
    var memoryStore = new Memory({ data: [
        // data here...
    ]});
    var objectStore = new ObjectStore({ objectStore: memoryStore });

    var grid = new DataGrid({
        structure: [
            { field: 'id', name: 'ID', width: '10%' },
            { field: 'name', name: 'Name', width: '20%' },
            { field: 'description', name: 'Description', width: '70%' }
        ],
        store: objectStore
    }, 'grid');
    grid.startup();
});
```

A result similar to the above example could be achieved using dgrid with the
following styles...

```css
#dgrid .field-id {
    width: 10%;
}
#dgrid .field-name {
    width: 20%;
}
#dgrid .field-description {
    width: 70%;
}
```

...and the following JavaScript...

```js
require([
    'dojo/_base/declare',
    'dgrid/OnDemandGrid',
    'dgrid/Keyboard',
    'dgrid/Selection',
    'dstore/Memory',
    'dojo/domReady!'
], function (declare, OnDemandGrid, Keyboard, Selection, Memory) {
    var memoryStore = new Memory({data: [
        // data here...
    ]});

    var grid = new declare([ OnDemandGrid, Keyboard, Selection ])({
        columns: {
            id: 'ID',
            name: 'Name',
            description: 'Description'
        },
        collection: memoryStore
    }, 'grid');
    // dgrid will call startup for you if the node appears to be in flow
});
```

There are a few key differences worth pointing out:

* Whereas `dojox/grid` expects styles to be specified within the column definition
  to be eventually applied inline to all cells in the column, dgrid lets CSS do
  the talking whenever possible for purposes of layout and appearance.  This
  allows for better separation between visual and functional concerns.
* `dojox/grid` operates with stores implementing the earlier `dojo/data` APIs;
  in order to use it with a store instance implementing the `dojo/store` APIs,
  the store must first be wrapped using the `dojo/data/ObjectStore` module.
  On the other hand, dgrid communicates with dstore APIs out of the box.
  (Conversely, however, if you *do* need to work with a `dojo/data` store, you
  would then have to pass it through the `dojo/store/DataStore` and `dstore/legacy/StoreAdapter`
  wrappers in order for dgrid to work with it.)
* Note that in the dgrid version of the example, the Selection and Keyboard
  modules are required and mixed into the constructor to be instantiated, in
  order to enable those pieces of functionality which are baked-in by default
  in `dojox/grid` components.
* Also note that the dgrid example's structure is a bit more concise, taking
  advantage of the ability to provide simple column arrangements via an object
  hash instead of an array, in which case the object's keys double as the
  columns' `field` values (i.e., which store item properties the columns represent).

## Programmatic usage, with sub-rows

Assuming the same context as the examples in the previous section, here is a
contrived example demonstrating use of sub-rows in `dojox/grid`...

```js
var grid = new DataGrid({
    structure: [
        [
            { field: 'id', name: 'ID', width: '10%' },
            { field: 'name', name: 'Name', width: '20%' }
        ],
        [
            { field: 'description', name: 'Description', width: '70%', colSpan: 2 }
        ]
    ],
    store: objectStore
}, 'grid');
grid.startup();
```

...and the equivalent, using dgrid...
(again assuming the same context as the previous example)

```js
var grid = new declare([ OnDemandGrid, Keyboard, Selection ])({
    subRows: [
        [
            { field: 'id', label: 'ID' },
            { field: 'name', label: 'Name' }
        ],
        [
            { field: 'description', label: 'Description', colSpan: 2 }
        ]
    ],
    collection: memoryStore
}, 'grid');
```

Notice that `subRows` is now defined instead of `columns`.  The `columns`
property of dgrid components is usable *only* for simple cases involving a
single sub-row.

Also notice that each item in the top-level `subRows` array is itself another
array, containing an object for each column.  In this case, `field` must be
specified in each column definition object, since there is no longer an
object hash in order to infer field names from keys.

## Using views / columnsets

The `dojox/grid` components implement a concept known as "views", which are
represented as separate horizontal regions within a single grid.  This feature
is generally useful for situations where many fields are to be shown, and some
should remain visible while others are able to scroll horizontally.

This capability is also available in dgrid, via the ColumnSet mixin.

For instance, continuing in the vein of the examples in the previous two
sections, the following `dojox/grid` structure with multiple views...

```js
var grid = new DataGrid({
    structure: [
        { // first view
            width: '10%',
            cells: [
                { field: 'id', name: 'ID', width: 'auto' }
            ]
        },
        [ // second view
            [
                { field: 'name', name: 'Name', width: '20%' },
                { field: 'description', name: 'Description', width: '80%' }
            ]
        ]
    ],
    store: objectStore
}, 'grid');
grid.startup();
```

...could be represented in dgrid, using the following CSS...

```css
#dgrid .dgrid-column-set-0 {
    width: 10%;
}
#dgrid .field-name {
    width: 20%;
}
#dgrid .field-description {
    width: 80%;
}
```

...and the following JavaScript...
(require call included, to demonstrate additional dependency)

```js
require([
    'dojo/_base/declare',
    'dgrid/OnDemandGrid',
    'dgrid/ColumnSet',
    'dgrid/Keyboard',
    'dgrid/Selection',
    'dstore/Memory',
    'dojo/domReady!'
], function (declare, OnDemandGrid, ColumnSet, Keyboard, Selection, Memory) {
    // ... create memoryStore here ...

    var grid = new declare([ OnDemandGrid, ColumnSet, Keyboard, Selection ])({
        columnSets: [
            [ // first columnSet
                [
                    { field: 'id', label: 'ID' }
                ]
            ],
            [ // second columnSet
                [
                    { field: 'name', label: 'Name' },
                    { field: 'description', label: 'Description' }
                ]
            ]
        ],
        collection: memoryStore
    }, 'grid');
});
```

## Specifying column layout via HTML

While programmatic creation of grids is highly encouraged, dgrid does allow for
declarative specification of grid layouts via a `table` element, somewhat along
the same lines of `dojox/grid`.

In the case of dgrid, this ability is not baked in by default, but is instead
exposed primarily by the GridFromHtml module, which adds table-scanning
capabilities atop the OnDemandGrid constructor.

Note that unlike `dojox/grid`, which is *only* capable of reading declarative
layouts through the use of `dojo/parser`, dgrid is also capable of creating
instances programmatically while referencing a `table` node from which to read
a declarative layout.  For the purposes of the examples below, use of parser
will be assumed, in order to allow comparison between `dojox/grid` and dgrid
usage.

For instance, the following declarative `dojox/grid` layout...

```html
<table id="grid" data-dojo-type="dojox/grid/DataGrid"
    data-dojo-props="store: objectStore">
    <thead>
        <tr>
            <th field="id" width="10%">ID</th>
            <th field="name" width="20%">Name</th>
            <th field="description" width="70%">Description</th>
        </tr>
    </thead>
</table>
```

...could be achieved declaratively using dgrid as follows...

```html
<table id="grid" data-dojo-type="dgrid/GridFromHtml"
    data-dojo-mixins="dgrid/OnDemandList, dgrid/Keyboard, dgrid/Selection"
    data-dojo-props="collection: memoryStore">
    <thead>
        <tr>
            <th data-dgrid-column="{ field: 'id' }">ID</th>
            <th data-dgrid-column="{ field: 'name' }">Name</th>
            <th data-dgrid-column="{ field: 'description' }">Description</th>
        </tr>
    </thead>
</table>
```

Notice that rather than specifying individual non-standard attributes inside the
`th` elements, declarative layout specification with dgrid centers primarily
around a data-attribute named `data-dgrid-column`.  This attribute receives a
string representation of a JavaScript object, which will ultimately become the
basis for the column definition.  (It operates much like `data-dojo-props`,
except that the surrounding curly braces must be included.)

Note that some properties which have standard equivalents, such as `colspan` and
`rowspan`, can be specified directly as HTML attributes in the element instead.
Additionally, the innerHTML of the `th` becomes the column's label.

Note that since the modules in the dgrid package are written to be
pure AMD, they do not expose globals, so module ID syntax *must* be used when
parsing declarative dgrid instances.  `data-dojo-mixins` can be used to incorporate
the desired functionality, analogous to using `declare` programmatically.

### Column Layout via HTML with views / columnsets

While both `dojox/grid` and dgrid also enable declarative creation
of grids with multiple views/columnsets, in dgrid's case this is again separated
to its own module, GridWithColumnSetsFromHtml.  This separation exists due to
the significant amount of additional code necessary to resolve columnsets from
the representative markup, combined with the relative rarity of cases calling
for the additional functionality.

As a quick example, here is what a simple declarative grid with two views could
look like with `dojox/grid`...

```html
<table id="grid" data-dojo-type="dojox/grid/DataGrid"
    data-dojo-props="store: objectStore">
    <colgroup span="1" width="10%"></colgroup>
    <colgroup span="2"></colgroup>
    <thead>
        <tr>
            <th field="id" width="auto">ID</th>
            <th field="name" width="20%">Name</th>
            <th field="description" width="80%">Description</th>
        </tr>
    </thead>
</table>
```

...and here is the equivalent, using dgrid...
(this assumes the same styles are in play as the earlier programmatic ColumnSet
example)

```html
<table id="grid" data-dojo-type="dgrid/GridWithColumnSetsFromHtml"
    data-dojo-mixins="dgrid/OnDemandList, dgrid/Keyboard, dgrid/Selection"
    data-dojo-props="collection: memoryStore">
    <colgroup span="1"></colgroup>
    <colgroup span="2"></colgroup>
    <thead>
        <tr>
            <th data-dgrid-column="{ field: 'id' }">ID</th>
            <th data-dgrid-column="{ field: 'name' }">Name</th>
            <th data-dgrid-column="{ field: 'description' }">Description</th>
        </tr>
    </thead>
</table>
```

## Events

`dojox/grid` and dgrid take significantly different approaches to handling
events.  `dojox/grid` provides a wide selection of stub methods which
can be connected to in order to react to many common events on rows or cells in
the header or body.  The [Working with Grids](http://dojotoolkit.org/documentation/tutorials/1.7/working_grid/)
tutorial gives an idea of what kinds of events are supported by `dojox/grid`.

On the other hand, dgrid leaves it up to the developer as to which events are
at all worth listening for.  This results in generally far less overhead, since
listeners are hooked up only for events of interest; at the same time, it
still allows for the same range of event listeners as `dojox/grid`.

`dojox/grid` components generally attach any useful information directly to
the event object received by the handler callback.  While dgrid does this to a
certain degree for custom events, the most commonly-sought information is
retrievable using the `row` and `cell` methods.

See [Working with Events](../usage/Working-with-Events.md) for more information.

As a quick side-by-side comparison, here is an example logging the name property
of an item whose row was clicked, using `dojox/grid`...

```js
grid.connect(grid, "onRowClick", function(evt){
    var item = grid.getItem(evt.rowIndex);
    // don't forget to use store.getValue, since dojox/grid uses a dojo/data store
    console.log("Clicked item with name: " +
        grid.store.getValue(item, "name"));
});
```

...and using dgrid...

```js
grid.on('.dgrid-row:click', function (event) {
    var item = grid.row(event).data;
    console.log('Clicked item with name: ' + item.name);
});
```