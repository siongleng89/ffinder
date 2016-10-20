//
//  NotificationSender.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class NotificationSender{
    
    public static let TTL_LONG:Int = 1814399;
    public static let TTL_INSTANT:Int = 0;
    
    public static func sendWithUserId(_ myUserId:String, _ targetUserId:String, _ fcmMessageType:FCMMessageType, _ ttl:Int, dict: [String:String]? = nil, callback:(()->Void)? = nil){
        FirebaseDB.getUserToken(targetUserId,
                                {(token, status) in
                                    if status == Status.Success && token != nil{
                                        sendFcm(myUserId, token!, fcmMessageType, ttl, dict: dict, callback: callback)
                                    }
        })
    }

    public static func sendWithToken(_ myUserId:String, _ targetToken:String, _ fcmMessageType:FCMMessageType, _ ttl:Int, dict: [String:String]? = nil, callback:(()->Void)? = nil ){
        sendFcm(myUserId, targetToken, fcmMessageType, ttl, dict: dict, callback: callback)
    }
    
    
    private static func sendFcm(_ myUserId:String, _ targetToken:String, _ fcmMessageType:FCMMessageType, _ ttl:Int, dict: [String:String]? = nil, callback:(()->Void)? = nil){
        
        let myUrl = URL(string: "https://fcm.googleapis.com/fcm/send")
        var request = URLRequest(url:myUrl!)
        request.httpMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("key=" + Constants.FcmKey, forHTTPHeaderField: "Authorization")

        
        var dataMap = [String:String]()
        dataMap["action"] = fcmMessageType.rawValue
        dataMap["senderId"] = myUserId
        dataMap["fromPlatform"] = "Firebase"
        dataMap["messageId"] = StringsHelper.generateRandomKey(30)
        
        if let dict = dict {
            for (key, value) in dict{
                dataMap[key] = value
            }
        }
        
        
        var notificationMap = [String:String]()
        notificationMap["badge"] = "0"
        
        
        var postMap = [String:Any]()
        postMap["data"] = dataMap
        postMap["notification"] = notificationMap
        postMap["to"] = targetToken
        postMap["priority"] = "high"
        postMap["delay_while_idle"] = false
        postMap["time_to_live"] = ttl
        postMap["content_available"] = true
        
        do{
            let jsonData = try JSONSerialization.data(withJSONObject: postMap,
                                                      options: .prettyPrinted)
            
            request.httpBody = jsonData
            
            let task = URLSession.shared.dataTask(with: request) {
                data, response, error in
                if let callback = callback{
                    NSLog("done sending fcm")
                    callback()
                }
            }
            
            task.resume()
        }
        catch{
            print("Failed to post to fcm")
        }
        
    }
    
    
    
    
    
}
