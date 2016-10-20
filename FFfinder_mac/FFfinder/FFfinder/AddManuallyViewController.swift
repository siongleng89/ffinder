//
//  AddManuallyViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit

class AddManuallyViewController: MyViewController {

    @IBOutlet weak var txtFieldUserKey: UITextField!
    @IBOutlet weak var txtFieldFriendName: UITextField!
    @IBOutlet weak var txtFieldYourName: UITextField!
    @IBOutlet weak var labelError: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        
        txtFieldYourName.text = IOSUtils.getUsername()
    }
    
    
    
    private func checkCanAdd(){
        showLoading(Message: "adding_friend_msg".localized)
        clearError()
        
        let targetKey:String = txtFieldUserKey.text!
        let myName:String = txtFieldYourName.text!
        
        FirebaseDB.checkKeyExist(self.myModel.userId!, targetKey,
             {(keyModel, status) in
                if status == Status.Success && keyModel != nil{
                    
                    if (self.txtFieldFriendName.text?.isEmpty)!{
                        self.txtFieldFriendName.text = keyModel?.userName
                    }
                    
                    guard self.myModel.checkFriendExist((keyModel?.userId)!) == false else{
                        self.errorOccured(AddUserErrorType.UserAlreadyAdded)
                        return
                    }
                    
                    FirebaseDB.addNewLink(self.myModel.userId!, (keyModel?.userId)!, myName, self.txtFieldFriendName.text!, {(status) in
                        
                        guard status == Status.Success else{
                            self.errorOccured(AddUserErrorType.UnknownError)
                            return
                        }
                        
                        self.successAddUser((keyModel?.userId)!, self.txtFieldFriendName.text!, myName);
                        
                    })
                    
                }
                else{
                    self.errorOccured(AddUserErrorType.KeyNotExistOrOutdated)
                }
                
                
                                    
        })
    }
    
    private func clearError(){
        labelError.text = ""
    }
        
        
    private func errorOccured(_ errorType:AddUserErrorType){
        
        var msg:String = ""
        
        switch errorType{
            case AddUserErrorType.UserAlreadyAdded:
                msg = "user_already_added_error_msg".localized
            break
            case AddUserErrorType.KeyNotExistOrOutdated:
                msg = "key_expired_or_not_exist_msg".localized
            break
            case AddUserErrorType.UnknownError:
                msg = "unknown_error_msg".localized
            break
        }
        
        
        hideLoading()
        labelError.text = msg
        
    }
    
    
    private func successAddUser(_ addingUserId:String, _ addingUsername:String, _ myName:String){
        let friendModel:FriendModel = FriendModel()
        friendModel.userId = addingUserId
        friendModel.username = addingUsername.isEmpty ? "No Name" : addingUsername
        
        self.myModel.addFriendModel(friendModel)
        friendModel.save()
        self.myModel.commitFriendUserIds()
        
        hideLoading()
        
        //todo: send notification
    }

    
    
    @IBAction func onConfirmBtnTapped(_ sender: AnyObject) {
        checkCanAdd()
    }
    
    
    enum AddUserErrorType{
        case KeyNotExistOrOutdated
        case UnknownError
        case UserAlreadyAdded
    }
    
}
