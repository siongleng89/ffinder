//
//  AutoHeightTextView.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 15/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
@IBDesignable
class AutoHeightTextView: UITextView {
    
    var heightConstraint: NSLayoutConstraint?
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }
    
    override init(frame frameRect: CGRect, textContainer aTextContainer: NSTextContainer!) {
        super.init(frame: frameRect, textContainer: aTextContainer)
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        let size = self.sizeThatFits(CGSize(width: self.bounds.size.width, height: CGFloat(FLT_MAX)))
        if self.heightConstraint == nil {
            self.heightConstraint = NSLayoutConstraint(item: self, attribute: .height, relatedBy: .equal, toItem: nil, attribute: NSLayoutAttribute(rawValue: 0)!, multiplier: 1.0, constant: size.height)
            self.addConstraint(self.heightConstraint!)
        }
        
        self.heightConstraint!.constant = size.height
        
        super.layoutSubviews()
        
    }
    
    
}
