//
//  Logs.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 6/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
public class Logs{
    
    public static func show(_ msg:String){
        print("\(NSDate().timeIntervalSince1970)-FFfinder log: \(msg)")
    }

}

