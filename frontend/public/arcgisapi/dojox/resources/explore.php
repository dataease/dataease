<?php // IF you don't have PHP5 installed, you can't use this index! ?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
	"http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>

	<link rel="stylesheet" href="../../dijit/tests/css/dijitTests.css">
	<link rel="stylesheet" href="../../dijit/themes/tundra/tundra.css">
	<link rel="stylesheet" href="../../dojox/widget/Dialog/Dialog.css">
	<style type="text/css">
		.innard {
			padding:12px;
			margin-top:0;
		}
	</style>
	<title>Dojo Toolkit - DojoX Demos and Tests by Project</title>

	<script src="../../dojo/dojo.js"></script>
	<script type="text/javascript">
		dojo.require('dojox.widget.Dialog');
		dojo.require("dojo.fx.easing");
		dojo.addOnLoad(function(){
			var dialog = new dojox.widget.Dialog({ title: "About", viewportPadding:100, fixedSize:true });
			dialog.startup();
			dojo.query("span.projectname + a")
				.connect("onclick",function(e){
					e.preventDefault();
					dojo.xhrGet({
						url: e.target.href,
						load: function(data){
							var content = data.replace(/\</g,"&lt;");
							dialog.show();
							dialog.setContent("<pre class='innard'>" + content + "</pre>");
						}
					})
				})
			;
		});
	</script>

</head>
<body>

	<h1 class="testTitle">DojoX test files overview</h1>
	
	<table id="testMatrix">
		<thead>
			<tr class="top"><th rowspan="2">Test</th><th colspan="4">Tundra</th><th colspan="4">Nihilo</th><th colspan="4">Soria</th></tr>
			<tr class="tests"><th>Normal</th><th>a11y</th><th>rtl</th><th>a11y + rtl<th>Normal</th><th>a11y</th><th>rtl</th><th>a11y + rtl<th>Normal</th><th>a11y</th><th>rtl</th><th>a11y + rtl</tr>
		</thead>
		<tbody><?php

			foreach(getprojects() as $project){
				$note = "";
				if($project['readme']){
					$note = "<a class='readmeLink' href='".$project['readme']."'>about</a>"; 
				}
				if($project['tests'] || $project['demos']){
					print "<tr class='spacer'><td colspan='13'><span class='projectname'>dojox.". $project['name'] . "</span> ".$note."</td></tr>";

					if($project['tests']){
						printLinks($project['tests']);					
					}
					if($project['demos']){
						printLinks($project['demos']);
					}

				}
				
			}
		//	printLinks("./tree","Dijit Tree Tests");
			
		?>
		</tbody>
	</table>

	<p>* note: All themes and modes included for completeness. Some projects don't even use themes. The "basic" link
		is the direct link to the file with no enhancements. The test file must include _testCommon from dijit to
		include theme / rtl / a11y testing capabilities.</p>
</body>
</html>
<?php

function getprojects(){
	$projects = array();
	$path = "../../dojox";
	$handle = opendir($path);
	while(false !== ($file = readdir($handle))){
		$README = "";
		$full = $path."/".$file;
		if(is_dir($full)){
			if(file_exists($full."/README")){
				$README = $full."/README";
			}
			$tests = false; $demos = false;
			if(is_dir($full."/tests")){
				$tests = $full."/tests";
			}
			if(is_dir($full."/demos")){
				$demos = $full."/demos";
			}
			$projects[] = array(
				"name" => $file,
				"tests" => $tests,
				"demos" => $demos,
				"readme" => $README
			);
		}
	}
	return $projects;
}

function printLinks($path){
	$handle = opendir($path);
	$i = 0;

	while(false !== ($file = readdir($handle))){
		if(preg_match("/([a-zA-Z])(.*)\.html/", $file, $matches)){
			$base = $matches[0];
			$link = $path."/".$matches[0];
			print 
			"<tr class='testRow ". (++$i % 2 ==0 ? "alt" : "")   ."'>" .

				"<td class='label'>" . $base . "</td>" .

			    // standard / tundra:
				"<td><a href='".$link."'>basic</a></td>" .
				"<td><a href='".$link."?a11y=true'>a11y</a></td>" .
				"<td><a href='".$link."?dir=rtl'>rtl</a></td>" .
				"<td><a href='".$link."?dir=rtl&amp;a11y=true'>both</a></td>" .
				
				// nihilo
				"<td><a href='".$link."?theme=nihilo'>nihilo</a></td>" .
				"<td><a href='".$link."?theme=nihilo&amp;a11y=true'>a11y</a></td>" .
				"<td><a href='".$link."?theme=nihilo&amp;dir=rtl'>rtl</a></td>" .
				"<td><a href='".$link."?theme=nihilo&amp;dir=rtl&amp;a11y=true'>combo</a></td>" .

				// soria
				"<td><a href='".$link."?theme=soria'>soria</a></td>" .
				"<td><a href='".$link."?theme=soria&amp;a11y=true'>a11y</a></td>" .
				"<td><a href='".$link."?theme=soria&amp;dir=rtl'>rtl</a></td>" .
				"<td><a href='".$link."?theme=soria&amp;dir=rtl&amp;a11y=true'>combo</a></td>" .
									
			 "</tr>";
		}
	}
}

?>
