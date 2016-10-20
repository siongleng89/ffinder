//
//  FirebaseModelProtocol.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 12/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import FirebaseDatabase

protocol FirebaseModelProtocol{

    func toAnyObject() -> [AnyHashable:Any]

    func fromAnyObject(_ value:Any?)
}
