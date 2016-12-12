//
//  CustomInfoWindow.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 10/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class CustomInfoWindow:UIView{

    func xibSetup() {
        
        // use bounds not frame or it'll be offset
        self.frame = bounds
        
        // Make the view stretch with containing view
        self.autoresizingMask = [UIViewAutoresizing.flexibleWidth, UIViewAutoresizing.flexibleHeight]
     
        
    }
    
    override init(frame: CGRect) {
        // 1. setup any properties here
        
        // 2. call super.init(frame:)
        super.init(frame: frame)
        
        // 3. Setup view from .xib file
        xibSetup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        // 1. setup any properties here
        
        // 2. call super.init(coder:)
        super.init(coder: aDecoder)
        
        // 3. Setup view from .xib file
        xibSetup()
    }
    
    
    override func awakeFromNib() {
        Logs.show("dsa")
    }
    
}
