//
//  TextboxPopupViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 9/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class TextboxPopupViewController:UIViewController{
    
    var popupTitle:String!
    var textFieldDefaultText:String!
    
    @IBOutlet weak var labelTitle: UILabel!
    @IBOutlet weak var textField: UITextField!
    
    override func viewDidLoad() {
        labelTitle.text = self.popupTitle
        textField.text = textFieldDefaultText
    }
    
        
    
    
}
