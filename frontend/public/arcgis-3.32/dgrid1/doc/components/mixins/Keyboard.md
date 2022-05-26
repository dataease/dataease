# Keyboard

This mixin adds keyboard handling functionality. The arrow keys can be used to
navigate the focus across cells and rows, providing accessibility and ease of
use. The page up and page down keys may also be used for faster navigation,
traversing the number of rows specified in the `pageSkip` property of the
instance. When used with grids, this mixin references the `cellNavigation`
property of the grid instance, to determine whether keyboard navigation and
focus should operate at the individual cell level (`true`, the default) or at
the row level (`false`).

```js
require([
    'dojo/_base/declare',
    'dgrid/OnDemandGrid',
    'dgrid/Keyboard'
], function (declare, OnDemandGrid, Keyboard) {
    var grid = new (declare([ OnDemandGrid, Keyboard ]))({
        pageSkip: 20,
        cellNavigation: false
    }, 'grid');
});
```

## APIs

The Keyboard mixin supports the following additional instance properties and methods.

### Property Summary

Property | Description
-------- | -----------
`cellNavigation` | Boolean indicating whether keyboard navigation should occur at the row or cell level.  Defaults to `true` (cell-level) for grids, `false` (row-level) for lists.
`pageSkip` | Number indicating how many rows to navigate when page up or page down is pressed.  Defaults to `10`.
`keyMap` | Object hash mapping key codes to callbacks to be executed when the respective keys are pressed within the body of the grid.  This is typically augmented by calling `addKeyHandler`, but it can also be completely overridden by passing an object hash to the constructor (or otherwise creating one before `Keyboard#postMixInProperties` executes).  The default `keyMap` is exposed via `Keyboard.defaultKeyMap`.
`headerKeyMap` | Object hash mapping key codes to callbacks to be executed when the respective keys are pressed within the header of the grid.  This is typically augmented by calling `addKeyHandler`, but it can also be completely overridden by passing an object hash to the constructor (or otherwise creating one before `Keyboard#postMixInProperties` executes).  The default `headerKeyMap` is exposed via `Keyboard.defaultHeaderKeyMap`.

### Method Summary

Method | Description
------ | -----------
`addKeyHandler(key, callback, isHeader)` | Registers the given callback to be executed when the indicated key code is pressed.  By default the handler is registered for keypresses within the body of the grid; if `isHeader` is true, it is registered for header keypresses instead.
`focus([target])` | Focuses the given target cell or row (depending on the value of `cellNavigation`), or on the last focused target if none is provided.
`focusHeader([cell])` | Focuses the header row.  If `cellNavigation` is true, will focus on the given cell, or the last header cell that was focused if none is provided.

**Note:** The `focus` method can be passed any type of argument acceptable to
List's `row` method or Grid's `cell` method (depending on the value of
`cellNavigation`).

## Events

The Keyboard mixin also emits two custom events, `dgrid-cellfocusin` and
`dgrid-cellfocusout`, when keyboard navigation occurs. Both events bubble, and
include the following properties:

* `grid`: The instance in which the event occurred
* `row` or `cell` (depending on value of `cellNavigation` instance property): A row or cell object representing the target of the event
* `parentType`: If the event was triggered by user interaction, this property indicates what type of event originally triggered the event

## Customizing Mappings

The Keyboard mixin provides two ways to customize handling of keys pressed within the body or header of a grid.

### Adding Mappings

In many cases it may be desirable to map keys in addition to the defaults.  This can easily be accomplished by calling the `addKeyHandler` method with a key code and a callback to register.  This method may be called multiple times for the same key code; Keyboard uses `dojo/aspect` to apply new callbacks in sequence if there is an existing callback registered.  For example, to hook up an action to the enter key:

```js
// Using direct code here; could also use keys.ENTER from dojo/keys
grid.addKeyHandler(13, function(event) {
    // Do something in response to a keydown event for Enter
});
```

Note that the Keyboard mixin does not automatically call `preventDefault` or `stopPropagation`; it is up to the discretion of the developer as to whether the event should be canceled or allowed to bubble.  However, the default key map does include handlers to call `preventDefault` for events on the spacebar, since various dgrid components make use of the spacebar and some browsers would scroll the viewport if this were not canceled.

### Overriding Defaults

In cases where the provided default key mappings are undesirable, they can be overridden by passing `keyMap` and `headerKeyMap` object hashes to the constructor.

For reference, the default key mappings are available via `Keyboard.defaultKeyMap` and `Keyboard.defaultHeaderKeyMap`.  Note, however, that modifying these directly would affect every grid instance created thereafter using the Keyboard mixin.  Instead, if some of the defaults are still desired, use `lang.clone` or `lang.mixin` from `dojo/_base/lang` to mix it in and then overwrite some values.  For example:

```js
function doSomethingOnPageUp(event) {
    // Do something in response to a keydown event for Page Up
}
function doSomethingOnPageDown(event) {
    // Do something in response to a keydown event for Page Down
}

var grid = new (declare([ Grid, Keyboard ]))({
    // Override page up and page down, but keep the rest of the defaults.
    // Don't forget the first empty object parameter - this prevents from
    // accidentally mixing into defaultKeyMap instead!
    keyMap: lang.mixin({}, Keyboard.defaultKeyMap, {
        33: doSomethingOnPageUp,
        34: doSomethingOnPageDown
    }),
    // other properties here...
});
```

### Default Mappings

The following table outlines the default key mappings, including the functions that provide their behavior, which can be reused if desired.  These functions are executed in instance context, and are passed the keydown event received by the delegated event handler responsible for dispatching to all mapped callbacks.

Key | Action | Function
--- | ------ | --------
Up | Move focus up by one row or cell (grid body only). | `Keyboard.moveFocusUp`
Down | Move focus down by one row or cell (grid body only). | `Keyboard.moveFocusDown`
Left | Move focus left by one cell (only when `cellNavigation` is `true`). | `Keyboard.moveFocusLeft`
Right | Move focus right by one cell (only when `cellNavigation` is `true`). | `Keyboard.moveFocusRight`
Page up | Move focus up by the number of rows indicated by the `pageSkip` instance property (grid body only). | `Keyboard.moveFocusPageUp`
Page down | Move focus up by the number of rows indicated by the `pageSkip` instance property (grid body only). | `Keyboard.moveFocusPageDown`
Home | In the header, move focus to the first cell (only when `cellNavigation` is `true`); in the body, move focus to the first row in the list or grid. | `Keyboard.moveFocusHome`
End | In the header, move focus to the last cell (only when `cellNavigation` is `true`); in the body, move focus to the last row in the list or grid. | `Keyboard.moveFocusEnd`