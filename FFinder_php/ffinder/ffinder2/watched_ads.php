<?php
	include 'firebaseInterface.php';
	include 'firebaseStub.php';
	include 'firebaseLib.php';
	include 'firebase_details.php';
	include 'BeforeValidException.php';
	include 'ExpiredException.php';
	include 'JWT.php';
	include 'SignatureInvalidException.php';
	include 'secret_check.php';
	
	
		
	
	if(!checkSecretMatched($_POST["restSecret"])){
		return;
	}
	
	$userId = htmlspecialchars($_POST["userId"]);
		
	$firebase = new \Firebase\FirebaseLib($DEFAULT_URL, $DEFAULT_TOKEN);
	
	$maxCount = json_decode($firebase->get('showAdsIn/'));
	
        $currentCount = json_decode($firebase->get('nextAds/'.$userId.'/count'));
        
        //user current count is higher than maxcount, no need set to max count 
        if(!empty($currentCount) && (int)$currentCount > (int)$maxCount){
            echo $currentCount;
            return;
        }
        else{
            $firebase->set('nextAds/'.$userId.'/count', $maxCount);
            echo $maxCount;
            return;
        }
        
	
	
	
	
	
	
	
	
?>
