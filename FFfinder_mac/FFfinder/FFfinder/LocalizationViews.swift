//
//  LocalizationViews.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 8/11/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
class LocalizedLabel : UILabel {
    override func awakeFromNib() {
        if let text = text {
            self.text = NSLocalizedString(text, tableName: nil, bundle: Bundle.main, value: "", comment: "")
        }
    }
}
