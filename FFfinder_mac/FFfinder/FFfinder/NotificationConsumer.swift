//
//  NotificationConsumer.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import Firebase
class NotitificationConsumer{
    
    private var locationUpdateTask:LocationUpdateTask?
    private var locationReceiverTask:LocationReceiverTask?
    private static var handledMsgIds = [String]()
    
    public func consume(_ dict:[AnyHashable:Any]){
        
        if let actionString = dict["action"] {
            let action = FCMMessageType.convertStringToFCMMessageType(actionString as! String)
            
            
            //my friend has added me, refresh my friend list
            if action == FCMMessageType.FriendsAdded{
                let senderId = dict["senderId"] as! String
                let username = dict["username"] as! String
                
                let myModel:MyModel = MyModel.shared
                if !myModel.checkFriendExist(senderId){
                    let friendModel:FriendModel = FriendModel()
                    friendModel.username = username
                    friendModel.userId = senderId
                    friendModel.save()
                    
                    myModel.addFriendModel(friendModel)
                    myModel.sortFriendModels()
                    myModel.commitFriendUserIds()
                    
                    broadcastReloadWholeFriendsList()
                    
                    //todo show notification
                    NotificationShower.show("app_name".localized,
                                            "notification_x_added_you".localized.format(username))
                }
                
            
            }
            //request me to update my location
            else if action == FCMMessageType.UpdateLocation{
                //make sure msg id is not duplicated
                if let msgId = dict["messageId"] as? String{
                    if NotitificationConsumer.handledMsgIds.contains(msgId){
                        return
                    }
                    else{
                        NotitificationConsumer.handledMsgIds.append(msgId)
                        NotitificationConsumer.handledMsgIds.removeIfExceedLimit(5)
                    }
                }
                let senderId = dict["senderId"] as! String
                let senderToken = dict["senderToken"] as? String
                let fromPlatform = dict["fromPlatform"] as? String
                
                locationUpdateTask = LocationUpdateTask(senderId, senderToken!, fromPlatform!)
            }
            else if action == FCMMessageType.IsAliveMsg{
                let senderId = dict["senderId"] as! String
                let myModel:MyModel = MyModel(dontLoadFriends:true)
                var locationDisabled:Bool = false
                if let isDisabled = dict["locationDisabled"] as? String{
                    if isDisabled == "1"{
                        locationDisabled = true
                    }
                }
                
                myModel.loadFriend(senderId)
                if let friendModel = myModel.getFriendModelById(senderId){
                    
                    if friendModel.searchStatus != SearchStatus.WaitingUserRespond{
                        return
                    }
                    
                    if(locationDisabled){
                        friendModel.searchStatus = SearchStatus.End
                        friendModel.searchResult = SearchResult.ErrorLocationDisabled
                    }
                    else{
                        friendModel.searchStatus = SearchStatus.WaitingUserLocation
                    }
                    
                    
                    friendModel.save()
                    
                    self.broadcastReloadFriend(senderId)
                }
            }
            else if action == FCMMessageType.UserLocated{
                let senderId = dict["senderId"] as! String
                let latitude = dict["latitude"] as! String
                let longitude = dict["longitude"] as! String
                var auto:Bool = false
                if let isAutoNotification = dict["isAutoNotification"] as? String{
                    if isAutoNotification == "1"{
                        auto = true
                    }
                }
                
                locationReceiverTask = LocationReceiverTask(senderId, latitude, longitude, auto)
            }
        
        }
    }
    
    private func broadcastReloadFriend(_ reloadId:String){
        var dict = [String:String]()
        dict["reloadFriendId"] = reloadId
        
        NotificationCenter.default.post(name: .needToReloadFriendModel, object: nil,
                                        userInfo: dict)
    }
    
    private func broadcastReloadWholeFriendsList(){
        NotificationCenter.default.post(name: .needToReloadWholeFriendsList, object: nil)
    }
    

}

