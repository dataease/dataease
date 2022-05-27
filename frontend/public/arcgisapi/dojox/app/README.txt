-------------------------------------------------------------------------------
dojox/app 
-------------------------------------------------------------------------------
Project state: stable

This project is licensed under the Dojo Tookit licensing scheme and contributions
provided under the Dojo CLA.
-------------------------------------------------------------------------------
Project authors
	Dustin Machi
	Stephen Zhang
	Eric Wang
	Ed Chatelain
	Christophe Jolif
-------------------------------------------------------------------------------
Project description

dojox/app is a small application framework providing a set of classes to manage the the lifecycle and behavior of a single page application delivered to a mobile or desktop platform.
The main class, Application, is responsible for providing the lifecycle of the application and is designed to be easily modified with additional custom behaviors.
An application instance contains views which provide the visible user interface.
The available views, module dependencies, and other information about the application are all passed into the Application class through a JSON configuration file.
-------------------------------------------------------------------------------
Dependencies:

Dojo Core (dijit, dojox/mobile).
-------------------------------------------------------------------------------
Documentation

config.json 

The config file defines all of the dependencies, application behaviors, top level views, and any other information required for the application to function.

Example Config:
```javascript
{
	/* any object that complies with Dojo AMD loader configuration. */
	"loaderConfig" : {
		"paths": {
			"mypackage" : "can/be/found/here"
		}
	},

	/* global application dependencies */
	"dependencies": [
		"dojox/mobile/Heading",
		"dojo/mobile/RoundRect",
		"my/custom/module"
	],

	/* Application Modules.  These are implicitly added to the above set of dependencies */
	modules: [
		"dojox/app/module/history",
		"my/custom/appModule"
	],

	/* Application Controllers. */
	"controllers": [
		"dojox/app/controllers/History",
		"my/custom/appController"
	],

	/* The Application level HTML template. */
	template: "./views/example.html",
	
	/* Application level view controller. Application will have a root view even if it has no template.
	  "./views/myView.js" -- load controller from "./views/myView.js",
	  no controller -- no controller for this view */
	"controller": "./views/myView.js",

	/* the view to start on by default */
	"defaultView": "home",

	/* transition to use if none has been provided */
	"defaultTransition": "slide",

	/* Views */
	"views": {

		/* home is a top level dojox.app.view */
		"home": {

			/* dependencies specific to this view */
			"dependencies: [
				"dojox/mobile/ListItem",
				"dojox/mobile/EdgeToEdgeCategory"
			],

			/* template to use for this view */
			template: "./views/home.html"

			/* view controller. "none" -- ,
			  "./views/myHome.js" -- load controller from "./views/myHome.js",
			  no controller -- no controller for this view */
		},
	
		/* tabs view contains three child views */
		"tabs": { 
			/* the tabs view template */
			"template": "./views/tabScene.html",

			/* the default view within tabs view */	
			"defaultView": "tab1",

			/* when transitioning between tabs, use a flip animation by default */
			"defaultTransition": "flip",

			//the views available to tabs view
			"views": { 
				"tab1":{
					"template": "./views/tabs/tab1.html"
					/* no controller define for tab1 view, load tab1 view controller from the same location as the template. */
				},
				"tab2":{
					"template": "./views/tabs/tab2.html" 
				},
				"tab3":{
					"template": "./views/tabs/tab3.html" 
				}
			},

			/* dependencies specific to tabs view */
			"dependencies":["dojox/mobile/RoundRectList","dojox/mobile/ListItem", "dojox/mobile/EdgeToEdgeCategory"]
		}

	}
}
```

Property descriptions

	- loaderConfig -  This is the configuration that will be passed to the Dojo AMD loader. This allows to specify for example where the loader needs to find modules.
	 See: http://livedocs.dojotoolkit.org/loader/amd#module-identifiers

	- dependencies -  These are the modules that are required for the application to run when defined at the root of the configuration.
	 When defined inside of a view, the dependency property defines modules which must be loaded before that view can be instantiated.

	- modules -  The modules property defines application modules that will mixed into the Application class to control the lifecycle and behavior of the application. This property will become the array of mixins provided to a dojo/declare extending the base Application class. In other words, the Application class that is instantiated dynamically is created at run time using the base class and this list of modules.

	- controllers -  The controllers property defines application controllers that will be loaded during application startup to respond to events of the application. The controllers bind events on application's root domNode and the events can be triggered by application's emit() method and documented actions.

	- template -  This is the template/HTML that is used for the application when defined at the root of the configuration.
	 Within the context of a view, it is the template/HTML for defining the view.

	- controller -  This is the view controller that is used for the application template view when defined at the root of the configuration. It implements the view's life cycle interface like init(), beforeActivate(), destroy(), etc. and allows one to control the view.

	- defaultView -  The defaultView defines the startup view for the application.

	- defaultTransition -  This is the default transition method for top level views when defined at the root of the configuration.
	 When defined within a view, it is the default transition method for the associated views only.

	- views -  The views property is a nested set of objects defining the views available to the application. Details of views classes is discussed below.

	- stores -  The dojo/store implementations that will be used by the application

	- models -  The models (potentially dojox/mvc models) connecting to the stores and exposing data to the application.

	- id - The identifier of the application, a global variable with the id name is created by the application.

