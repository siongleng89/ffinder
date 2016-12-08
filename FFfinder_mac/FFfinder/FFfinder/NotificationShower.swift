//
//  NotificationShower.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 8/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class NotificationShower{

    public static func show(_ title:String, _ body:String){
    
        let notification = UILocalNotification()
        notification.fireDate = NSDate(timeIntervalSinceNow: 1) as Date
        notification.alertTitle = title
        notification.alertBody = body
        notification.soundName = UILocalNotificationDefaultSoundName
        notification.userInfo = ["title": title, "body" : body]
        UIApplication.shared.scheduleLocalNotification(notification)
    }

    public static func showNotificationAlert(_ title:String, _ body:String){
        OverlayBuilder
            .build().setTitle(title).setMessage(body)
            .setOverlayType(OverlayType.OkOnly)
            .show()
    }
    

}
