# util/misc

The `util/misc` module provides miscellaneous utility functions. Currently this
consists primarily of functions related to throttling or debouncing function
calls, which can be particularly useful for events which tend to fire rapidly,
such as scroll.  As of dgrid 0.3.5, `util/misc` is also home to the `addCssRule`
function, used by various APIs across dgrid components.

```js
require(["dgrid/util/misc"], function(misc){
    // Produce a new function which will call `myFunction` a maximum of
    // once per second
    var myThrottledFunction = misc.throttle(myFunction, null, 1000);
})
```

## Usage

### Throttling and Debouncing

For those unfamiliar with the distinction between throttling and debouncing,
they both center around the idea of limiting the frequency in which functions
are called.

* *Throttling* limits a function to being called at most once within a given
  period of time; subsequent calls within the same time window are ignored.
* *Debouncing* involves waiting until a certain amount of time has
  elapsed before calling the function; the target function will not be
  called at all until no calls have been made within that amount of time.

## APIs

### Property Summary

Property | Description
-------- | -----------
`defaultDelay` | Default delay (in milliseconds) used for `throttle`, `debounce`, and `throttleDelayed`; default value is `15`.

### Function Summary

Function | Description
-------- | -----------
`addCssRule(selector, rule)` | Programmatically adds styles for a given CSS selector by injecting a rule into a stylesheet in the document.  Returns a handle with a`remove` function, which can be called to later remove the added style rule.  This function is used by instance methods such as `List#addCssRule` and `Grid#styleColumn`; note however that calling the function in `util/misc` directly will not set up automatic removal at any given point.
`escapeCssIdentifier(id)` | Escapes characters in the given string that would be invalid in a CSS identifier (i.e. a tag name, class name, or id in a selector).  Used internally by dgrid modules for calls to `addCssRule` and derivatives.
`throttle(func, context, delay)` | Returns a new function throttling the original.  `context` defaults to the global object; `delay` defaults to `defaultDelay`.
`debounce(func, context, delay)` | Returns a new function debouncing the original.  `context` and `delay` have the same defaults as for `throttle`.
`throttleDelayed(func, context, delay)` | Like `throttle`, but runs the original function *after* each time window elapses, rather than before.  This can be useful for things like scroll events, where the last event is really what matters, but you still want to handle intermediate events as well.