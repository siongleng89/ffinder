//
//  SinglePageViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 10/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class SinglePageViewController:UIViewController{

    @IBOutlet weak var label: UILabel!
    @IBOutlet weak var image: UIImageView!
    var labelText:String?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        label.text = labelText
    }
    
    
}