Some additional properties, such as name and description are reserved for future use, but their exact use is still under development.

The Application module:

The Application class itself doesn't currently exist as an exported class.
This module exports a generation function, which when provided a configuration file will declare & instantiate the application class that will actually be used on a page. Finally it then starts it up at a specific node:

```javascript
	require(["dojox/json/ref", "dojox/app/main", "dojo/text!app/config.json"],function(json, Application, config){
		app = Application(json.parse(config));
	});
```

The View module:

The View module provides a View class to construct View instances, a template rendering engine to render view template and view lifecycle APIs. Each View can have one parent view and several children views. It provides a templated container to host the domNodes for the children views. Its purpose is to allow the layout of the view to be provided through an html template and to have a set of children views which the view transitions between. For example, to display a set of tabs, you would use a View with a child view for each tab. The view's template would define where within the view the children views are displayed and where any tab buttons and such are displayed.
In this case the  "template", for the base View is pretty simple. It is a simple HTML content. However, nodes within the template can be tagged with data-app-constraint="top" (bottom, left, right) to define where that node and its children should be displayed.
For example:

```html
<div  style="background:#c5ccd3;" class="view mblView"> 
	<div data-app-constraint="top" data-dojo-type="dojox/mobile/Heading">Tab View</div>
	<ul data-app-constraint="top" data-dojo-type="dojox/mobile/TabBar" barType="segmentedControl">
		<li data-dojo-type="dojox/mobile/TabBarButton" icon1="images/tab-icon-16.png" icon2="images/tab-icon-16h.png"
			transitionOptions='{title:"TabScene-Tab1",target:"tabscene,tab1",url: "#tabscene,tab1"}' selected="true">Tab 1</li>
		<li data-dojo-type="dojox/mobile/TabBarButton" icon1="images/tab-icon-15.png" icon2="images/tab-icon-15h.png"
			transitionOptions='{title:"TabScene-Tab2",target:"tabscene,tab2",url: "#tabscene,tab2"}'>Tab 2</li>
		<li data-dojo-type="dojox/mobile/TabBarButton" icon1="images/tab-icon-10.png" icon2="images/tab-icon-10h.png"
			transitionOptions='{title:"TabScene-Tab3",target:"tabscene,tab3",url: "#tabscene,tab3"}'>Tab 3</li>
	</ul>
</div>
```

This template for the tabs view defines two areas with region top, a header and the tab buttons. The will be placed at the top of this main view when rendered.

Normally, when using a BorderContainer, one would also have a data-app-constraint="center" section. In the case of a View however, the "center" region will be applied to the currently active view (the current tab for example).

The application can also provide view controller modules to implement the View lifecyle APIs (like init(), destory(),...) for each view. The Transition controller controls the transition from one child view to another. This includes propagating transition events on to children if the active child is itself another view.

The Controller module:

The Controller module provides a base Class to control the application by binding events on application's root domNode. Several controllers required by the framework are implemented in dojox/app/controllers package:
	
   * Load controller: load view templates and view controllers
   * Transition controller: respond to "startTransition" event and do transition between views
   * Layout controller: perform views layout
   * History controller: maintain application's history. This is based on HTML5 history APIs and will not work on platforms that do not support it like IE, Android 3 & 4, iOS4, etc.
   * HistoryHash controller: maintain application's history. This is based on URL hash and has limitation if refresh the browser and back to an URL which out of current application's history stack.
	
A developer using the dojox/app framwork can define additional custom controller by extending the base class (dojox/app/Controller) and specifying them in the application configuration file. The events binding to application's root domNode is done by default.
A developer can use Controller's bind() method to bind event to document, window or dojo/Evented object if needed.
	
```javascript
define(["dojo/_base/lang", "dojo/_base/declare"], function(lang, declare){

	return declare("myApp.myController", Controller, {

		constructor: function(app, events){
			// summary:
			//		bind "myEvent1" and "myEvent2" events on application's root domNode.
			//		bind "resize" event on window
			// app:
			//		dojox.app application instance.
			// events:
			//		{event : handler}
			this.events = {
				"myEvent1": this.myEvent1,
				"myEvent2": this.myEvent2
			};
			this.inherited(arguments);

			// bind "resize" event on window
			this.bind(window, "resize", this.onResize);
		},
		
		myEvent1: function(){
			// add your code here
		},
		
		myEvent2: function(){
			// add your code here
		},
		
		onResize: function(){
			// add your code here
		}
	});
});
```
