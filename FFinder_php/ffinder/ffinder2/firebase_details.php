<?php
	
	
	if (strpos($_SERVER['SERVER_NAME'], 'localhost') !== false || 
			strpos($_SERVER['SERVER_NAME'], '192.168.0') !== false) // or any other host
	{
		//testing
	     $DEFAULT_URL = 'https://ffinder-74ebd.firebaseio.com/';
	     $DEFAULT_TOKEN = "x6V3xPSPlmZa9ySjbLEzbg3McXRwlUQ6opOEe3fI";
	}
	
	else
	{
		 $DEFAULT_URL = 'https://ffinder-74ebd.firebaseio.com/';
	     $DEFAULT_TOKEN = "x6V3xPSPlmZa9ySjbLEzbg3McXRwlUQ6opOEe3fI";
	}
	
?>
