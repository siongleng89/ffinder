//
//  FirebaseDB.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 10/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import Firebase
import FirebaseDatabase


class FirebaseDB{

    static var database:FIRDatabaseReference!
    
    
    public static func getNewUserId() -> String{
        let db:FIRDatabaseReference = getTable(TableName.users);
        return db.childByAutoId().key;
    }
    
    public static func loginWithFireDbToken(_ token:String,
                                            _ callback:@escaping (Status) -> Void){
        FIRAuth.auth()?.signIn(withCustomToken: token ) { (user, error) in
            if error == nil && user != nil{
                callback(Status.Success)
            }
            else{
                callback(Status.Failed)
            }
        }
        
    }
    
    public static func getUserToken(_ userId:String, _ callback: @escaping (String?, Status) -> Void){
        getSingleData(getTable(TableName.users).child(userId).child("token"),
                      {(result, status) in
                        if let result = result, status == Status.Success {
                            callback("\(result)", Status.Success)
                        }
                        else{
                            callback(nil, status)
                        }
        });
    }
    
    public static func getUserData(_ userId:String, _ callback: @escaping (UserDataModel?, Status) -> Void){
        getSingleData(getTable(TableName.users).child(userId),
                      {(snapshot:Any?, status) in
                        if let snapshot = snapshot, status == Status.Success{
                            let dataModel = UserDataModel()
                            dataModel.fromAnyObject(snapshot)
                            callback(dataModel, Status.Success)
                        }
                        else{
                            callback(nil, Status.Failed)
                        }
        
        })
    
    }
    
    
    //must use identifier table since it is public,
    //because usually at this step user havent login firebase
    public static func checkUserIdExist(_ userId:String,
                                        _ callback:@escaping (Bool, Status) -> Void){
        let identifier = userId + "ios";
        checkExist(getTable(TableName.identifierToUserIdMaps).child(identifier), callback)
    }
    
    public static func saveToIdentifier(_ userId:String,
                                       _ callback:((Status) -> Void)?){
        let identifier = userId + "ios";
        var map = [String:String]()
        map[identifier] = userId;

        setValue(getTable(TableName.identifierToUserIdMaps), map, callback)
    }

    
    public static func updateUserToken(_ userId:String, _ token:String,
                                        _ callback:((Status) -> Void)?){
        var map = [String:String]()
        map["token"] = token;
        map["platform"] = "ios"
        setValue(getTable(TableName.users).child(userId), map, callback)
    }
    
    
    public static func tryInsertKey(_ userId:String, _ username:String, _ key:String,
                                    _ callback:@escaping (Bool, Status) -> Void){
        getSingleData(getTable(TableName.keys).child(key),
            {(snapshot:Any?, status) in
                let toInsertKeyModel:KeyModel = KeyModel()
                toInsertKeyModel.userId = userId
                toInsertKeyModel.userName = username
                
                //key doesnt exist in database, so this key is usable
                if snapshot == nil {
                    setValue(getTable(TableName.keys).child(key),
                             toInsertKeyModel.toAnyObject(),
                             {(status) in
                                callback(true, status)
                            })
                }
                //already exist, check timestamp
                else{
                    let currentKeyModel:KeyModel = KeyModel()
                    currentKeyModel.fromAnyObject(snapshot)
    
                    getCurrentTimestamp(userId,
                        {(timestamp:String?, status) in
                            if status == Status.Success && timestamp != nil{
                                
                                let timestampInMiliSecs:UInt64 = UInt64(timestamp!)!
                                let diffInMs = timestampInMiliSecs - currentKeyModel.insertAt!
                                let diffSecs = diffInMs / 1000
                                
                                //more than expired total secs
                                //change the key owner to me now
                                if diffSecs > Constants.KeyExpiredTotalSecs{
                                    setValue(getTable(TableName.keys).child(key), toInsertKeyModel.toAnyObject(),
                                             {(status) in
                                                callback(status == Status.Success, Status.Success)
                                             })
                                }
                                else{
                                    callback(false, Status.Success)
                                }
                                
                                
                            
                            }
                            
                            else{
                                callback(false, Status.Success)
                            }
                    })
                    
                    
                }
                
            }
        )
    
    }
    
    
    public static func addNewLink(_ myUserId:String, _ targetUserId:String, _ myName:String, _ targetName:String, _ callback: @escaping (Status) -> Void){
        
        var map1 = [String:String]()
        map1[targetUserId] = targetName
        
        var map2 = [String:String]()
        map2[myUserId] = myName
        
        let db:FIRDatabaseReference = getTable(TableName.links)
        
        setValue(db.child(myUserId), map1, nil)
        setValue(db.child(targetUserId), map2, callback)
    }
    
