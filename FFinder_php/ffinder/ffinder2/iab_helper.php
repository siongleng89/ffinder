<?php


	function getPurchaseDetails($productId, $productToken, $count, $firebase){
            
            if(empty($productId) || empty($productToken) || $count >= 3) return -1;

            $accessToken = json_decode($firebase->get('secret/accessToken/token'));
           
            if(empty($accessToken)){
                    $accessToken = refreshAccessToken();
                    $firebase->set('secret/accessToken/token', $accessToken);
            }


            $url  = "https://www.googleapis.com/androidpublisher/v2/applications/com.ffinder.android/purchases/subscriptions/".
                                      $productId."/tokens/".
                                      $productToken."/?access_token=".$accessToken;

            
            
            $ch = curl_init();

            curl_setopt($ch, CURLOPT_URL, $url);
            curl_setopt($ch, CURLOPT_HEADER, 0);
            curl_setopt($ch, CURLOPT_POST, 0);
            curl_setopt($ch, CURLOPT_TIMEOUT, 10);
            curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 10);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
            curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "GET");
            $output = curl_exec($ch);
            curl_close($ch);

            $decoded = json_decode($output);
         
            
            if(isset($decoded->error->code)){
                    if($decoded->error->code == 401){
                            $accessToken = refreshAccessToken();
                            $firebase->set('secret/accessToken/token', $accessToken);
                            return getPurchaseDetails($productId, $productToken, $count + 1, $firebase);
                    }
                    else{
                            return -1;
                    }	
            }
            else{
                    return $decoded;
            }	
	}
	
	function refreshAccessToken(){
            $url  = "https://accounts.google.com/o/oauth2/token";

            $post = [
                        'grant_type' => 'refresh_token',
                        'client_id' => '679786452242-9feomrts3ve89iteil4kqvi8q1opaul4.apps.googleusercontent.com',
                        'client_secret'   => 'ldsWipMYQY2AfZZ_tgRNcrlj',
                        'refresh_token' => '1/XiiFW7iZS2_Jov7CPQNpyyPmxSKtbNuXYaxp_Q0uQJLzDbhsC2mCgY1OVu9bzf_z'
                    ];

            $ch = curl_init($url);
            curl_setopt($ch, CURLOPT_POST, 1);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($ch, CURLOPT_TIMEOUT, 10);
            curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 10);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
            curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
            curl_setopt($ch, CURLOPT_POSTFIELDS, $post);

            // execute!
            $output = curl_exec($ch);

            // close the connection, release resources used
            curl_close($ch);

            $decoded = json_decode($output);
            
            $accessToken = $decoded->access_token;
                
            return $accessToken;
	}
	
	
?>
