<?php

// THIS IS AN EXAMPLE
// you will obviously need to do more server side work than I am doing here to check and move your upload.
// API is up for discussion, jump on http://dojotoolkit.org/forums

// JSON.php is available in dojo svn checkout
require("../../../dojo/tests/resources/JSON.php");
$json = new Services_JSON();

// fake delay
sleep(3);
$name = empty($_REQUEST['name'])? "default" : $_REQUEST['name'];
if(is_array($_FILES)){
	$ar = array(
		// lets just pass lots of stuff back and see what we find.
		// the _FILES aren't coming through in IE6 (maybe 7)
		'status' => "success",
		'name' => $name,
		'request' => $_REQUEST,
		'postvars' => $_POST,
		'details' => $_FILES,
		// and some static subarray just to see
		'foo' => array('foo'=>"bar")
	);

}else{
	$ar = array(
		'status' => "failed",
		'details' => ""
	);
}

// yeah, seems you have to wrap iframeIO stuff in textareas?
$foo = $json->encode($ar);
?>
<textarea><?php print $foo; ?></textarea>
