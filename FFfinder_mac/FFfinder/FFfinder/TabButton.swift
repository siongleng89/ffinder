//
//  TabButton.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 10/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class TabButton:UIView{

     var view: UIView!
    
    @IBOutlet weak var bgImage: UIImageView!
    @IBOutlet weak var label: UILabel!
    @IBOutlet weak var imageViewIcon: UIImageView!
    func xibSetup() {
        view = loadViewFromNib()
        
        // use bounds not frame or it'll be offset
        view.frame = bounds
        
        // Make the view stretch with containing view
        view.autoresizingMask = [UIViewAutoresizing.flexibleWidth, UIViewAutoresizing.flexibleHeight]
        // Adding custom subview on top of our view (over any custom drawing > see note below)
        addSubview(view)
       
    }
    
    
    func loadViewFromNib() -> UIView {
        
        let bundle = Bundle(for: type(of: self))
        let nib = UINib(nibName: "TabButton", bundle: bundle)
        let view = nib.instantiate(withOwner: self, options: nil)[0] as! UIView
        
        return view
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
    
    func selectedChanged(){
        if let selected = self.selected{
            if selected{
                bgImage.image = bgImage.image!.withRenderingMode(.alwaysTemplate)
                bgImage.tintColor = UIColor.black
            }
            else{
                bgImage.image = bgImage.image!.withRenderingMode(.alwaysOriginal)
            }
        }
    }
    
    
    
    
    @IBInspectable var text:String?{
        didSet{
            self.label.text = text?.localized
        }
    }
    
    @IBInspectable var iconImage:UIImage?{
        didSet{
            self.imageViewIcon.image = iconImage
        }
    }
    
    @IBInspectable var selected:Bool?{
        didSet{
            self.selectedChanged()
        }
    }
    
    
}
