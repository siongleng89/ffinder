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
            self.text = text.localized
        }
    }
    
    @IBInspectable var topInset: CGFloat = 0.0
    @IBInspectable var bottomInset: CGFloat = 0.0
    @IBInspectable var leftInset: CGFloat = 0.0
    @IBInspectable var rightInset: CGFloat = 0.0
    
    override func drawText(in rect: CGRect) {
        let insets = UIEdgeInsets(top: topInset, left: leftInset, bottom: bottomInset, right: rightInset)
        super.drawText(in: UIEdgeInsetsInsetRect(rect, insets))
    }
    
    override var intrinsicContentSize: CGSize {
        get {
            var contentSize = super.intrinsicContentSize
            contentSize.height += topInset + bottomInset
            contentSize.width += leftInset + rightInset
            return contentSize
        }
    }
    
}
