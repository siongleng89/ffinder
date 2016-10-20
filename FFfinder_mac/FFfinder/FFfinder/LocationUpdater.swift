//
//  LocationUpdater.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import CoreLocation

class LocationUpdater : NSObject, CLLocationManagerDelegate{

    var fromUserId:String?
    var fromUserToken:String?
    var locationManager:CLLocationManager?
    var myModel:MyModel
    var callback:(()->Void)?
    var locationRetrieved:Bool
    
    init(_ fromUserId:String? = nil, _ fromUserToken:String? = nil, _ callback:(()->Void)?){
        self.myModel = MyModel()
        self.myModel.load()
        
        self.locationRetrieved = false
        
        self.callback = callback
        self.fromUserId = fromUserId
        self.fromUserToken = fromUserToken
        
        super.init()
        
        self.runProcess()
    }
    
    
    
    private func runProcess(){
//        
        if let fromUserToken = self.fromUserToken{
            
            var dict = [String:String]()
            dict["latitude"] = "1.5611390552076"
            dict["longitude"] = "103.802937559952"
            //is auto notificaiton decide whether show push notification on user tray
            dict["isAutoNotification"] = "0"
            
            NotificationSender.sendWithToken(myModel.userId!, fromUserToken,
                                             FCMMessageType.UserLocated,
                                             NotificationSender.TTL_INSTANT,
                                             dict: dict, callback: callback);
            
            NSLog("sent location updated fcm")
        }
        else{
            NSLog("no token to send leh")
            if let callback = self.callback{
                callback()
            }
        }
//
//        
//        
        
        //if from usertoken not empty, means it is through normal search, reply alive msg
//        if let fromUserToken = self.fromUserToken{
//            replyAliveMsg(myModel.userId!, fromUserToken);
//        }
//        
//        self.locationManager = CLLocationManager()
//        
//        if let locationManager = self.locationManager{
//            locationManager.delegate = self
//            locationManager.desiredAccuracy = kCLLocationAccuracyBest
//            locationManager.requestAlwaysAuthorization()
//            
//            if #available(iOS 9.0, *) {
//                locationManager.allowsBackgroundLocationUpdates = true
//                locationManager.pausesLocationUpdatesAutomatically = false;
//            } else {
//                // Fallback on earlier versions
//            }
//            
//            if LocationUpdater.isLocationServiceEnabled() {
//                NSLog("starting update location")
//                locationManager.startUpdatingLocation()
//            }
//            else{
//                if let callback = callback{
//                    callback()
//                }
//            }
//
//        }
        
       
    }
    
    func replyAliveMsg(_ myUserId:String, _ fromUserToken:String){
        NotificationSender.sendWithToken(myUserId, fromUserToken,
                                         FCMMessageType.IsAliveMsg, NotificationSender.TTL_INSTANT);
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print(error.localizedDescription)
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        NSLog("locationManager didUpdateLocations fired")
        
        if locationRetrieved == false{
            //last location is the most recent one
            if let location = locations.last{
                let howRecent = location.timestamp.timeIntervalSinceNow
                
                //less than 30 secs, still quite recent and useable
                if abs(howRecent) < 30 {
                    locationRetrieved = true
                    locationManager?.stopUpdatingLocation()
                    
                    let latitude:String = "\(location.coordinate.latitude)"
                    let longitude:String = "\(location.coordinate.longitude)"
                    locationSuccessfullyRetrieved(latitude, longitude)
                    NSLog("locationManager successfully retrieved location")
                }
                else{
                    NSLog("locationManager last location is too old...")
                }
            }
            else{
                NSLog("locationManager no last location")
            }
        }
        else{
            NSLog("locationManager location already retrieved successfully")
        }
        
    }

    func locationSuccessfullyRetrieved(_ latitude:String, _ longitude:String){
        let locationModel:LocationModel = LocationModel()
        locationModel.latitude = latitude
        locationModel.longitude = longitude
        
        //immediately reply fcm with current location, then save to firebase later,
        //to avoid firebase login delay
        if let fromUserToken = self.fromUserToken{
            
            var dict = [String:String]()
            dict["latitude"] = latitude
            dict["longitude"] = longitude
            //is auto notificaiton decide whether show push notification on user tray
            dict["isAutoNotification"] = "0"
            
            NotificationSender.sendWithToken(myModel.userId!, fromUserToken,
                                             FCMMessageType.UserLocated,
                                             NotificationSender.TTL_INSTANT,
                                             dict: dict, callback: callback);
            
             NSLog("sent location updated fcm")
        }
        else{
            NSLog("no token to send leh")
            if let callback = self.callback{
                callback()
            }
        }
        
//        myModel.loginFirebase(0,
//            {(success) in
//                FirebaseDB.updateLocation(self.myModel.userId!, locationModel,
//                    {(status) in
//                        if status == Status.Success{
//                            //to do, auto notification part
//                        }
//                })
//        })
        
        
        
    }
    
    public func dispose(){
        if let locationManager = locationManager{
            locationManager.stopUpdatingLocation()
        }
        NSLog("disposing locationUpdater")
    }
    

    public static func isLocationServiceEnabled() -> Bool{
        var enabled:Bool = false
        
        if CLLocationManager.locationServicesEnabled() {
            switch(CLLocationManager.authorizationStatus()) {
            case .authorizedAlways:
                enabled = true
                break
            default:
                break
            }
        }
        return enabled
        
    }
    
    
}













