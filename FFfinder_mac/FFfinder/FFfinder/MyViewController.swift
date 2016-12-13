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
  

    func hideKeyboardWhenTappedAround() {
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(MyViewController.dismissKeyboard))
        view.addGestureRecognizer(tap)
    }
    
    func dismissKeyboard() {
        view.endEditing(true)
    }
    
  
    
    
    
    
    func adaptivePresentationStyle(for controller: UIPresentationController, traitCollection: UITraitCollection) -> UIModalPresentationStyle {
        return .none
    }
    
    
  
 
    
}














