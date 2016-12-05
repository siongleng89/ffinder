//
//  StringsHelper.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 15/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class StringsHelper{

    public static func isEmpty(_ input:String?) -> Bool{
        
        if let input = input{
            if input.trimmingCharacters(in: CharacterSet.whitespacesAndNewlines) != ""{
                return false
            }
        }
        
        return true;
    
    }
    
    public static func safeSubstring(_ input:String?, _ length:Int) -> String{
        if let input = input{
            if input.characters.count <= length{
                return input
            }
            else{
                return input[0...(length-1)]
            }
        }
        return ""
    }
    
    public static func isNumeric(_ input:String?) -> Bool{
        let badCharacters = NSCharacterSet.decimalDigits.inverted
        
        if !(isEmpty(input)) && input?.rangeOfCharacter(from: badCharacters) == nil {
            return true
        } else {
            return false
        }
    }
    
    
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
