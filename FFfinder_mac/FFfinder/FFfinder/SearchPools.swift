//
//  SearchPools.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 30/11/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import FirebaseInstanceID

class SearchPools{

    private static var pool:[String: SearchTask] = [String: SearchTask]()
    
    public static func newTask(_ friendModel:FriendModel, _ myModel:MyModel){
        
        if let value:SearchTask = pool[friendModel.userId!]{
            //task already exist
            if !value.finish{
                //task not yet finish, let it finish
                return
            }
        }
        
        if let token = FIRInstanceID.instanceID().token(){
            Analytics.trackEvent(AnalyticEvent.Search_Deduct_Credit)
            pool[friendModel.userId!] = SearchTask(friendModel, myModel, token)
        }
        
    
    }
    
    
    public static func taskFinish(_ friendModel:FriendModel){
        if let value:SearchTask = pool[friendModel.userId!]{
            value.searchCompleted()
            pool[friendModel.userId!] = nil
        }
    }
    

}
