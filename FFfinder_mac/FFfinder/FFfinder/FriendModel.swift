//
//  FriendModel.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 10/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import EVReflection

class FriendModel : EVObject{

    var userId:String?
    var username:String?
    var locationModel:LocationModel?
    
    var timeoutPhase:Int?
    var searchStatus:SearchStatus?
    var searchResult:SearchResult?
    var recentlyFinished:Bool?

    
     //used to prevent double triggering of notificationCenter while loading
    var copying:Bool = false
    
    //selected properties will be excluded when converting to json
    override public func propertyMapping() -> [(String?, String?)] {
        return [("copying",nil), (nil,"copying"),
                ("timeoutPhase",nil), (nil,"timeoutPhase"),
                ("recentlyFinished",nil), (nil,"recentlyFinished")]
    }
    
    override func setValue(_ value: Any!, forUndefinedKey key: String) {
        switch key {
        case "searchStatus":
            if let rawValue = value as? String {
                if let enumValue =  SearchStatus(rawValue: rawValue) {
                    self.searchStatus = enumValue
                }
            }
        case "searchResult":
            if let rawValue = value as? String {
                if let enumValue =  SearchResult(rawValue: rawValue) {
                    self.searchResult = enumValue
                }
            }
        default:
            print("---> setValue for key '\(key)' should be handled.")
        }

    }
    
    
    public func load(){
        copying = true
        if let json = Preferences.get(self.userId!){
            let loadingModel:FriendModel = FriendModel(json:json)
            copyToThis(loadingModel)
        }
        copying = false
    }
    
    
    public func save(){
        let json = self.toJsonString()
        Preferences.put(self.userId!, json)
    }
    
    public func delete(){
        self.userId = nil
        Preferences.delete(self.userId!)
    }
    
    private func copyToThis(_ loadedFriendModel:FriendModel){
        self.userId = loadedFriendModel.userId
        self.username = loadedFriendModel.username
        self.locationModel = loadedFriendModel.locationModel
        self.searchStatus = loadedFriendModel.searchStatus
        self.searchResult = loadedFriendModel.searchResult
    }
    
    public func notificateChanged(){
        var dict = [String:FriendModel]();
        dict["friendModel"] = self
        
        // Register to receive notification
        NotificationCenter.default.post(name: .friendModelChanged, object: nil,
                                        userInfo: dict)

    }
    
}
