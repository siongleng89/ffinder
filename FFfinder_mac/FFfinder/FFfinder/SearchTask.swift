//
//  SearchTask.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 30/11/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import Firebase

class SearchTask{

    private var friendModel:FriendModel
    private var myModel:MyModel
    private var myToken:String
    public var finish:Bool
    
    init(_ friendModel:FriendModel, _ myModel:MyModel, _ myToken:String){
        self.friendModel = friendModel
        self.myModel = myModel
        self.myToken = myToken
        self.finish = false
        
        self.run()
    }
    
    
    private func run(){
        Threadings.runInBackground {
            
            FIRMessaging.messaging().unsubscribe(fromTopic: "/topics/\(self.friendModel.userId!)")
            self.reset()
            self.setStatus(SearchStatus.CheckingData)
            
            self.monitorTimeoutPhase()
            
            self.checkHasLink({
                (hasLink) in
                if self.finish == true{
                    return
                }
                
                if hasLink{
                    
                    self.checkMeIsBlocked({
                        (isBlocked) in
                        if self.finish == true{
                            return
                        }
                        
                        
                        if !isBlocked{
                            self.setStatus(SearchStatus.WaitingUserRespond)
                            NotificationSender.sendWithUserId(self.myModel.userId!,
                                                              self.friendModel.userId!,
                                          FCMMessageType.UpdateLocation,
                                          Int(Constants.SearchTimeoutSecs) / 2,
                                          StringsHelper.generateRandomKey(30),
                                          dict: ["senderToken": self.myToken]);
                            
                            
                            Threadings.runInBackground {
                                Threadings.sleep(
                                    (UInt32(Constants.SearchTimeoutSecs) / 2) * 1000 + 500
                                    )
                                
                                if (self.finish){
                                    return
                                }
                                
                                NotificationSender.sendWithUserId(self.myModel.userId!,
                                                          self.friendModel.userId!,
                                                          FCMMessageType.UpdateLocation,
                                                          Int(Constants.SearchTimeoutSecs),
                                                          StringsHelper.generateRandomKey(30),
                                                          dict: ["senderToken": self.myToken]);
                                
                            }
                            
                        }
                        else{
                            //set a small delay to prevent jumpy animation bug
                            Threadings.delay(1000, {
                                self.setResult(SearchResult.ErrorUserBlocked)
                            })
                        
                        }
                    
                    })
                    
                    
                }
                else{
                    //set a small delay to prevent jumpy animation bug
                    Threadings.delay(1000, {
                        self.setResult(SearchResult.ErrorNoLink)
                    })
                
                }
            
            })
            
            
            var totalSleepPeriod:UInt32 = 0
            let sleepDuration:UInt32 = 500
            while !self.finish{
                Threadings.sleep(sleepDuration)
                totalSleepPeriod += sleepDuration
                
                if totalSleepPeriod > (UInt32(Constants.SearchTimeoutSecs) * 1000){
                    self.taskTimeout()
                    self.finish = true
                }
            }
            
            
        }
    }
    
    
    private func checkHasLink(_ callback:@escaping (Bool)->Void){
        FirebaseDB.checkLinkExist(self.myModel.userId!, friendModel.userId!, {
            (result, status) in
                callback(result)
        })
    }
    
    private func checkMeIsBlocked(_ callback:@escaping (Bool)->Void){
        FirebaseDB.checkMeIsBlocked(self.myModel.userId!, friendModel.userId!, {
            (result, status) in
            callback(result)
        })
    
    }
    
    private func monitorTimeoutPhase(){
        Threadings.runInBackground {
            let sleepDuration:Double = 1000
            var totalSleepDuration:Double = 0
            var phase = 0
            while (!self.finish){
                if totalSleepDuration >
                            Double(Constants.SearchTimeoutSecs) * 1000 * 0.33 && phase == 0{
                    phase = 1
                    self.setTimeoutPhase(1)
                }
                else if totalSleepDuration >
                    Double(Constants.SearchTimeoutSecs) * 1000 * 0.66 && phase == 1{
                    phase = 2
                    self.setTimeoutPhase(2)
                }
                
                Threadings.sleep(UInt32(sleepDuration))
                totalSleepDuration += sleepDuration
            }
        }
    
    }
    
 
    
    private func taskTimeout(){
        Logs.show("Search timeout")
        
        self.myModel.loadFriend(self.friendModel.userId!)
        
        //update task search status with friend model one, as friend model always hold latest status
        if let friendModel:FriendModel = myModel.getFriendModelById(self.friendModel.userId!){
            var shouldAutoNotify = false
            var searchTarget = "Me"
            if friendModel.userId != self.myModel.userId{
                searchTarget = "Others"
            }
            
            if friendModel.searchStatus == SearchStatus.WaitingUserLocation{
                self.setResult(SearchResult.ErrorTimeoutUnknownReason)
                Analytics.trackEvent(AnalyticEvent.Search_Timeout,
                                "\(SearchStatus.WaitingUserLocation.rawValue)_\(searchTarget)")
                shouldAutoNotify = true
            }
            else if friendModel.searchStatus == SearchStatus.WaitingUserRespond{
                self.setResult(SearchResult.ErrorTimeoutUnknownReason)
                Analytics.trackEvent(AnalyticEvent.Search_Timeout,
                                     "\(SearchStatus.WaitingUserRespond.rawValue)_\(searchTarget)")
                shouldAutoNotify = true
            }
            else{
                self.setResult(SearchResult.ErrorTimeoutNoConnection)
                shouldAutoNotify = false
            }
            
            if shouldAutoNotify{
                FIRMessaging.messaging().subscribe(toTopic: "/topics/\(self.friendModel.userId!)")
                NotificationSender.sendWithUserId(self.myModel.userId!, self.friendModel.userId!,
                                                  FCMMessageType.UpdateLocation,
                                                  NotificationSender.TTL_LONG,
                                                  StringsHelper.generateRandomKey(30),
                                                  dict: ["senderToken": self.myToken]);
            }

            }
        
    
    }

    
    private func reset(){
        self.friendModel.timeoutPhase = 0
        self.friendModel.searchResult = SearchResult.Normal
        self.friendModel.hideErrorMsg = false
        self.friendModel.save()
    }
    
    private func setTimeoutPhase(_ phase:Int){
        self.friendModel.timeoutPhase = phase
        self.friendModel.notificateChanged()
    }
    
    private func setStatus(_ searchStatus:SearchStatus){
        Logs.show("New search status \(searchStatus)")
        self.friendModel.searchStatus = searchStatus
        self.friendModel.save()
        self.friendModel.notificateChanged()
    }
    
    private func setResult(_ searchResult:SearchResult){
        Logs.show("New search result \(searchResult)")
        self.finish = true
        self.friendModel.searchStatus = SearchStatus.End
        self.friendModel.searchResult = searchResult
        self.friendModel.recentlyFinished = true
        self.friendModel.save()
        self.friendModel.notificateChanged()
        
        Analytics.trackEvent(AnalyticEvent.Search_Result, searchResult.rawValue)
    }
    
    public func searchCompleted(){
        self.myModel.loadFriend(self.friendModel.userId!)
        if let friendModel:FriendModel = myModel.getFriendModelById(self.friendModel.userId!){
            friendModel.recentlyFinished = true
            friendModel.notificateChanged()
            
            if !(friendModel.searchResult?.isError())!{
                Analytics.trackEvent(AnalyticEvent.Search_Result, "SearchSuccess")
            }
            
        }
        self.finish = true
        
        
        
        
    }
    
    public func dispose(){
        finish = true
    }
    

}
