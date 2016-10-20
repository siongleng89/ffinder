//
//  MyViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 12/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import UIKit

class MyViewController : UIViewController, UIPopoverPresentationControllerDelegate{
    
    var myModel:MyModel!
    var alertController:UIAlertController?
    
    
    public static func getMyClassName() -> String{
        return NSStringFromClass(self).components(separatedBy: ".").last!
    }

    func setLeftTabButtonAsBack(){
        let backButton:UIBarButtonItem = UIBarButtonItem(title: "Back", style: UIBarButtonItemStyle.plain, target: self, action: #selector(backButtonTapped))
        self.navigationItem.leftBarButtonItem = backButton
    }
    
    func backButtonTapped(){
        dismiss(animated: true, completion: nil)
    }
    
    func showPopover(_ popoverContent:UIViewController){
        popoverContent.modalPresentationStyle = .popover
        if let popover = popoverContent.popoverPresentationController {
            
            //no arrow
            popover.permittedArrowDirections = .init(rawValue: 0)
            
            //let viewForSource = sender as! UIView
            popover.sourceView = self.view
            
            // the position of the popover where it's showed
            popover.sourceRect = CGRect(x: self.view.bounds.midX, y: self.view.bounds.midY ,width: 0, height:0)

            
            // the size you want to display
            popoverContent.preferredContentSize = CGSize(width: popoverContent.view.bounds.width, height: popoverContent.view.bounds.height)

            popover.delegate = self
        }
        
        self.present(popoverContent, animated: true, completion: nil)
    }
    
    func showLoading(Message msg:String = "loading".localized){
        alertController = UIAlertController(title: nil, message: msg, preferredStyle: .alert)
        
        alertController!.view.tintColor = UIColor.black
        let loadingIndicator: UIActivityIndicatorView = UIActivityIndicatorView(
            frame: CGRect(x:10, y:5, width:50, height:50)) as UIActivityIndicatorView
        loadingIndicator.hidesWhenStopped = true
        loadingIndicator.activityIndicatorViewStyle = UIActivityIndicatorViewStyle.gray
        loadingIndicator.startAnimating();
        
        alertController!.view.addSubview(loadingIndicator)
        self.present(alertController!, animated: true, completion: nil)
    }
    
    func hideLoading(){
        alertController?.dismiss(animated: true, completion: nil)
    }
    
    func showConfirmDialog(title:String? = "", message:String? = "",
                           positiveText:String? = "ok".localized,
                           negativeText:String? = "cancel".localized,
                           positiveToRun:(()->Void)? = nil,
                           negativeToRun:(()->Void)? = nil){
        let refreshAlert = UIAlertController(title: title, message: message, preferredStyle: UIAlertControllerStyle.alert)
        
        refreshAlert.addAction(UIAlertAction(title: positiveText, style: .default, handler: { (action: UIAlertAction!) in
            if positiveToRun != nil{
                positiveToRun!()
            }
        }))
        
        refreshAlert.addAction(UIAlertAction(title: negativeText, style: .cancel, handler: { (action: UIAlertAction!) in
            if negativeToRun != nil{
                negativeToRun!()
            }
        }))
        
        self.present(refreshAlert, animated: true, completion: nil)
    
    }
    
    
    
    
    func adaptivePresentationStyle(for controller: UIPresentationController, traitCollection: UITraitCollection) -> UIModalPresentationStyle {
        return .none
    }
    
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let vc = segue.destination as? MyViewController{
            vc.myModel = self.myModel
        }
    }
 
    
}














