//
//  LocationReceiverTask.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 13/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import Firebase

class LocationReceiverTask{
    
    private var senderId:String?
    private var latitude:String?
    private var longitude:String?
    private var isAutoNotification:Bool
    private var backgroundUpdateTask: UIBackgroundTaskIdentifier!
    private var locationUpdater:LocationUpdater!
    
    init(_ senderId:String?, _ latitude:String?, _ longitude:String?, _ isAutoNotification:Bool){
        self.senderId = senderId
        self.latitude = latitude
        self.longitude = longitude
        self.isAutoNotification = isAutoNotification
        
        beginBackgroundUpdateTask()
    }
    
    func beginBackgroundUpdateTask() {
        self.backgroundUpdateTask = UIApplication.shared.beginBackgroundTask(expirationHandler: {
            self.endBackgroundUpdateTask()
        })
        doBackgroundTask()
    }
    
    func endBackgroundUpdateTask() {
        UIApplication.shared.endBackgroundTask(self.backgroundUpdateTask)
        self.backgroundUpdateTask = UIBackgroundTaskInvalid
    }
    
    func doBackgroundTask() {
        Threadings.runInBackground {
            let myModel:MyModel = MyModel(dontLoadFriends:true)
            myModel.loadFriend(self.senderId!)
            if let friendModel = myModel.getFriendModelById(self.senderId!){
                
                let locationModel:LocationModel = LocationModel()
                locationModel.latitude = self.latitude
                locationModel.longitude = self.longitude
                locationModel.timestampLastUpdated = "\(DateTimeUtils.getCurrentUnixSecs())"
                friendModel.searchResult = SearchResult.Normal
                friendModel.searchStatus = SearchStatus.End
                
                locationModel.geodecodeCoordinatesIfNeeded {
                    friendModel.locationModel = locationModel
                    friendModel.save()
                    
                    Threadings.postMainThread {
                        
                        var dict = [String:String]()
                        dict["reloadFriendId"] = self.senderId!
                        
                        NotificationCenter.default.post(name: .needToReloadFriendModel, object: nil, userInfo: dict)
                        
                        
                        //only show notification on user system tray if it is from auto notification
                        if self.isAutoNotification{
                            Logs.show("Showing auto")
                            NotificationShower.show("notification_user_located_title".localized, "notification_x_has_been_located_msg".localized.format(friendModel.username!))
                            FIRMessaging.messaging().unsubscribe(fromTopic: "/topics/\(friendModel.userId!)")
                        }
                        
                        self.endBackgroundUpdateTask()
                        
                    }
                }
                
            }
        }
        
    }
    
    
}
