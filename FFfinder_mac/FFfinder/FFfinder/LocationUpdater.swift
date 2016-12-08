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
    var fromPlatform:String?
    var locationManager:CLLocationManager!
    var myModel:MyModel
    var callback:(()->Void)?
    var finish:Bool
    var locationSharingEnabled:Bool?
    
    init(_ fromUserId:String? = nil, _ fromUserToken:String? = nil, _ fromPlatform:String? = nil,
         _ callback:(()->Void)? = nil){
        self.myModel = MyModel(dontLoadFriends:true)
        self.finish = false
        
        self.callback = callback
        self.fromUserId = fromUserId
        self.fromUserToken = fromUserToken
        self.fromPlatform = fromPlatform
        
        super.init()
        
        self.runProcess()
    }
    
    
    
    private func runProcess(){
        
        
        locationSharingEnabled = LocationUpdater.isLocationServiceEnabled()
   
        if self.fromUserToken != nil{
            replyAliveMsg(myModel.userId!)
        }

        
        

        if locationSharingEnabled!{
            
            
            Threadings.postMainThread {
                self.locationManager = CLLocationManager()
                self.locationManager.delegate = self

                self.locationManager.desiredAccuracy = kCLLocationAccuracyBest
                self.locationManager.requestAlwaysAuthorization()
                
                if #available(iOS 9.0, *) {
                    self.locationManager.allowsBackgroundLocationUpdates = true
                    self.locationManager.pausesLocationUpdatesAutomatically = false;
                } else {
                    // Fallback on earlier versions
                }
                
                Logs.show("Start location manager update location")
                self.locationManager.startUpdatingLocation()
            }
            
          
        }
        else{
            if let callback = callback{
                callback()
            }
        }
    }
    
    
    func replyAliveMsg(_ myUserId:String){
        var locationDisabled = "0"
        if !locationSharingEnabled!{
            locationDisabled = "1"
        }
        
        NotificationSender.sendWithToken(myUserId, self.fromUserToken!,
                                         FCMMessageType.IsAliveMsg, NotificationSender.TTL_INSTANT, "", self.fromPlatform!,
                                         dict: ["locationDisabled": locationDisabled])
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Logs.show(error.localizedDescription)
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation])
    {
        Logs.show("locationManager didUpdateLocations fired")
        
        //last location is the most recent one
        if let location = locations.last{
            let howRecent = location.timestamp.timeIntervalSinceNow
            
            //less than 30 secs, still quite recent and useable
            if abs(howRecent) < 30 {
                locationManager.stopUpdatingLocation()
                
                let latitude:String = "\(location.coordinate.latitude)"
                let longitude:String = "\(location.coordinate.longitude)"
                locationSuccessfullyRetrieved(latitude, longitude)
            }
            else{
            }
        }
        else{
            Logs.show("locationManager no last location")
        }

        
    }

    func locationSuccessfullyRetrieved(_ latitude:String, _ longitude:String){
        if finish{
            return
        }
        
        finish = true
        Logs.show("Updating my location to \(latitude) \(longitude)")
        
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
                                             NotificationSender.TTL_INSTANT, "",
                                             self.fromPlatform!, retryIfError: true,
                                             dict: dict, callback: callback)
            
        }
        
        
        //send to auto notification subscriber
        let current = NSDate().timeIntervalSince1970

        //five seconds window, make sure dont keep sending
        if let lastLastUpdatedSecs = Preferences.get(PreferenceType.LastSentAutoNotification){
            if(StringsHelper.isNumeric(lastLastUpdatedSecs)){
                if(current - Double(lastLastUpdatedSecs)! <  5){
                    return;
                }
            }

        }
        
        var dict = [String:String]()
        dict["latitude"] = latitude
        dict["longitude"] = longitude
        //is auto notificaiton decide whether show push notification on user tray
        dict["isAutoNotification"] = "1"
        //send to those waiting auto notificaiton my userid topic subscribers
        NotificationSender.sendToTopic(myModel.userId!, myModel.userId!,
                                       FCMMessageType.UserLocated,
                                       NotificationSender.TTL_LONG, "", dict: dict)
        
        Logs.show("Send to topic")
        Preferences.put(PreferenceType.LastSentAutoNotification, String(current))
        
        if let callback = self.callback{
            callback()
        }
        
    }
    
    public func dispose(){
        finish = true
        
        if let locationManager = locationManager{
            locationManager.stopUpdatingLocation()
        }
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













