//
//  LocationUpdateTask.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 7/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class LocationUpdateTask{

    private var senderId:String?
    private var senderToken:String?
    private var platform:String?
    private var backgroundUpdateTask: UIBackgroundTaskIdentifier!
    private var locationUpdater:LocationUpdater!
    
    init(_ senderId:String?, _ senderToken:String?, _ platform:String?){
        self.senderId = senderId
        self.senderToken = senderToken
        self.platform = platform
        
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
        Logs.show("Starting background location task")
        Threadings.runInBackground {
            self.locationUpdater = LocationUpdater(self.senderId,
                                                   self.senderToken, self.platform, {
                Logs.show("Ending background location task")
                
                if self.locationUpdater != nil{
                     self.locationUpdater.dispose()
                }
                                                    
               
                self.endBackgroundUpdateTask()
                                                    
            })
        }
        
    }
    

}
