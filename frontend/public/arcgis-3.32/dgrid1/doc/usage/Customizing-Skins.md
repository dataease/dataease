# Customizing Skins

The skins in dgrid 1.0 are implemented using [Stylus](http://stylus-lang.com/), and are designed to be
easily extensible.  All skins consist primarily of a series of variable definitions, which are used by
`skin.styl` to generate commonly-used styles.

In order to further customize dgrid skins externally to dgrid itself, you will need to either install Stylus and Nib
globally (i.e. `npm i -g stylus nib`), or implement a workflow (e.g. via npm scripts or Grunt) which incorporates them
as dependencies (see dgrid's `package.json` and `Gruntfile.js` for example).

If running stylus from the command line, you will need to pass `-u nib` in order for requires/imports of nib components
to work.

The following is the general format for skins, and can be followed to create new skins as well:

```
@require 'nib/gradients'; // Include if you intend to use CSS gradients
@require 'nib/vendor'; // Include for automatic vendor prefixing

// Define variables here

.yourskinclass {
	@import 'dgrid/css/skin/skin'; // Generate styles based on defined variables

	// Include any extra specific styles not covered by skin.styl here
}
```

See `dgrid/css/skin.styl` for supported variables.

## Extending an existing skin by overriding variables

Each of the included skins uses the conditional assignment operator (`?=`) when defining its variables.  This makes
overriding skin variables in Stylus as easy as defining your own values prior to including the skin:

```
// You can optionally use conditional assignment as well to make your own extensions extensible
$dgrid-body-row-odd-background ?= #e7f0f7;
@import 'dgrid/css/skin/slate';
```

## Common dgrid CSS classes

The following is a list of commonly-used dgrid CSS classes:

* `dgrid`: The top-level element of each List or Grid
* `dgrid-header`: The element containing the header row
* `dgrid-header-scroll`: The element in the top-right corner of a Grid (or top-left for RTL),
above the vertical scrollbar
* `dgrid-footer`: The (optional) element at the bottom of the grid (e.g. used for Pagination controls)
* `dgrid-scroller`: The element responsible for scrolling the grid's data
* `dgrid-content`: The element inside of the scroller area, which displays all of the data
* `dgrid-row`: Each row element
* `dgrid-row-even`: Each even row element
* `dgrid-row-odd`: Each odd row element; applying a different color to alternating rows can help
visually distinguish individual items
* `dgrid-cell`: Each cell element
* `dgrid-selected`: Each selected row or cell
* `dgrid-focus`: The focused element (row or cell); added by the Keyboard mixin
* `dgrid-expando-icon`: The expando icon in Tree cells
* `dgrid-row-expandable`: Added to parent rows that can be expanded with the `Tree` mixin
* `dgrid-row-expanded`: Added to parent rows that have been expanded when using the `Tree` mixin

When `addUiClasses` is set to `true` (the default), the following generic class
names are also available for generic skinning (following the jQuery ThemeRoller convention):

* `ui-widget-content`: Applied to each dgrid list or grid at the top element
* `ui-widget-header`: Applied to the element that contains the header rendering
* `ui-state-default`: Applied to each row element
* `ui-state-active`: Applied to selected rows or cells
* `ui-state-highlight`: Applied to a row for a short time when the contents are changed (or it is newly created)
