//
//  FFTextButton.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 15/11/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
class FFIconButton : UIButton {
    
    var iconColorNormal:UIColor?
    var bgColorNormal:UIColor?
    var iconColorOnTapped:UIColor?
    var bgColorOnTapped:UIColor?
    var imagePath:String?
    var selectedImagePath:String?
    var isBtnSelected:Bool? = false

    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    
    override func awakeFromNib() {
        iconColorNormal = UIColor.colorPrimaryDark()
        bgColorNormal = UIColor.colorContrast()
        
        iconColorOnTapped = UIColor.colorContrast()
        bgColorOnTapped = UIColor.colorPrimaryDark()
        
        layer.cornerRadius = 6.0
        layer.borderColor = iconColorNormal?.cgColor
        layer.borderWidth = 1
        contentEdgeInsets = UIEdgeInsets(top: 5, left: 5, bottom: 5, right: 5)
        clipsToBounds = true
        self.imageView?.contentMode = UIViewContentMode.scaleAspectFit
        self.isUserInteractionEnabled = true
        
        if self.isEnabled{
            colorUp()
        }
        else{
            colorDisabled()
        }
        
    }
    
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        if !isBtnSelected! && self.isEnabled{
            colorDown()
        }
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        if !isBtnSelected! && self.isEnabled{
            colorUp()
        }
    }
    
    override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
        if !isBtnSelected! && self.isEnabled{
            colorUp()
        }
    }
    
    func colorUp(){
        layer.borderColor = iconColorNormal?.cgColor
        layer.backgroundColor = bgColorNormal?.cgColor
        
        let image = UIImage(named: imagePath!)?.withRenderingMode(.alwaysTemplate)
        self.setImage(image, for: .normal)
        self.tintColor = iconColorNormal
    }
    
    func colorDown(){
        layer.borderColor = iconColorNormal?.cgColor
        layer.backgroundColor = bgColorOnTapped?.cgColor
        
        if self.isBtnSelected! && self.selectedImagePath != nil{
            let image = UIImage(named: selectedImagePath!)?.withRenderingMode(.alwaysTemplate)
            self.setImage(image, for: .normal)
        }
        else{
            let image = UIImage(named: imagePath!)?.withRenderingMode(.alwaysTemplate)
            self.setImage(image, for: .normal)
        }
        
        self.tintColor = iconColorOnTapped
    }
    
    func colorDisabled(){
        layer.borderColor = UIColor.colorLightGray().cgColor
        layer.backgroundColor = UIColor.colorLightGray().cgColor
        
        let image = UIImage(named: imagePath!)?.withRenderingMode(.alwaysTemplate)
        self.setImage(image, for: .disabled)
        self.setImage(image, for: .normal)
        self.tintColor = UIColor.colorContrast()
    }
    
    public func setIsBtnSelected(_ selected:Bool){
        
        if (!isEnabled){
            return
        }
        
        self.isBtnSelected = selected
        
        if isBtnSelected!{
            colorDown()
        }
        else{
            colorUp()
        }
    }
    
    override var isEnabled: Bool {
        didSet {
            if self.isEnabled{
                colorUp()
            }
            else{
                colorDisabled()
            }
        }
    }
 
    @IBInspectable var imageName : String? {
        set (newValue) {
            self.imagePath = newValue
        }
        get {
            return self.imagePath
        }
    }
    
    
    @IBInspectable var selectedImageName : String? {
        set (newValue) {
            self.selectedImagePath = newValue
        }
        get {
            return self.selectedImagePath
        }
    }
    
}
