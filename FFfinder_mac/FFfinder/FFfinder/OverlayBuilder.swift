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

    private var overlayType:OverlayType?
    private var title:String?
    private var message:String?
    private var onDismiss:(()->Void)?
    private var onChoices:[(()->Void)]?
    private var vc:UIViewController?
    private static var alertController: UIAlertController?
    private static var popupController: PopupDialog?

    
    public static func build() -> OverlayBuilder{
        return OverlayBuilder()
    }
    
    private init(){
 
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
    
    public func setVc(_ vc:UIViewController) -> OverlayBuilder{
        self.vc = vc
        return self
    }
    
    public func setOnDismiss(_ onDismiss:@escaping (()->Void)) -> OverlayBuilder{
        self.onDismiss = onDismiss
        return self
    }
    
    public func setOnChoices(_ choices:@escaping (()->Void)...) -> OverlayBuilder{
        self.onChoices = []
        for choice:(()->Void) in choices{
            self.onChoices?.append(choice)
        }
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

        OverlayBuilder.forceCloseAllOverlays()
        
        if self.overlayType == OverlayType.Loading{
            
            var msg:String? = self.message
            if msg == nil{
                msg = "loading".localized
            }
            
            OverlayBuilder.alertController = UIAlertController(title: nil, message: msg, preferredStyle: .alert)
            
            OverlayBuilder.alertController!.view.tintColor = UIColor.black
            let loadingIndicator: UIActivityIndicatorView = UIActivityIndicatorView(
                frame: CGRect(x:10, y:5, width:50, height:50)) as UIActivityIndicatorView
            loadingIndicator.hidesWhenStopped = true
            loadingIndicator.activityIndicatorViewStyle = UIActivityIndicatorViewStyle.gray
            loadingIndicator.startAnimating();
            
            OverlayBuilder.alertController!.view.addSubview(loadingIndicator)
            
            if let vc = self.vc{
                vc.present(OverlayBuilder.alertController!, animated: true, completion: nil)
            }
            else{
                if let topController = UIApplication.topViewController() {
                    topController.present(OverlayBuilder.alertController!, animated: true, completion: nil)
                }
            }
            
        }
        else{
            // Create the dialog
            let popup = PopupDialog(title: self.title, message: self.message,
                                    gestureDismissal: false, completion: {
                if self.onDismiss != nil{
                    self.onDismiss!()
                }
            })
            
            if self.overlayType == OverlayType.OkOnly{
                let buttonOk = DefaultButton(title: "ok".localized) {
                    self.getChoiceToRun(0)?()
                }
                popup.addButtons([buttonOk])
                
            }
            else if self.overlayType == OverlayType.OkOrCancel{
                let buttonOk = DefaultButton(title: "ok".localized) {
                    self.getChoiceToRun(0)?()
                }
                let buttonCancel = DefaultButton(title: "cancel".localized) {
                    self.getChoiceToRun(1)?()
                }
                popup.addButtons([buttonOk, buttonCancel])
                
            }
            
            
            
            // Present dialog
            
            if let vc = self.vc{
                vc.present(popup, animated: true, completion: nil)
            }
            else{
                if let topController = UIApplication.topViewController() {
                    topController.present(popup, animated: true, completion: nil)
                }
            }
            
            
           
            
            OverlayBuilder.popupController = popup
            
        }

        
    }
    
    public static func forceCloseAllOverlays(){
        OverlayBuilder.popupController?.dismiss(animated: false, completion: nil)
        OverlayBuilder.alertController?.dismiss(animated: false, completion: nil)
        
        OverlayBuilder.popupController = nil
        OverlayBuilder.alertController = nil
    }
    
    
    
    private func getChoiceToRun(_ index:Int) -> (()->Void)?{
        if let choices = self.onChoices{
            if choices.count - 1 >= index{
                return choices[index]
            }
        }
        return nil
    }
   
    
}
