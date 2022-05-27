# DnD

The DnD extension can be used to add row drag'n'drop functionality.

**Note:** Be sure to include `dojo/resources/dnd.css` or equivalent custom styles when using this extension.

```js
require([
    'dojo/_base/declare',
    'dgrid/OnDemandGrid',
    'dgrid/extensions/DnD',
    'dojo/dnd/Source'
], function (declare, OnDemandGrid, DnD, DnDSource) {
    var grid = new (declare([ OnDemandGrid, DnD ]))({
        collection: myStore,
        columns: {
            name: 'Name'
            // ...
        }
    }, 'grid');

    // Set up another target
    var target = new DnDSource('target', {
        accept: [ 'dgrid-row' ],
        isSource: false
        // Optionally, override onDrop(source, nodes) with custom behavior
    });
});
```

## Requirements

The DnD extension assumes usage of a store-backed component, most commonly an
[OnDemandGrid](../core-components/OnDemandList-and-OnDemandGrid.md#ondemandgrid)
instance. The store should be order-aware, supporting the `options.beforeId`
parameter on `add()` and `put()` calls to properly respond to DnD operations.
(`dstore/Memory` and `dstore/Rest` both support this feature.)

Additionally, if the store supports a `copy` method, it will be called for DnD
copy operations within the same list/grid (since a `put` would normally relocate
the item).

Note that the DnD extension inherits the [Selection](../mixins/Selection.md) mixin, which allows it to
behave more resiliently when dragging items.

## API

The DnD extension supports the following additional instance properties.

### Property Summary

Property | Description
-------- | -----------
`dndSourceType` | String specifying the type of DnD items to host and accept. Defaults to `dgrid-row`.
`dndParams` | Object to be passed as the second argument to the DnD Source constructor.  Note that the `accept` DnD parameter is set to match `dndSourceType` by default, but this may be overridden.
`dndConstructor` | Constructor from which to instantiate the DnD Source for the grid.  This defaults to the `GridSource` constructor defined and exposed by the DnD module itself.  It is recommended to only override this value with a constructor which extends `GridSource`.

## Touch Support

The `dojo/dnd` package supports interaction via touch events.
However, since touch events are also used to control scrolling of dgrid
components on touch devices, a conflict ensues and touch scrolling wins by default.
The typical way to resolve this is to set up DnD handles somewhere in the grid,
and instruct the DnD source to only drag by handles by passing `withHandles: true` in `dndParams`.

An example of this solution can be found in `test/extensions/DnD_touch.html`.

## Styling DnD Avatars

Styling can be accomplished the same as for any element that uses `dojo/dnd`.
See the [CSS classes section](http://dojotoolkit.org/reference-guide/dojo/dnd.html#id10)
of the `dojo/dnd` Reference Guide page for details.

The following are generally the primary classes of interest:

* `dojoDndAvatar`: the top-level node of the avatar; useful for setting dimensions
* `dojoDndAvatarItem`: the content node of the avatar; useful for setting font properties
