//
//  ColorExtension.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 6/11/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
extension UIColor{
  

    
    static func colorPrimaryDark() -> UIColor{
        
        return UIColor(netHex:0x497462)
    }
    
    static func colorStatusBar() -> UIColor{
        return UIColor(netHex: 0x668e57)
    }
    
    static func colorGreenTextButtonNormal() -> UIColor{
        return UIColor(netHex: 0xabd489)
    }
    
    static func colorGreenTextButtonTapped() -> UIColor{
        return UIColor(netHex: 0x497462)
    }
    
    static func colorContrast() -> UIColor{
        
        return UIColor(netHex:0xffffff)
    }
    
    static func colorNormalText() -> UIColor{
        
        return UIColor(netHex:0x7b7a7a)
    }

    static func colorLightGray() -> UIColor{
        
        return UIColor(netHex:0xCCCCCC)
    }
    
    
    static func colorNavBar() -> UIColor{
        return UIColor(netHex: 0x648857)
    }

    convenience init(netHex:Int) {
        self.init(red:(netHex >> 16) & 0xff, green:(netHex >> 8) & 0xff, blue:netHex & 0xff)
    }
    
    convenience init(red: Int, green: Int, blue: Int) {
        assert(red >= 0 && red <= 255, "Invalid red component")
        assert(green >= 0 && green <= 255, "Invalid green component")
        assert(blue >= 0 && blue <= 255, "Invalid blue component")
        
        self.init(red: CGFloat(red) / 255.0, green: CGFloat(green) / 255.0, blue: CGFloat(blue) / 255.0, alpha: 1.0)
    }
    
}
