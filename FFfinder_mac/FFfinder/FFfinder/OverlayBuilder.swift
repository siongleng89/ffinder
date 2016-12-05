//
//  OverlayBuilder.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 29/11/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import PopupDialog

class OverlayBuilder{

    private var vc:UIViewController?
    private var overlayType:OverlayType?
    private var title:String?
    private var message:String?
    private var onDismiss:(()->Void)?
    
    public static func build(_ vc:UIViewController) -> OverlayBuilder{
        return OverlayBuilder(vc)
    }
    
    private init(_ vc:UIViewController){
        self.vc = vc
    }
    
    public func setOverlayType(_ overlayType:OverlayType) -> OverlayBuilder{
        self.overlayType = overlayType
        return self
    }
    
    public func setTitle(_ title:String) -> OverlayBuilder{
        self.title = title
        return self
    }
    
    public func setMessage(_ message:String) -> OverlayBuilder{
        self.message = message
        return self
    }
    
    public func setOnDismiss(_ onDismiss:@escaping (()->Void)) -> OverlayBuilder{
        self.onDismiss = onDismiss
        return self
    }

    
    public func show(){
        
        
//        // Create a custom view controller
//        let ratingVC = DialogLayoutViewController(nibName: "DialogLayout", bundle: nil)
//
//        // Create the dialog
//        let popup = PopupDialog(viewController: ratingVC, buttonAlignment: .horizontal, transitionStyle: .bounceDown, gestureDismissal: true)
//
//        // Create first button
//        let buttonOne = CancelButton(title: "CANCEL") {
//        }
//
//        // Create second button
//        let buttonTwo = DefaultButton(title: "RATE") {
//        }
//
//        // Add buttons to dialog
//        popup.addButtons([buttonOne, buttonTwo])
//
//        // Present dialog
//        present(popup, animated: true, completion: nil)


        // Create the dialog
        let popup = PopupDialog(title: title, message: message, completion: {
            if self.onDismiss != nil{
                self.onDismiss!()
            }
        })

        if overlayType == OverlayType.OkOnly{
            let buttonOk = DefaultButton(title: "ok".localized) {
                
            }
            popup.addButtons([buttonOk])
            
        }
        
        // Present dialog
        vc?.present(popup, animated: true, completion: nil)
        
    
    }
    
}
