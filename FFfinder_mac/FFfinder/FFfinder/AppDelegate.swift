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
import GoogleMaps
import GooglePlaces

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        
        GMSServices.provideAPIKey(Constants.GoogleMapApiKey)
        GMSPlacesClient.provideAPIKey(Constants.GoogleMapApiKey)
        
        setupGoogleAnalytics()
        
        
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
    
    
    func setupGoogleAnalytics() {
        
        // pod 'Google/Analytics'
        //        let configureError:NSError?
        //        GGLContext.sharedInstance().configureWithError(&configureError)
        //        assert(configureError == nil, "Error configuring Google services: \(configureError)")
        //
        //        let gai = GAI.sharedInstance()
        //        gai.trackUncaughtExceptions = true  // report uncaught exceptions
        //        gai.logger.logLevel = GAILogLevel.Verbose  // remove before app release
        
        
        // pod 'GoogleAnalytics'
        let tracker = GAI.sharedInstance().tracker(withTrackingId: Constants.AnalyticsTrackingID)
        GAI.sharedInstance().trackUncaughtExceptions = true;
        
        // default dispatchInterval is 120
        
        #if DEBUG
            
            GAI.sharedInstance().dispatchInterval = 1
            GAI.sharedInstance().logger.logLevel = .verbose //.Verbose
            
            // disable sending data to Google.
            // very useful option for debugging.
            GAI.sharedInstance().dryRun = true
            
        #endif
        
        
        // check defaultTacker property
        let firstTracker = GAI.sharedInstance().defaultTracker
        
        if (tracker?.isEqual(firstTracker))! {
            #if DEBUG
                
                print("same Tracker")
                
            #endif
        }
    }
    
    func openMain(_ addingKey:String){
        // Register routes that're handled
//        
//        let storyboard = UIStoryboard(name: "Main", bundle: nil)
//        let root = self.window?.rootViewController as! UINavigationController
//        
//        let profileVC: LaunchViewController = storyboard.instantiateViewController(withIdentifier: "LaunchViewController") as! LaunchViewController
       Vars.pendingUserKey = addingKey
//        root.pushViewController(profileVC, animated: true)
        
        
    }
    
    func application(_ application: UIApplication,
                     continue userActivity: NSUserActivity,
                     restorationHandler: @escaping ([Any]?) -> Void) -> Bool {
        
        // 1
        guard userActivity.activityType == NSUserActivityTypeBrowsingWeb,
            let url = userActivity.webpageURL,
            let components = URLComponents(url: url, resolvingAgainstBaseURL: true) else {
                return false
        }
        
        // 2
        var compo = components.path.components(separatedBy: "/")
        if compo.count >= 3{
            self.openMain(compo[2])
            return true
        }
        
        
        // 3
        let webpageUrl = URL(string: "https://ff-finder.com")!
        application.openURL(webpageUrl)
        
        return false
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
         GAI.sharedInstance().dispatch()
    }
    // [END disconnect_from_fcm]
    
    func applicationWillTerminate(_ application: UIApplication) {
         GAI.sharedInstance().dispatch()
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        
        #if DEBUG
             FIRInstanceID.instanceID().setAPNSToken(deviceToken as Data, type: FIRInstanceIDAPNSTokenType.unknown)
        #else
            //Tricky line
            FIRInstanceID.instanceID().setAPNSToken(deviceToken as Data, type: FIRInstanceIDAPNSTokenType.prod)
        #endif
        
        
        
        
        
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
