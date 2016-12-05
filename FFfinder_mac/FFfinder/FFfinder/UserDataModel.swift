//
//  UserDataModel.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 12/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation

// this class is for user important data, token etc 
// not for user apps settings!
class UserDataModel : FirebaseModelProtocol{
    
    var fcmToken:String?
    var platform:String?
    
    func toAnyObject() -> [AnyHashable : Any] {
        var result = [AnyHashable : Any]();
        result["token"] = fcmToken
        result["platform"] = platform
        return result
    }
    
    func fromAnyObject(_ value: Any?) {
        if let result = value as? [String:Any]{
            fcmToken = result["token"] as! String?
            platform = result["platform"] as! String?
        }
    }
    
    
}
