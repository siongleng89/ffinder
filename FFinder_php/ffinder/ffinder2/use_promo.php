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
	$promoCode = htmlspecialchars($_POST["promoCode"]);
		
	$firebase = new \Firebase\FirebaseLib($DEFAULT_URL, $DEFAULT_TOKEN);
	
	$dbCodes = json_decode($firebase->get('promoCodes/'));
	
	$credits = 0;
	
	$matched = false;
	if(!empty($dbCodes)){
		foreach($dbCodes as $key=>$value){
			if($key == $promoCode){
				$matched = true;
				$credits = $value;
				break;
			}
		}
	}
	
	
	if(!$matched){
		echo -1;
		return;
	}
	
	$usedBefore = json_decode($firebase->get('promoUsages/'.$userId.'/'.$promoCode));
	
	if(!empty($usedBefore)){
		echo -1;
		return;
	}
	
	$test = array(
	    ".sv" => "timestamp"
	);
	
	$firebase->set('promoUsages/'.$userId.'/'.$promoCode, $test);
	
	
	$currentCount = json_decode($firebase->get('nextAds/'.$userId.'/count/'));
	
	if(empty($currentCount)){
		$currentCount = json_decode($firebase->get('showAdsIn/'));
	}
	
	$currentCount += $credits;
	
	$firebase->set('nextAds/'.$userId.'/count/', $currentCount);
	
	echo $credits;
	return;
	
	
	
	
	
	
	
	
?>
