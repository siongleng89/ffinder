//
//  Threadings.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 15/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class Threadings{

    public static func delay(_ delayMiliSeconds:Int, _ toRun:@escaping ()-> Void){
        DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(delayMiliSeconds), execute: {
            toRun()
        })
    }
    
    public static func runInBackground(_ toRun:@escaping ()-> Void){
        DispatchQueue.global(qos: .background).async {
            toRun()
        }
    }
    
    public static func postMainThread(_ toRun:@escaping ()-> Void){
        DispatchQueue.main.async {
            toRun()
        }
    }
    
    public static func sleep(_ sleepMiliSecs:UInt32){
        usleep(sleepMiliSecs)
    }
    
    
}
