//
//  FirebaseListener.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 10/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation

protocol FirebaseListener{

    func onResult<T:Any>(_ object:T?, _ status:Status);

}
