<?php
	require_once("./JSON.php");

	$filename = "./logs/analytics.log";
	$json = new Services_JSON;

	$id = $_REQUEST["id"];
	$items = $json->decode($_REQUEST["data"]);

	if (!$handle = fopen($filename, 'a+')) {
		print '{error: "server error"}';
		exit;
	}

	foreach($items as $i=>$item){
		$item->_analyticsId = $id;
		$item->_analyticsTimeStamp = time();
		$log = $json->encode($item) . "\n";
		fwrite($handle, $log);
	}
	
	fclose($handle);

	$response = '{"eventsReceived": "' . sizeof($items) . '", "id": "' . htmlentities($id) . '"}';
	if ($_REQUEST["callback"]){
		print htmlentities($_REQUEST["callback"]) . "(" . $response . ");";
	}else{
		print $response;
	}
	
?>
