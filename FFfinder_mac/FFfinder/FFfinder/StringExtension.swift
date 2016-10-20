//
//  StringExtension.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 11/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation

extension String {
    var localized: String {
        return NSLocalizedString(self, tableName: nil, bundle: Bundle.main, value: "", comment: "")
    }
}
