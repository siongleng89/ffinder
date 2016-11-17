<?php
	include 'firebaseInterface.php';
	include 'firebaseStub.php';
	include 'firebaseLib.php';
	include 'firebase_details.php';
	include 'BeforeValidException.php';
	include 'ExpiredException.php';
	include 'JWT.php';
	include 'secret_check.php';
        include 'iab_helper.php';
	
	
	if(!checkSecretMatched($_POST["restSecret"])){
		return;
	}
	
	$token = $_POST["token"];
        $productId = $_POST["productId"];
	
        if(empty($token) || empty($productId)){
            echo "-1";
            return;
        }
        
        $firebase = new \Firebase\FirebaseLib($DEFAULT_URL, $DEFAULT_TOKEN);
         
        $json = getPurchaseDetails($productId, $token, 0, $firebase);
        if($json == -1){
            echo "-1";
            return;
        }
        else{
            $remainingMilicSecs = $json->expiryTimeMillis - round(microtime(true) * 1000);
            echo $remainingMilicSecs;
            return;
        }
	
	
	
	
	
	
?>
