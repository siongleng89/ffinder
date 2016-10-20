//
//  KeyModel.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 15/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import FirebaseDatabase

public class KeyModel : FirebaseModelProtocol{

    public var userId:String?
    public var userName:String?
    public var insertAt:UInt64?
    
    
    func fromAnyObject(_ value: Any?) {
        if let result = value as? [String:Any]{
            userId = result["userId"] as! String?
            userName = result["userName"] as! String?
            if let val = result["insertAt"] as? NSNumber {
                insertAt = UInt64(val.int64Value)
            }
        }
    }
    
    func toAnyObject() -> [AnyHashable : Any] {
        var result = [String:Any]()
        result["userId"] = userId
        result["userName"] = userName
        result["insertAt"] = FIRServerValue.timestamp()
        
        return result
    }
    
}
