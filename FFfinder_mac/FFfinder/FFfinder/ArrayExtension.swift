//
//  ArrayExtension.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
extension Array where Element: Equatable {
    
    // Remove first collection element that is equal to the given `object`:
    mutating func remove(_ object: Element) {
        if let index = index(of: object) {
            remove(at: index)
        }
    }
    
    mutating func removeIfExceedLimit(_ limit: Int){
        while self.count > limit{
            self.remove(at: 0)
        }
    }
    
}
