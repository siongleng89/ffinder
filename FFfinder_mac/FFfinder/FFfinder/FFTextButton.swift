//
//  FFTextButton.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 15/11/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
class FFTextButton : UIButton {
    
    var colorNormal:UIColor?
    var colorOnTapped:UIColor?
    var currentColorStyle:String?
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        
    }
    
    
    override func awakeFromNib() {
        if let colorStyle = self.currentColorStyle{
            if colorStyle == "green"{
                colorNormal = UIColor.colorGreenTextButtonNormal()
                colorOnTapped = UIColor.colorGreenTextButtonTapped()
            }
        
        }
        
        layer.cornerRadius = 6.0
        clipsToBounds = true
        contentEdgeInsets = UIEdgeInsets(top: 15, left: 15, bottom: 15, right: 15)
        setTitleColor(UIColor.colorContrast(), for: .normal)
        setTitleColor(UIColor.colorContrast(), for: .highlighted)
        
        self.titleLabel?.font = UIFont.boldSystemFont(ofSize: 16)
        
        
        setBackgroundImage(UIImage(color: colorNormal!), for: .normal)
        
        if let title = self.title(for: .normal) {
            self.setTitle(title.localized, for: .normal)
        }
    }
    
    override var isHighlighted: Bool {
        get {
            return super.isHighlighted
        }
        set {
            if newValue {
                backgroundColor = colorOnTapped!
                setTitleColor(UIColor.colorContrast(), for: .normal)
            }
            else {
                backgroundColor = colorOnTapped!
                setTitleColor(UIColor.colorContrast(), for: .normal)
            }
            super.isHighlighted = newValue
        }
    }
    
    @IBInspectable var colorStyle : String? {
        set (newValue) {
            if newValue == "green"{
                self.currentColorStyle = newValue
            }
        }
        get {
            return self.currentColorStyle
        }
    }

}
