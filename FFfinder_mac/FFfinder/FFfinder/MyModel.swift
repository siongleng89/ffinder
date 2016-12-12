//
//  MyModel.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 11/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import EVReflection
import FirebaseAuth
class MyModel:EVObject{
    
    var userId:String?
    var userKey:String?
    var userKeyGeneratedUnixTime:String?        //in seconds
    var firebaseLogon:Bool = false
    var friendModels = [FriendModel]()
    
    
    static let shared = MyModel(true)
    
    private init(_ staticInit:Bool){
        super.init()
        load()
        loadAllFriendModels()
    }
    
    internal required init(){
        super.init()
    }
    
    init(dontLoadFriends:Bool){
        super.init()
        load()
        if !dontLoadFriends{
            loadAllFriendModels()
        }
    }
    
    
    //selected properties will be excluded when converting to json
    override public func propertyMapping() -> [(String?, String?)] {
        return [("friendModels",nil), (nil,"friendModels")]
    }
    
    public func resetUserKeyGeneratedTime(){
        userKeyGeneratedUnixTime = "\(DateTimeUtils.getCurrentUnixSecs())"
    }
    
    //count is for recursive counter, normally we try to login twice before declare as fail
    public func loginFirebase(_ count:Int, _ callback:((Bool) -> Void)?){
        if(firebaseLogon){
            if(callback != nil){
                callback!(true)
                return
            }
        }
        
        if (FIRAuth.auth()?.currentUser) != nil {
            firebaseLogon = true
            if(callback != nil){
                callback!(true)
            }
            return
        }
        
        let fireDbToken:String? = Preferences.get(PreferenceType.FireDbToken);
        
        if fireDbToken == nil{
            RestfulService.getToken(self.userId!,
                                    {(result, status) in
                                        if status == Status.Success && !((result?.isEmpty)!) {
                                            Preferences.put(PreferenceType.FireDbToken, result!)
                                            self.loginFirebaseWithToken(count, result!, callback)
                                        }
                                        //unable to retrieve token from restful
                                        else{
                                            if(callback != nil){
                                                callback!(false)
                                            }
                                        }
            })
        }
        else{
            loginFirebaseWithToken(count, fireDbToken!, callback)
        }
    }
    

    
    private func loginFirebaseWithToken(_ count:Int, _ token:String, _ callback:((Bool) -> Void)?){
        FirebaseDB.loginWithFireDbToken(token,
                                        {(status) in
                                            if status == Status.Success {
                                                self.firebaseLogon = true;
                                                if(callback != nil){
                                                    callback!(true)
                                                }
                                            }
                                            else{
                                                //token might be expired, re-get and login again
                                                if count == 0 {
                                                    Preferences.delete(PreferenceType.FireDbToken)
                                                    self.loginFirebase(count+1, callback)
                                                }
                                                else{
                                                    if(callback != nil){
                                                        callback!(false)
                                                    }
                                                }
                                            }
                                            
        })
    }

    
    public func load(){
        if let json = Preferences.get(PreferenceType.MyModelJson){
            let loadingModel:MyModel = MyModel(json:json)
            copyToThis(loadingModel)
            
            //ensure user doesnt change userId manually, security purpose
            let keychain = KeychainSwift()
            let keyChainStoredId = keychain.get(KeyChainType.UserId.rawValue)
            if keyChainStoredId != nil {
                if(self.userId != keyChainStoredId){
                    self.userId = keyChainStoredId;
                    save();
                }
            }
        }
    }
    
    public func save(){
        let json = self.toJsonString()
        Preferences.put(PreferenceType.MyModelJson, json)
    }
    
    public func delete(){
        self.userId = nil
        
        Preferences.deleteAll()
        
        do{
            try FIRAuth.auth()?.signOut()
        }
        catch{
        }
    }
    
    private func copyToThis(_ loadingModel:MyModel){
        self.userId = loadingModel.userId
        self.userKey = loadingModel.userKey
        self.userKeyGeneratedUnixTime = loadingModel.userKeyGeneratedUnixTime
    }
    
    
    //all about friends list 
    //----------------------------------------
    public func commitFriendUserIds(){
        var userIds = [String]()
        
        for friendModel in friendModels{
            if !userIds.contains(friendModel.userId!){
                userIds.append(friendModel.userId!)
            }
        }
    
        let result:String = userIds.joined(separator: ",")
        Preferences.put(PreferenceType.FriendUserIds, result)
    }
    
    public func loadAllFriendModels(){
        if let friendUserIdsString = Preferences.get(PreferenceType.FriendUserIds){
            var friendUserIds = [String]()
            friendUserIds = friendUserIdsString.components(separatedBy: ",")
            
            for userId in friendUserIds{
                if !StringsHelper.isEmpty(userId){
                    let friendModel:FriendModel = FriendModel()
                    friendModel.userId = userId
                    friendModel.load()
                    
                    if friendModel.searchStatus != SearchStatus.End{
                        friendModel.searchStatus = SearchStatus.End
                        friendModel.save()
                    }
                    addFriendModel(friendModel)
                }
            }
        }
    }
    
    public func loadFriend(_ friendId:String){
        if let friendUserIdsString = Preferences.get(PreferenceType.FriendUserIds){
            var friendUserIds = [String]()
            friendUserIds = friendUserIdsString.components(separatedBy: ",")
            
            if friendUserIds.contains(friendId){
                let friendModel:FriendModel = FriendModel()
                friendModel.userId = friendId
                friendModel.load()
                addFriendModel(friendModel)
            }
        }
    }
    
    public func addFriendModel(_ friendModel:FriendModel){
        if (!checkFriendExist(friendModel.userId!)){
            self.friendModels.append(friendModel)
        }
    }
    
    public func deleteFriend(_ toDeleteModel:FriendModel){
        if let toDeleteModel = getFriendModelById(toDeleteModel.userId!){
            self.friendModels.remove(toDeleteModel)
        }
    }
    
    public func getFriendModelById(_ friendUserId:String) -> FriendModel?{
        for friendModel in self.friendModels{
            if friendModel.userId! == friendUserId{
                return friendModel
            }
        }
        
        return nil
    }
 
    public func getNonSelfFriendModelsCount() -> Int{
        if(self.friendModels.count == 1 && getFriendModelById(self.userId!) != nil){
            return 0
        }
        else{
            return self.friendModels.count
        }
    }

    
    public func checkFriendExist(_ userId:String) -> Bool{
        return getFriendModelById(userId) != nil
    }
    
    
    public func sortFriendModels(){
        self.friendModels = self.friendModels.sorted { ($0.username?.lowercased())! < ($1.username?.lowercased())! }
    }
    
    
    
    
    
}
