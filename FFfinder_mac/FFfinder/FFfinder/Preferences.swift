//
//  Preferences.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 11/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class Preferences{

    
    public static func put(_ type:PreferenceType, _ value:String){
        put(type.rawValue, value)
    }

    
    public static func put(_ key:String, _ value:String){
        let preferences = UserDefaults.standard
        preferences.set(value, forKey: key)
        preferences.synchronize()
    }
    
    public static func get(_ type:PreferenceType) -> String?{
        return get(type.rawValue);
    }
    
    public static func get(_ key:String) -> String?{
        let preferences = UserDefaults.standard
        if preferences.object(forKey: key) == nil {
            return nil
        } else {
            return preferences.string(forKey: key)
        }
    }

    
    public static func delete(_ type:PreferenceType) -> Void{
        delete(type.rawValue)
    }
    
    public static func delete(_ key:String) -> Void{
        let preferences = UserDefaults.standard
        preferences.removeObject(forKey: key)
    }
    
    public static func deleteAll(){
        if let bundle = Bundle.main.bundleIdentifier {
            UserDefaults.standard.removePersistentDomain(forName: bundle)
        }
    }
    
    
}
