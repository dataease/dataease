# DijitRegistry

The DijitRegistry extension adds a couple of considerations for working with
Dijit, particularly useful when a dgrid instance is to live inside of a Dijit
layout widget. Its primary purpose is to add dgrid instances to the Dijit
registry, as well as provide a couple of methods and properties which Dijit
widgets will generally expect to exist.

```js
require([
    'dojo/_base/declare',
    'dgrid/OnDemandGrid',
    'dgrid/extensions/DijitRegistry'
], function (declare, OnDemandGrid, DijitRegistry) {
    var grid = new (declare([ OnDemandGrid, DijitRegistry ]))({
        collection: myStore,
        columns: myColumns
    }, 'grid');
});
```

## Usage

See [Working with Widgets](../../usage/Working-with-Widgets.md) for more information.