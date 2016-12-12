//
//  SegueExtensions.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 9/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class PushNoAnimationSegue: UIStoryboardSegue {
    
    override func perform() {
        let source1 = source as UIViewController
        if let navigation = source1.navigationController {
            navigation.pushViewController(destination as UIViewController, animated: false)
        }
    }
    
}
