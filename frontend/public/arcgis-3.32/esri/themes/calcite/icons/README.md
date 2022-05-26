# CalciteIcons Font Project

**This is still in an internal ALPHA (work-in-progress) state**

Icons (SVG, icon font, & sprite) in the Calcite theme for JS API. [Devtopia Wiki](https://devtopia.esri.com/WebGIS/arcgis-js-api/wiki/arcgis-theme-calcite)

## Use within Calcite theme
* Icon-Font with Stylus
 * A way to reference the icons from the icon-font can be done calling **iconfont(..)** with a string parameter of the icon needed. The function will return the CSS property and value, for example: *content: "\e645";*
 * https://devtopia.esri.com/WebGIS/arcgis-theme-calcite/blob/master/css/resources/iconfonts.styl
 * Note: When the Icon-font source files are updated, the iconfont.styl file needs to be updated with the respective icon references within the **iconfont(..)** function.


----
## Questions
  Questions about an icon? Need an icon that isn't included in the library/repo? E-mail calcite@esri.com

  
## Demos

Open *[demo.html](https://devtopia.esri.com/pages/WebGIS/arcgis-theme-calcite-icons/demo.html)* to see a list of all the glyphs in your font along with their codes/ligatures.

You won't need any of the files located under the *demo-files* directory when including the generated font in your own projects.

## Directory

Below is the file structure for this project.

* `demo-files/` Files used to preview the font.
* `fonts/` contains the icon font files for different browsers.
* `ie7/` contains files to support ie7.
* `images/` contains icon images in various formats.
* `images/svg/` has SVG icons that are **NOT** used for the icon font.
* `images/help/` has images specific to demos or using this repository.
* `demo.html` Used to preview the icon font.
* `selection.json` IcoMoon project settings file.
* `style.css` CSS file for using the font within a project.

## How to update this font

You can import *selection.json* back to the IcoMoon app using the *Import Icons* button (or via Main Menu > Manage Projects) to retrieve your icon selection.

1. Head to the [icomoon projects page](https://icomoon.io/app/#/projects).
2. Click on the Import Project button. <br />![import](https://devtopia.esri.com/WebGIS/arcgis-theme-calcite-icons/raw/master/images/help/import.png)
3. Browse for the selection.json file that contains the project settings and icon-to-character mappings.
4. Load the imported project. <br />![load](https://devtopia.esri.com/WebGIS/arcgis-theme-calcite-icons/raw/master/images/help/load.png)
5. Import new SVG icons, remove existing icons or modify existing icons. <br />![import icons](https://devtopia.esri.com/WebGIS/arcgis-theme-calcite-icons/raw/master/images/help/import-icons.png)
6. Download the font. <br />![download](https://devtopia.esri.com/WebGIS/arcgis-theme-calcite-icons/raw/master/images/help/download.png)
7. Replace the files in this repository with the downloaded files.
