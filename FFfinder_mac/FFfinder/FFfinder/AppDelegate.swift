//
//  AppDelegate.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 3/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//
import UIKit
import UserNotifications

import Firebase
import FirebaseInstanceID
import FirebaseMessaging

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        
        
        
        // [START register_for_notifications]
        if #available(iOS 10.0, *) {
            let authOptions : UNAuthorizationOptions = [.alert, .badge, .sound]
            UNUserNotificationCenter.current().requestAuthorization(
                options: authOptions,
                completionHandler: {_,_ in })
            
            // For iOS 10 display notification (sent via APNS)
            UNUserNotificationCenter.current().delegate = self
            // For iOS 10 data message (sent via FCM)
            FIRMessaging.messaging().remoteMessageDelegate = self
            
        } else {
            let settings: UIUserNotificationSettings =
                UIUserNotificationSettings(types: [.alert, .badge, .sound], categories: nil)
            application.registerUserNotificationSettings(settings)
        }
        
        application.registerForRemoteNotifications()
        
        // [END register_for_notifications]
        
        FIRApp.configure()
        
        // Add observer for InstanceID token refresh callback.
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(self.tokenRefreshNotification),
                                               name: .firInstanceIDTokenRefresh,
                                               object: nil)

        return true
    }
    
    
    func application(_ application: UIApplication, didReceive notification: UILocalNotification) {
        
        if let title = notification.userInfo?["title"]{
            if let body = notification.userInfo?["body"]{
                NotificationShower.showNotificationAlert(title as! String, body as! String)
            }
        }
        
        Logs.show("receive local notification.")
    }
    
    // [START receive_message]
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any],
                     fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        // If you are receiving a notification message while your app is in the background,
        // this callback will not be fired till the user taps on the notification launching the application.
        // TODO: Handle data of notification
        Logs.show("receive FCM.")
        
        NotitificationConsumer().consume(userInfo)
        completionHandler(.newData)
        

    }
    // [END receive_message]
    
    // [START refresh_token]
    func tokenRefreshNotification(_ notification: Notification) {
        if let refreshedToken = FIRInstanceID.instanceID().token() {
            Logs.show("InstanceID token: \(refreshedToken)")
            
            let myModel:MyModel = MyModel();
            myModel.load()
            if let userId = myModel.userId {
                myModel.loginFirebase(0,
                    {(success) in
                        FirebaseDB.updateUserToken(userId, refreshedToken, nil)
                    }
                )
            }
            
        }
        
        // Connect to FCM since connection may have failed when attempted before having a token.
        connectToFcm()
    }
    // [END refresh_token]
    
    // [START connect_to_fcm]
    func connectToFcm() {
        FIRMessaging.messaging().connect { (error) in
            if (error != nil) {
                Logs.show("Unable to connect with FCM. \(error)")
            } else {
                Logs.show("Connected to FCM.")
            }
        }
    }
    // [END connect_to_fcm]
    
    func applicationDidBecomeActive(_ application: UIApplication) {
        connectToFcm()
    }
    
    // [START disconnect_from_fcm]
    func applicationDidEnterBackground(_ application: UIApplication) {
        FIRMessaging.messaging().disconnect()
        Logs.show("Disconnected from FCM.")
    }
    // [END disconnect_from_fcm]
    
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        //Tricky line
        FIRInstanceID.instanceID().setAPNSToken(deviceToken as Data, type: FIRInstanceIDAPNSTokenType.unknown)
        
        var token: String = ""
        for i in 0..<deviceToken.count {
            token += String(format: "%02.2hhx", deviceToken[i] as CVarArg)
        }
        
        Logs.show("device token:\(token)")
        
    }
    
    
}

// [START ios_10_message_handling]
@available(iOS 10, *)
extension AppDelegate : UNUserNotificationCenterDelegate {
    
    // Receive displayed notifications for iOS 10 devices.
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        
        if let title = notification.request.content.userInfo["title"]{
            if let body = notification.request.content.userInfo["body"]{
                NotificationShower.showNotificationAlert(title as! String, body as! String)
            }
        }
        
        
        
        Logs.show("receive notification FCM ios 10.")
    }
}

extension AppDelegate : FIRMessagingDelegate {
    // Receive data message on iOS 10 devices.
    func applicationReceivedRemoteMessage(_ remoteMessage: FIRMessagingRemoteMessage) {
        Logs.show("receive data FCM ios 10.")
        NotitificationConsumer().consume(remoteMessage.appData)
    }
}

// [END ios_10_message_handling]
