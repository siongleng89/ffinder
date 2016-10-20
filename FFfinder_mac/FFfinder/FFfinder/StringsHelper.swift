//
//  StringsHelper.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 15/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class StringsHelper{

    
    
    public static func generateRandomKey(_ length:Int) -> String{
        let base = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_"
        var randomString: String = ""
        
        for _ in 0..<length {
            let randomValue = arc4random_uniform(UInt32(base.characters.count))
            randomString += "\(base[base.index(base.startIndex, offsetBy: Int(randomValue))])"
        }
        return randomString
    }
    
    private static func generateRandomNumber(_ length:Int) -> String{
        let base = "0123456789"
        var randomString: String = ""
        
        for _ in 0..<length {
            let randomValue = arc4random_uniform(UInt32(base.characters.count))
            randomString += "\(base[base.index(base.startIndex, offsetBy: Int(randomValue))])"
        }
        return randomString
    }
 
    public static func generateUserKey() -> String{
        return generateRandomNumber(4) + "-" + generateRandomNumber(4) + "-" + generateRandomNumber(4) 
    }
 
}
