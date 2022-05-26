# util/mouse

The `util/mouse` module defines a number of extension events which are useful in
situations which require the mouse moving into and/or out of rows or cells.
These scenarios warrant extension events due to the often-problematic bubbling
nature of the `mouseover` and `mouseout` DOM events.

```js
require(["dgrid/util/mouse"], function(mouseUtil){
    // Assume we have a Grid instance in the variable `grid`...
    grid.on(mouseUtil.enterRow, function(event){
        var row = grid.row(event);
        // Do something with `row` here in reaction to when the mouse enters
    });
});
```

## Events

The following extension events are provided:

* `enterRow`: fires when the mouse moves into a row within the body of a list
  or grid.
* `leaveRow`: fires when the mouse moves out of a row within the body of a list
  or grid.
* `enterCell`: fires when the mouse moves into a cell within the body of a grid.
* `leaveCell`: fires when the mouse moves out of a cell within the body of a
  grid.
* `enterHeaderCell`: fires when the mouse moves into a cell within the header of
  a grid.
* `leaveHeaderCell`: fires when the mouse moves out of a cell within the header
  of a grid.

These extension events can be used as indicated in the example above, further
described in the respective section of the
[`dojo/on` Reference Guide](http://dojotoolkit.org/reference-guide/dojo/on.html#extension-events).