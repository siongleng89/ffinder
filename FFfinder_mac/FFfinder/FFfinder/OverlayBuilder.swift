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
    private var checkboxTitle:String?
    private var textFieldText:String?
    private var customViewController:UIViewController?
    
    private static var alertController: UIAlertController?
    private static var popupController: PopupDialog?
    private static var checkboxController: CheckboxPopupViewController?
    private static var textboxController: TextboxPopupViewController?
    
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
    
    public func setCheckboxTitle(_ checkboxTitle:String) -> OverlayBuilder{
        self.checkboxTitle = checkboxTitle
        return self
    }
    
    public func setTextFieldText(_ textFieldText:String) -> OverlayBuilder{
        self.textFieldText = textFieldText
        return self
    }
    
    public func setMessage(_ message:String) -> OverlayBuilder{
        self.message = message
        return self
    }
    
    
    public func setCustomVc(_ vc:UIViewController) -> OverlayBuilder{
        self.customViewController = vc
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
            
            var popup:PopupDialog!
            
            if let checkboxTitle = self.checkboxTitle{
                // Create a custom view controller
                let checkboxVc = CheckboxPopupViewController(nibName: "CheckboxPopup", bundle: nil)
                checkboxVc.setMessage(self.message!)
                checkboxVc.setCustomTitle(self.title!)
                checkboxVc.setCustomCheckboxTitle(checkboxTitle)
                
                OverlayBuilder.checkboxController = checkboxVc
                
                // Create the dialog
                popup = PopupDialog(viewController: checkboxVc, gestureDismissal: false)
            }
            else if let textFieldText = self.textFieldText{
                // Create a custom view controller
                let textboxVc = TextboxPopupViewController(nibName: "TextboxPopup", bundle: nil)
                textboxVc.popupTitle = self.title!
                textboxVc.textFieldDefaultText = textFieldText
                
                OverlayBuilder.textboxController = textboxVc
                
                // Create the dialog
                popup = PopupDialog(viewController: textboxVc, gestureDismissal: false)
            }
            else if let customViewController = self.customViewController{
                popup = PopupDialog(viewController: customViewController, gestureDismissal: false)
            }
            else{
                popup = PopupDialog(title: self.title, message: self.message,
                                    gestureDismissal: false)
            }
            
            
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
                let buttonCancel = CancelButton(title: "cancel".localized) {
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
    
    
    public static func isChecked() -> Bool{
        if let checkboxVc = OverlayBuilder.checkboxController{
            return checkboxVc.checkbox.isSelected
        }
        return false
    }
    
    public static func getTextFieldText() -> String{
        if let textboxVc = OverlayBuilder.textboxController{
            return textboxVc.textField.text!
        }
        return ""
    }
    
    
    
    
    public static func forceCloseAllOverlays(){
        OverlayBuilder.popupController?.dismiss(animated: false, completion: nil)
        OverlayBuilder.alertController?.dismiss(animated: false, completion: nil)
        
        OverlayBuilder.popupController = nil
        OverlayBuilder.alertController = nil
        OverlayBuilder.checkboxController = nil
        OverlayBuilder.textboxController = nil
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
