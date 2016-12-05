<?php
	include 'firebaseInterface.php';
	include 'firebaseStub.php';
	include 'firebaseLib.php';
	include 'firebase_details.php';
	include 'BeforeValidException.php';
	include 'ExpiredException.php';
	include 'JWT.php';
	include 'SignatureInvalidException.php';
        
	$key = $_GET["code"];
	$firebase = new \Firebase\FirebaseLib($DEFAULT_URL, $DEFAULT_TOKEN);
	
	$name = json_decode($firebase->get('keys/'.$key.'/userName'));
	
        echo $name;
        
	return;
	
	
	
	
	
	
	
	
?>
