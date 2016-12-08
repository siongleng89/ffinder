//
//  FCMMessageType.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
enum FCMMessageType:String{
    
    case UpdateLocation
    case UserLocated
    case IsAliveMsg
    case FriendsAdded
    case Nothing
    
    
    
    
    public static func convertStringToFCMMessageType(_ input:String) -> FCMMessageType{
        if let result:FCMMessageType = FCMMessageType(rawValue: input){
            return result
        }
        else{
            return .Nothing
        }
    }
  
}
