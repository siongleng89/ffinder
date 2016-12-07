//
//  FlowerType.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 6/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
public enum FlowerType{
    case Starting
    case AutoSearching
    case Confusing
    case Ending
    case HappySwinging
    case SearchSuccess
    case Sleeping
    case Troubling
    case Satisfied
    
    static let allValues = [Starting, AutoSearching, Confusing,
                            Ending, HappySwinging, SearchSuccess,
                            Sleeping, Troubling, Satisfied]
}
