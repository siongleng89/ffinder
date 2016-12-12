//
//  WaitAutoNotificationPopupViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 9/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class WaitAutoNotificationPopupViewController:UIViewController{
    
        
    @IBOutlet weak var buttonSearchAgain: FFTextButton!
    @IBOutlet weak var btnWaitAutoNotification: FFTextButton!
    @IBOutlet weak var labelReason2: LocalizedLabel!
    var onSearchAgainChosen:(()->Void)?
    var onWaitAutoNotificationChosen:(()->Void)?
    
    override func viewDidLoad() {
        buttonSearchAgain.titleLabel?.textAlignment = .center
        btnWaitAutoNotification.titleLabel?.textAlignment = .center
        
        var text = labelReason2.text
        let textRange = NSMakeRange(0, (text?.characters.count)!)
        let attributedText = NSMutableAttributedString(string: text!)
        attributedText.addAttribute(NSUnderlineStyleAttributeName , value: NSUnderlineStyle.styleSingle.rawValue, range: textRange)
        // Add other attributes if needed
        labelReason2.attributedText = attributedText
    }
    
    @IBAction func onWaitAutoNotificationTapped(_ sender: AnyObject) {
        onWaitAutoNotificationChosen!()
    }
    
    @IBAction func onSeachAgainTapped(_ sender: AnyObject) {
         onSearchAgainChosen!()
    }
}
