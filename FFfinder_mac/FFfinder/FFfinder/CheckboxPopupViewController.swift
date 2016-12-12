//
//  CheckboxPopupViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 9/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import DLRadioButton
class CheckboxPopupViewController:UIViewController{

    var msg:String!
    var customTitleMsg:String!
    var checkboxTitle:String!
    
    @IBOutlet weak var labelMessage: UILabel!
    @IBOutlet weak var checkbox: DLRadioButton!
    @IBOutlet weak var labelTitle: UILabel!
    
    override func viewDidLoad() {
        labelMessage.text = self.msg
        checkbox.setTitle(checkboxTitle, for: .normal)
        labelTitle.text = customTitleMsg
        
        self.checkbox?.addGestureRecognizer(UITapGestureRecognizer(target: self,
                                                                       action: #selector(onCheckboxTapped)))
    }
    
    public func onCheckboxTapped(){
        if (self.checkbox?.isSelected)!{
            self.checkbox.isSelected = false
        }
        else{
            self.checkbox.isSelected = true
        }
    }
    
    public func setCustomTitle(_ text:String){
        self.customTitleMsg = text
    }
    
    public func setMessage(_ text:String){
        self.msg = text
    }
    
    public func setCustomCheckboxTitle(_ text:String){
        self.checkboxTitle = text
    }
    
    
    
}
