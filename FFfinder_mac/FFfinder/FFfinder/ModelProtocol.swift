//
//  ModelProtocol.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 10/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation

extension NSObject{
    convenience init(jsonStr:String) {
        self.init()
        
        if let jsonData = jsonStr.data(using: String.Encoding.utf8, allowLossyConversion: false)
        {
            do {
                let json = try JSONSerialization.jsonObject(with: jsonData, options: []) as! [String: AnyObject]
                
                // Loop
                for (key, value) in json {
                    let keyName = key as String
                    let keyValue: String = value as! String
                    
                    // If property exists
                    if (self.responds(to: NSSelectorFromString(keyName))) {
                        self.setValue(keyValue, forKey: keyName)
                    }
                }
                
            } catch let error as NSError {
                print(error.localizedDescription)
            }
        }
        else
        {
        }
    }
    
    func toDictionary() -> Dictionary<String, Any>{
        let mirror = Mirror(reflecting: self)
        
        var result:Dictionary<String, Any> = Dictionary<String, Any>();
        
        for (labelMaybe, valueMaybe) in mirror.children {
            result[labelMaybe!] = valueMaybe;
        }
        
        return result;
    }
    
    
    
}
