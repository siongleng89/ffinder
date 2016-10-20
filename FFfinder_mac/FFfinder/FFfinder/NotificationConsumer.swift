//
//  NotificationConsumer.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class NotitificationConsumer{
    
    private var locationUpdater:LocationUpdater?
    private static var handledMsgIds = [String]()
    
    public func consume(_ dict:[AnyHashable:Any], _ callback:(()->Void)? = nil){
        
        if let actionString = dict["action"] {
            let action = FCMMessageType.convertStringToFCMMessageType(actionString as! String)
            
            if action == FCMMessageType.UpdateLocation{
                
                //make sure msg id is not duplicated
                if let msgId = dict["messageId"] as? String{
                    if NotitificationConsumer.handledMsgIds.contains(msgId){
                        if let callback = callback{
                            callback()
                        }
                        return
                    }
                    else{
                        NotitificationConsumer.handledMsgIds.append(msgId)
                        NotitificationConsumer.handledMsgIds.removeIfExceedLimit(5)
                    }
                }
                let senderId = dict["senderId"] as! String
                let senderToken = dict["senderToken"] as? String
                NSLog("handled msg size: \(NotitificationConsumer.handledMsgIds.count)")
                
                locationUpdater = LocationUpdater(senderId, senderToken){
                    self.locationUpdater?.dispose()
                    self.locationUpdater = nil
                    if let callback = callback{
                        callback()
                    }
                }
            }
            else if action == FCMMessageType.IsAliveMsg{
                let senderId = dict["senderId"] as! String
                let myModel:MyModel = MyModel()
                myModel.loadFriend(senderId)
                if let friendModel = myModel.getFriendModelById(senderId){
                    friendModel.searchStatus = SearchStatus.WaitingUserLocation
                    friendModel.save()
                    
                    self.notificateReloadFriend(senderId)
                    if callback != nil{
                        callback!()
                    }
                }
            }
            else if action == FCMMessageType.UserLocated{
                let senderId = dict["senderId"] as! String
                let latitude = dict["latitude"] as! String
                let longitude = dict["longitude"] as! String
                //let isAutoNotification = dict["isAutoNotification"] as! String
                let myModel:MyModel = MyModel()
                myModel.loadFriend(senderId)
                if let friendModel = myModel.getFriendModelById(senderId){
                    let locationModel:LocationModel = LocationModel()
                    locationModel.latitude = latitude
                    locationModel.longitude = longitude
                    locationModel.timestampLastUpdated = "\(DateTimeUtils.getCurrentUnixSecs())"
                    friendModel.searchResult = SearchResult.Normal
                    friendModel.searchStatus = SearchStatus.End
                    
                    locationModel.geodecodeCoordinatesIfNeeded {
                        friendModel.locationModel = locationModel
                        friendModel.save()
                        self.notificateReloadFriend(senderId)
                    
                        //only show notification on user system tray if it is from auto notification
                        //todo
                    }
                    
                }
            }
        
        }
    }
    
    private func notificateReloadFriend(_ reloadId:String){
        var dict = [String:String]()
        dict["reloadFriendId"] = reloadId
        
        NotificationCenter.default.post(name: .needToReloadFriendModel, object: nil,
                                        userInfo: dict)
    }
    

}

