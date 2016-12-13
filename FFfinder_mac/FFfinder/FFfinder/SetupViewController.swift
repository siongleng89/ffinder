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

    @IBOutlet weak var layoutRetry: UIView!
    @IBOutlet weak var labelStatus: UILabel!
    @IBOutlet weak var loadingIcon: UIActivityIndicatorView!
    @IBOutlet weak var imageViewRetryIcon: UIImageView!
    var keychain:KeychainSwift?

    
    override func viewDidAppear(_ animated: Bool) {
        AnimateBuilder.build(imageViewRetryIcon).setAnimateType(AnimateType.RotateBy)
            .setValue(3.14).setDurationMs(2000).setRepeat(true).start();
        
        self.keychain = KeychainSwift()
        start()
        
        setListeners()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        Analytics.setScreen(name: "ActivitySetup")
    }
    
    private func start(){
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
            
            self.myModel.userId = finalUserId
            
            //at this point we already have userId, can try to login Firebase now
            self.changeStatus(SetupStatus.CheckingUser)
            
            self.loginUser(finalUserId, {
                (exception:SetupException?) in
                guard exception == nil else{
                    self.changeStatus(SetupStatus.Failed)
                    return
                }
                
                
                //save user token into database
                self.saveUser(finalUserId, 0,
                              {(exception2:SetupException?) in
                                
                                guard exception2 == nil else{
                                    self.changeStatus(SetupStatus.Failed)
                                    return
                                }
                                
                                //login and save success, userId is now populated in my model, can go to next screen now
                                self.myModel.save()
                                self.keychain!.set(self.myModel.userId!, forKey: KeyChainType.UserId.rawValue)
                                
                                self.changeStatus(SetupStatus.Success)
                                self.finishProcessAndGoToNextScreen()
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
    
    
    private func saveUser(_ userId:String, _ count:Int, _ callback: @escaping (SetupException?) -> Void){
       
        Threadings.runInBackground {
            let token:String? = FIRInstanceID.instanceID().token()
            guard token != nil else{
                
                if count > 3{
                    Threadings.postMainThread {
                        callback(SetupException.DBFailed)
                    }
                    
                }
                else{
                    Threadings.sleep(3000)
                    self.saveUser(userId, count + 1, callback)
                }
               
                return;
            }
            
            Threadings.postMainThread {
                FirebaseDB.updateUserToken(userId, token!,
                                           {(status) in
                                            callback(status == Status.Success ? nil :
                                                SetupException.DBFailed)
                    }
                )
            }

        }
    }
    
    private func finishProcessAndGoToNextScreen(){
        FirebaseDB.checkUserHasAnyLink(self.myModel.userId!, {
            (exist, status) in
            //no link found, add me in
            if !exist{
                FirebaseDB.addNewLink(self.myModel.userId!, self.myModel.userId!,
                                      "address_myself".localized, "address_myself".localized, {
                                            (status) in
                                        let myOwnModel:FriendModel = FriendModel()
                                        myOwnModel.userId = self.myModel.userId!
                                        myOwnModel.username = "address_myself".localized
                                        myOwnModel.save()
                                        self.myModel.addFriendModel(myOwnModel)
                                        self.myModel.sortFriendModels()
                                        self.myModel.commitFriendUserIds()
                                        
                                        let vc = self.storyboard?.instantiateViewController(withIdentifier: MainPageViewController.getMyClassName()) as! MainPageViewController
                                        vc.firstTimeRun = true
                                        let navController = UINavigationController(rootViewController: vc)
                                        self.present(navController, animated: false, completion: nil)
                
                })
            }
            else{
                let vc = self.storyboard?.instantiateViewController(withIdentifier: MainPageViewController.getMyClassName()) as! MainPageViewController
                let navController = UINavigationController(rootViewController: vc)
                self.present(navController, animated: true, completion: nil)
            }
            
        })
    
    }
    
    private func changeStatus(_ status:SetupStatus){
        var msg:String = "";
        
        layoutRetry.alpha = 0
        layoutRetry.isHidden = true
        loadingIcon.alpha = 1
        
        switch status {
            case SetupStatus.CheckingToken:
                msg = "retrieving_token".localized
            case SetupStatus.CheckingUser:
                msg = "initializing_user".localized
            case SetupStatus.Failed:
                msg = "no_connection_msg".localized
                layoutRetry.isHidden = false
                loadingIcon.alpha = 0
                AnimateBuilder.fadeIn(layoutRetry)
            case SetupStatus.Success:
                msg = "initialization_done".localized
        }
        
        labelStatus.text = msg
    }
    
    
    private func setListeners(){
        let gesture = UITapGestureRecognizer(target: self, action: #selector(SetupViewController.onLayoutRetryTapped(_:)))
        self.layoutRetry.addGestureRecognizer(gesture)
    }
    
    @objc private func onLayoutRetryTapped(_ sender:UITapGestureRecognizer){
        start()
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
