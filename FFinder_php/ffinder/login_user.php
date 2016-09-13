<?php
	include 'firebase_details.php';
	include 'BeforeValidException.php';
	include 'ExpiredException.php';
	include 'JWT.php';
	include 'SignatureInvalidException.php';
	include 'secret_check.php';
	use Firebase\JWT\JWT;
	
	
	
	
	$service_account_email = "ffinder-74ebd@appspot.gserviceaccount.com";
	$private_key = "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDVZ/KTLdkhl0qy\n+mAJoDkmdEIxa54P98NG5DWVdL9sBR7RzkvPy5WVcNw6nPCA1J/nT7NcQS19ca5E\nq23oCbQP/iqxA4AMasD+Ov9IRXMzTZ7spBZd90VSDIDDaX3v+icmwge88HZxtd0h\n8LCqG4W2ps26Fnf9OwxcXKIiQNj84Fe5yxfhzSVssGv9ThWRXeqRMXpUl8DWhFyZ\nJHfbMkPcOKz0HYvoYvTyChfUcVMzly46Es+yZr56GDcQVMibZ4bzDeH6gaxcdBUK\nOJUpmVsrVS87L3Qlh65Ue37tnUBi1I9uIQJKFNHqcOKkilTpHAWBWV+ZbL2ZwYlD\niIR+l2DrAgMBAAECggEAQjdZtj9Ao0KdZAF6MSJs+TiTnWAGEHBRQDrpQXGTP8Iq\n+PCo51YFGPSG8QznNyJCZ3R8c8Cpi8XtS9Nha+Eu5NN5jalHXxL0xS2rLlKWVzHb\ngRO8+PUTpkzI21ltQTv+T/Fek0qNheTnM3PKbU3S5ITHStlT69gZksMPb89tRLEK\nhb+VaIs2CLCs1mhQ9gE7Z6G7z/Hy4Mvc32my2NWs/FVFNqT8cdqwElFrO0fbfLEV\n41G8CGO1eZ7Wo9askG4FG/z1DYpvblzM7ixb87nNYazxO/YZ0lCMNiVbLB05F2OS\nehEE503jQ3EqYc43GSxPg6giOCK0AiO51TgPOu5I0QKBgQDxsHC4/yi97Djyf2V3\n6C0YPy3KrO6JL6lMvVhqgOc2wMrylCNRrlHQLnjenWnFIQCF4qRysWOY2NzPE3X7\nkL4nChKvN9AEg3IeFE1N1DkODpwMd4AfBLk66V5PUuafexjOWV/geeGBul2bTWvh\nRVRWM12jXXv/KryfCyK5/qMkmQKBgQDiCsluFCwyLO3LDkdlEZcHDtZIvikpuASZ\nAEko4OCC+qSvb5+L+49tFqMEgg9DL3+f8IHSWH9XKfVrgRPSQJLSGyKwm5k7q/xr\n+hhSK/LMsnEQbFmQxS2Xx5lmHzi8EJoa6/Jhkrr3cV6m6thLL51tUBPbjXGELFNI\nWZV8TxBgIwKBgAfgkEv3RQSrpk+BB3WZA58+r+djK4MdIo91vGP8P4zxS6wMV/pZ\njCEU4quexcu+/51dfa6fSXe52biGEdpZJVlwYpuDXixHfHiugLaArDthDoT5gBuC\ns+bPpFtBgHLCOFTBZPaUAl2QDWz6YQG+mLkWg37HlTsD0ZH9cuxM+FRBAoGAZ/48\nc6dWOfNPp1a3Y/k2uWmLDkZmW6ose1OVVPg4cinr8EZzaYxfjPmYIC/R48ALgEK/\nB03tr+U+4qmsu9M3ePBBAm1jjv9uDyMtY0iS2LfTqpGelysP8b9DhKcii+s7at1Y\nUqmV2NdQi4yJ2/VZAPyRVjX5nqxh2GxWiK07RG8CgYEAnxpqdolcyo+tXNY/gB5Z\n2VmA719+knngSkrq4iQLBKFyvE7daHzpcbg4F3wOd5ZGJ8RfdSpb/Zq4qQBcnxfv\ndNuwFeJmo5fwnCZR8PIsdgQL9HkoDleCeSyT5h2rqPwv5JYRNlij543FEdrnQhcA\nrDjqyWWiAFaXrav/jIXXVm0=\n-----END PRIVATE KEY-----\n";
	
	
	function create_custom_token($uid, $is_premium_account) {
	  global $service_account_email, $private_key;
	
	  $now_seconds = time();
	  $payload = array(
	    "iss" => $service_account_email,
	    "sub" => $service_account_email,
	    "aud" => "https://identitytoolkit.googleapis.com/google.identity.identitytoolkit.v1.IdentityToolkit",
	    "iat" => $now_seconds,
	    "exp" => $now_seconds+(60*60),  // Maximum expiration time is one hour
	    "uid" => $uid,
	    'claims' => [
       		 'uid' => $uid,
   		 ],
	  );
	  return JWT::encode($payload, $private_key, "RS256");
	}
	
	
	
	
	if(!checkSecretMatched($_POST["restSecret"])){
		return;
	}
	
	$userId = htmlspecialchars($_POST["userId"]);
		
	$token = create_custom_token($userId, false);
	
	echo $token;
	return;
	
	
	
	
	
	
	
	
?>