    public static func checkUserHasAnyLink(_ myUserId:String,
                                           _ callback: @escaping (Bool, Status) -> Void){
        checkExist(getTable(TableName.links).child(myUserId), callback)
    }
    
    
    public static func checkKeyExist(_ userID:String, _ userKey:String,
                                     _ callback:@escaping (KeyModel?, Status) -> Void){
        getSingleData(getTable(TableName.keys).child(userKey),
          {(object, status) in
            if status == Status.Success && object != nil{
                getCurrentTimestamp(userID,
                        {(timestamp:String?, status) in
        
                            if timestamp != nil && status == Status.Success{
                                let keyModel:KeyModel = KeyModel()
                                keyModel.fromAnyObject(object)
                               
                                let timestampInMiliSecs:UInt64 = UInt64(timestamp!)!
                                let diffInMs = timestampInMiliSecs - keyModel.insertAt!
                                let diffSecs = diffInMs / 1000
                                
                                //less than expired total secs
                                //can add user
                                if diffSecs < Constants.KeyExpiredTotalSecs{
                                     callback(keyModel, Status.Success)
                                }
                                else{
                                     callback(nil, Status.Failed)
                                }
                                
                                
                            }
                            else{
                                callback(nil, Status.Failed)
                            }
                })
            
            }
            else{
                callback(nil, Status.Failed)
            }
            
        
        })
    }
    
    public static func checkLinkExist(_ myUserId:String, _ targetUserId:String,
                                           _ callback: @escaping (Bool, Status) -> Void){
        checkExist(getTable(TableName.links).child(targetUserId).child(myUserId), callback)
    }
    
    public static func updateLocation(_ userId:String, _ locationModel:LocationModel,
                                      _ callback:@escaping (Status) -> Void){
        setValue(getTable(TableName.locations).child(userId), locationModel.toAnyObject(), callback)
    }
   
    
    public static func checkMeIsBlocked(_ myUserId:String, _ targetUserId:String,
                                      _ callback: @escaping (Bool, Status) -> Void){
        getSingleData(getTable(TableName.blockUsers).child(targetUserId).child(myUserId),
                      {(result, status) in
                        if let result = result, status == Status.Success {
                             callback("\(result)" == "1", Status.Success)
                        }
                        else{
                            callback(false, Status.Success)
                        }
        });
    }
    
    
    

    
    
    
    
    public static func getCurrentTimestamp(_ userId:String, _ callback:@escaping (String?, Status)-> Void){
        
        var map1 = [String:Any]()
        map1[userId] = FIRServerValue.timestamp()
        
        
        setValue(getTable(TableName.timestamps), map1,
                 {(status) in
                    if status == Status.Failed{
                        callback(nil, Status.Failed)
                    }
                    else{
                        getSingleData(getTable(TableName.timestamps).child(userId),
                                      {(value:Any?, status) in
                                        if value != nil && status == Status.Success{
                                            callback("\(value!)", Status.Success)
                                        
                                        }
                                        else{
                                            callback(nil, Status.Failed)
                                        }
                                      })
                    }
                })
    }

    private static func checkExist(_ reference:FIRDatabaseReference,
                                   _ callback:@escaping (Bool, Status) -> Void){
        reference.observeSingleEvent(of: .value,
                                    with: {(snapshot) -> Void in
                                        if snapshot.exists() {
                                            callback(true, Status.Success)
                                        }
                                        else{
                                            callback(false, Status.Success)
                                        }
            },
                                    withCancel: {(error) -> Void in
                                        callback(false, Status.Failed)
        })
        
    }
    
    private static func getSingleData(_ referece:FIRDatabaseReference, _ callback:@escaping         (Any?, Status) -> Void){
    
        referece.observeSingleEvent(of: .value,
                                    with: {(snapshot) -> Void in
                                        callback(snapshot.exists() ? snapshot.value : nil, Status.Success)
            },
                                    withCancel: {(error) -> Void in
                                        callback(nil, Status.Failed)
        })
    }
    
    private static func setValue(_ reference:FIRDatabaseReference, _ object:[AnyHashable:Any],
                                 _ callback:((Status) -> Void)?){
        reference.updateChildValues(object, withCompletionBlock: {(error, ref)
            in
                guard callback != nil else{
                    return;
                }
            
                if error != nil{
                    callback!(Status.Failed)
                }
                else{
                    callback!(Status.Success)
                }
            
            }
        )
    }
    
    private static func getData(_ referece:FIRDatabaseReference, _ callback:@escaping ([FIRDataSnapshot]?, Status) -> Void){
        referece.observeSingleEvent(of: .value,
                                        with: {(snapshot) -> Void in
                                                var results = [FIRDataSnapshot]();
                                                for childSnapshot in snapshot.children{
                                                    results.append(childSnapshot as! FIRDataSnapshot);
                                                }
                                                callback(results, Status.Success)
                                              },
                                    withCancel: {(error) -> Void in
                                                callback(nil, Status.Failed)
                                              }
        )
    
    }
    
    private static func getTable(_ tableName:TableName) -> FIRDatabaseReference{
        return getDatabase().child(tableName.rawValue);
    }
    
    private static func getDatabase() -> FIRDatabaseReference {
        if database === nil {
            database = FIRDatabase.database().reference();
        }
        return database;
    }
    
    
    enum TableName:String{
        case users
        case identifierToUserIdMaps
        case keys
        case timestamps
        case links
        case locations
        case blockUsers
    }
    
}


