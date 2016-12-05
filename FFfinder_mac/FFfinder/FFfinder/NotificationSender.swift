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
    
    public static func sendWithUserId(_ myUserId:String, _ targetUserId:String, _ fcmMessageType:FCMMessageType, _ ttl:Int, _ msgId:String, retryIfError:Bool? = false,
                                      dict: [String:String]? = nil, callback:(()->Void)? = nil){
        FirebaseDB.getUserData(targetUserId,
                                {(userDataModel, status) in
                                    if status == Status.Success && userDataModel != nil{
                                        sendFcm(myUserId, (userDataModel?.fcmToken)!, fcmMessageType, ttl, msgId, (userDataModel?.platform)!,
                                                retryIfError: retryIfError, dict: dict,
                                                callback: callback)
                                    }
        })
    }

    public static func sendWithToken(_ myUserId:String, _ targetToken:String, _ fcmMessageType:FCMMessageType, _ ttl:Int, _ msgId:String, _ toPlatform:String, retryIfError:Bool? = false,
                                     dict: [String:String]? = nil, callback:(()->Void)? = nil ){
        sendFcm(myUserId, targetToken, fcmMessageType, ttl, msgId, toPlatform,
                    retryIfError: retryIfError, dict: dict, callback: callback)
    }
    
    public static func sendToTopic(_ myUserId:String, _ topicId:String,
                                   _ fcmMessageType:FCMMessageType, _ ttl:Int, _ msgId:String,
                                     dict: [String:String]? = nil, callback:(()->Void)? = nil ){
        sendFcm(myUserId, "/topics/\(topicId)", fcmMessageType, ttl, msgId, "android",
                dict: dict, callback: callback)
    }
    
    
    
    private static func sendFcm(_ myUserId:String, _ targetToken:String, _ fcmMessageType:FCMMessageType, _ ttl:Int, _ msgId:String, _ toPlatform:String,
                                retryIfError:Bool? = false, count:Int? = 0,
                                 dict: [String:String]? = nil, callback:(()->Void)? = nil){
        
        let myUrl = URL(string: "https://fcm.googleapis.com/fcm/send")
        var request = URLRequest(url:myUrl!)
        request.httpMethod = "POST"
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("key=" + Constants.FcmKey, forHTTPHeaderField: "Authorization")

        
        var dataMap = [String:String]()
        dataMap["action"] = fcmMessageType.rawValue
        dataMap["senderId"] = myUserId
        dataMap["fromPlatform"] = "ios"
        dataMap["messageId"] = msgId
        
        if let dict = dict {
            for (key, value) in dict{
                dataMap[key] = value
            }
        }
        
        var postMap = [String:Any]()
        postMap["data"] = dataMap
        
        postMap["to"] = targetToken
        postMap["priority"] = "high"
        postMap["delay_while_idle"] = false
        postMap["time_to_live"] = ttl
        postMap["content_available"] = true
        
        
        if toPlatform.lowercased() == "ios"{
            var notificationMap = [String:String]()
            notificationMap["badge"] = "0"
            notificationMap["alert"] = ""
            notificationMap["sound"] = ""
            postMap["notification"] = notificationMap
        }
        
        
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
