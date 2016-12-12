//
//  TapToAddTableViewCell.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 10/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class TapToAddTableViewCell:UITableViewCell{

    @IBOutlet weak var label: LocalizedLabel!
    private var friendTableProtocol:FriendTableViewProtocol?

    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        self.label.isUserInteractionEnabled = true
        
        self.label?.addGestureRecognizer(UITapGestureRecognizer(target: self,
                                                                    action: #selector(onLabelTapped)))
    }
    
    override var layoutMargins: UIEdgeInsets {
        get { return UIEdgeInsets.zero }
        set(newVal) {}
    }
    
    func update(_ friendTableProtocol:FriendTableViewProtocol){
        self.friendTableProtocol = friendTableProtocol
    }
    
    func onLabelTapped(){
        self.friendTableProtocol!.onRequestShareKey()
    }
    

    
}
