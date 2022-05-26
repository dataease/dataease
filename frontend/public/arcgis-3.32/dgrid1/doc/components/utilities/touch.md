# util/touch

The `util/touch` module defines two simple extension events, `tap` and `dbltap`,
for detecting the respective actions on touch devices.  It also defines utility
functions for handling touch events.

```js
require([ 'dgrid/util/touch' ], function (touchUtil) {
    // Assume we have a Grid instance in the variable `grid`...
    grid.on(touchUtil.selector('.dgrid-content .dgrid-row', touchUtil.dbltap), function (event) {
        var row = grid.row(event);
        // Do something with `row` here in reaction to when it is double-tapped
    });
});
```

## APIs

In addition to the `tap` and `dbltap` extension events, `util/touch` also
defines the following functions.

### Function Summary

Function | Description
-------- | -----------
`countCurrentTouches(event, node)` | Counts the number of currently active touches which fall within the given node; useful in cases where other handlers may call `stopPropagation`, thus affecting other means of counting touches.
`selector(selector, eventType, children)` | A version of the `selector` function from `dojo/on`, with an additional fix to work around issues experienced on iOS Safari.  This is used by `dgrid/Selection` and `dgrid/Tree` for touch event handling.
