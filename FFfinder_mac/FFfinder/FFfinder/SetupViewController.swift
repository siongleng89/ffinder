//
//  SetupViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 11/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
import FirebaseInstanceID

class SetupViewController: MyViewController {

    @IBOutlet weak var statusLabel: UILabel!
    @IBOutlet weak var retryButton: UIButton!
    var keychain:KeychainSwift?
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.keychain = KeychainSwift()
        changeStatus(SetupStatus.CheckingToken)
        
        tryRetrieveFromKeyChain({
            (userId:String?, exception:SetupException?) in
            
            guard exception == nil else {
                self.changeStatus(SetupStatus.Failed)
                return
            }
            
            var finalUserId:String
            
            //succesfully retrieved userId
            if let retrievedUserId = userId {
                finalUserId = retrievedUserId;
            }
                //userId doesnt exist
            else{
                finalUserId = self.createNewUser();
            }
            
            
            //at this point we already have userId, can try to login Firebase now
            self.changeStatus(SetupStatus.CheckingUser)

            self.loginUser(finalUserId, {
                (exception:SetupException?) in
                    guard exception == nil else{
                        self.changeStatus(SetupStatus.Failed)
                        return
                    }
                
                
                //save user token into database
                self.saveUser(finalUserId,
                              {(exception2:SetupException?) in
                                
                                guard exception2 == nil else{
                                    self.changeStatus(SetupStatus.Failed)
                                    return
                                }
                                
                                //login and save success, userId is now populated in my model, can go to next screen now
                                self.myModel.save()
                                self.keychain!.set(self.myModel.userId!, forKey: KeyChainType.UserId.rawValue)
                                
                                self.changeStatus(SetupStatus.Success)
                                
                                //go to mainpage vc
                                let vc = self.storyboard?.instantiateViewController(withIdentifier: MainPageViewController.getMyClassName()) as! MainPageViewController
                                vc.myModel = self.myModel
                                let navController = UINavigationController(rootViewController: vc)
                                self.present(navController, animated: false, completion: nil)
                                
                              }
                )
            })
        })
        
    }
    
    private func tryRetrieveFromKeyChain(_ callback:@escaping (String?, SetupException?) -> Void){
        //id exist, check database get userId
        if let userId = self.keychain!.get(KeyChainType.UserId.rawValue){
            FirebaseDB.checkUserIdExist(userId,
                {(exist:Bool, status) in
                    if status == Status.Success && exist {
                        self.myModel.userId = userId
                        callback(userId, nil)
                    }
                    //id not exist in db, no way to retrieve, proceed to create new user
                    else if status == Status.Success && !exist{
                        callback(nil, nil)
                    }
                    else{
                        callback(userId, SetupException.DBFailed)
                    }
                }
            )
        }
        //id not exist in keychain, no way to retrieve, proceed to create new user
        else{
            callback(nil, nil)
        }
    }
    
    private func createNewUser() -> String!{
        let userId = FirebaseDB.getNewUserId()
        self.myModel.userId = userId
        
        return userId;
    }
    
    private func loginUser(_ userId:String, _ callback:@escaping (SetupException?) -> Void){
        self.myModel.loginFirebase(0,
                                   {(success) in
                                    if success{
                                        callback(nil)
                                    }
                                    else{
                                        callback(SetupException.LoginFailed)
                                    }
        })
    }
    
    
    private func saveUser(_ userId:String, _ callback: @escaping (SetupException?) -> Void){
       
        let token:String? = FIRInstanceID.instanceID().token()
        guard token != nil else{
            callback(SetupException.DBFailed)
            return;
        }
        
        FirebaseDB.updateUserToken(userId, token!,
                                   {(status) in
                                        callback(status == Status.Success ? nil :
                                            SetupException.DBFailed)
                                    }
        )
    }
    
    
    private func changeStatus(_ status:SetupStatus){
        var msg:String = "";
        
        retryButton.isHidden = true
        
        switch status {
            case SetupStatus.CheckingToken:
                msg = "retrieving_token".localized
            case SetupStatus.CheckingUser:
                msg = "initializing_user".localized
            case SetupStatus.Failed:
                msg = "no_connection_msg".localized
                retryButton.isHidden = false
            case SetupStatus.Success:
                msg = "ok".localized
        }
        
        statusLabel.text = msg
    }
    
    
    enum SetupStatus{
        case CheckingToken
        case CheckingUser
        case Failed
        case Success
    }
    
    enum SetupException:Error{
        case DBFailed
        case LoginFailed
    }
    
    
    
    
    
    
}
