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
    

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        self.myModel = MyModel.shared
    }
    
    
    public static func getMyClassName() -> String{
        return NSStringFromClass(self).components(separatedBy: ".").last!
    }
    
    
    override func viewDidLoad() {
        let app = UIApplication.shared
        let statusBarHeight: CGFloat = app.statusBarFrame.size.height
        
        if let nav = self.navigationController{
            let view = UIView(frame: CGRect(x: 0, y: -statusBarHeight,
                                            width: UIScreen.main.bounds.size.width, height: 20))
            view.backgroundColor = UIColor.colorStatusBar()
            nav.navigationBar.addSubview(view)
            
            nav.navigationBar.barTintColor = UIColor.colorStatusBar()
            nav.navigationBar.tintColor = UIColor.colorContrast()
            
            nav.navigationBar.titleTextAttributes = [NSForegroundColorAttributeName: UIColor.colorContrast()]
            
            navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: .plain, target: nil, action: nil)
        }
        else{
            let view = UIView(frame: CGRect(x: 0, y: 0,
                                            width: UIScreen.main.bounds.size.width, height: 20))
            view.backgroundColor = UIColor.colorStatusBar()
            self.view.addSubview(view)
        }
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        navigationItem.backBarButtonItem = UIBarButtonItem(title: "", style: .plain, target: nil, action: nil)
        
    }
    
    func hideNavBar(){
        if let nav = self.navigationController{
            nav.navigationBar.barTintColor = UIColor.clear
            nav.navigationBar.setBackgroundImage(UIImage(), for: .default)
            nav.navigationBar.shadowImage = UIImage()
        }
        
    }
    
    
    
    func addActionToNav(_ leftSide:Bool, _ type:NavItemActionType){
//        let add = UIBarButtonItem(barButtonSystemItem: .add, target: self, action: #selector(backButtonTapped))
//        let play = UIBarButtonItem(title: "Play", style: .plain, target: self, action: #selector(backButtonTapped))
//        
//        self.navigationItem.rightBarButtonItems = [add, play]
    }
    
    
    
    func hideKeyboardWhenTappedAround() {
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(MyViewController.dismissKeyboard))
        view.addGestureRecognizer(tap)
    }
    
    func dismissKeyboard() {
        view.endEditing(true)
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
    
    
  
 
    
}














