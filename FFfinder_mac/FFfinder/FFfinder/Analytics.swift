//
//  Analytics.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 13/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class Analytics{
    
    public static func setScreen(name: String){
        let tracker = GAI.sharedInstance().defaultTracker
        tracker?.set(kGAIScreenName, value: name)
        
        let builder = GAIDictionaryBuilder.createScreenView()
        
        let eventTracker: NSObject =  (builder?.build())!
        tracker?.send(eventTracker as! [NSObject : AnyObject])
        
        trackEvent(AnalyticEvent.ChangeScreen, name)
    }
    
    public static func trackEvent(_ event: AnalyticEvent) {
        trackEvent(action: event.rawValue, label: "")
    }
    
    public static func trackEvent(_ event: AnalyticEvent, _ label:String) {
        trackEvent(action: event.rawValue, label: label)
    }
    
    public static func trackEvent(action: String, label: String) {
        let tracker = GAI.sharedInstance().defaultTracker
        let trackDictionary = GAIDictionaryBuilder.createEvent(withCategory: "FFinder", action: action, label: label, value: 0)
        
        let eventTracker: NSObject =  (trackDictionary?.build())!
        tracker?.send(eventTracker as! [NSObject : AnyObject])
        
    }
    
}
