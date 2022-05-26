arcgis-theme-calcite
=========================

Calcite Theme for the ArcGIS JS API and Dojo dijits - using SASS CSS

E-mail alias: patterns_calcite_dojo@esri.com 

Devtopia Wiki & documentation: https://devtopia.esri.com/WebGIS/arcgis-js-api/wiki/calcite

-----

* Status notes: https://devtopia.esri.com/WebGIS/arcgis-theme-calcite/wiki/Status
* Prioritized Mapping Widgets: https://devtopia.esri.com/WebGIS/arcgis-theme-calcite/wiki/Prioritized-Mapping-Widgets

-----

## Test pages:

* Test pages: https://devtopia.esri.com/pages/yiwe6642/arcgis-theme-calcite/ 
* Dojo theme tester: https://devtopia.esri.com/pages/yiwe6642/arcgis-theme-calcite/dijits_tester/

-----

## Build steps

*.scss files can be compiled to *.css using the build steps below. Note that ruby and compass installation is a one-time process. 

```
gem install ruby
gem install compass

cd dijit/
compass compile

cd esri/
compass compile
```

-----

## How to use:

Paste the following code into the `<head>` section of your site's HTML:
```
<link rel="stylesheet" href="path/to/calcite/dijit/calcite.css">
<link rel="stylesheet" href="path/to/calcite/esri/esri.css">
```
And add class name `"calcite"` to the `<body>` tag.

-----

### Dijits supported:

**ArcGIS API for JS dijits**
- dijit/
    - BasemapGallery
    - BasemapToggle
    - Bookmarks
    - Directions
    - HomeButton
    - LayerSwipe
    - Legend
    - LocateButton
    - Measurement
    - OverviewMap
    - Popup
    - Print
    - Scalebar
    - Search
    - TimeSlider
- SimpleSilder

**Dojo dijits**
- form/
    - Button
    - CheckBox
    - ComboBox
    - ComboButton
    - CurrencyTextBox
    - DateTextBox
    - DropDownButton
    - FilteringSelect
    - HorizontalSlider
    - MultiSelect
    - NumberSpinner
    - NumberTextBox
    - RadioButton
    - Select
    - SimpleTextarea
    - TextBox
    - Textarea
    - TimeTextBox
    - ToggleButton
    - ValidationTextBox
    - VerticalSlider
- layout/
    - AccordionContainer
    - BorderContainer
    - ContentPane
    - TabContainer
- Calendar
- CheckedMenuItem
- ColorPalette
- Dialog
- DropDownMenu
- Editor
- InlineEditBox
- Menu
- MenuBar
- MenuBarItem
- MenuItem
- MenuSeparator
- PopupMenuBarItem
- PopupMenuItem
- ProgressBar
- RadioMenuItem
- TitlePane
- Toolbar
- ToolbarSeparator
- Tooltip
- TooltipDialog
- Tree