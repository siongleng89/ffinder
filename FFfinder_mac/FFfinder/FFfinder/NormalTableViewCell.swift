//
//  NormalTableViewCell.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 28/11/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
import DLRadioButton

class NormalTableViewCell: UITableViewCell {

    @IBOutlet weak var labelText: UILabel!
    @IBOutlet weak var radioButton: DLRadioButton!
    
    
    override func awakeFromNib() {
        let bgColorView = UIView()
        bgColorView.backgroundColor = UIColor.colorPrimaryDark()
        self.selectedBackgroundView = bgColorView
    }
    
    override var layoutMargins: UIEdgeInsets {
        get { return UIEdgeInsets.zero }
        set(newVal) {}
    }

    
    func setItemText(_ text:String){
        labelText.text = text
    }

    func enableRadioButton(){
        radioButton.isHidden = false
    }
    
    func changeRadioButtonState(_ selected:Bool){
        radioButton.isSelected = selected
    }
    
    
    
}
