//
//  AddManuallyViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit

class AddManuallyViewController: MyViewController {

    @IBOutlet weak var labelError: UILabel!
    
    @IBOutlet weak var txtWrapperMemberName: TextFieldWrapper!
    @IBOutlet weak var txtWrapperKey: TextFieldWrapper!
    var myName:String?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        
        self.title = "add".localized
        self.hideKeyboardWhenTappedAround()
        self.txtWrapperKey.enableNumericMode()
        
        myName = IOSUtils.getUsername()
        
        if let pendingKey = Vars.pendingUserKey{
            txtWrapperKey.setText(pendingKey)
            Vars.pendingUserKey = nil
        }
    
        
    }
    
    
    
    private func validateAndSubmit(){
        
        AnimateBuilder.fadeOut(labelError)
        
        if(txtWrapperKey.validateNotEmpty("no_user_key_msg".localized)){
        
            var addingKey:String = txtWrapperKey.getText()
                                .trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)
            if addingKey.characters.count > 14{
                let index = addingKey.index(addingKey.startIndex, offsetBy: 14)
                addingKey = addingKey.substring(to: index)
            }
        
             showLoading(Message: "adding_friend_msg".localized)
            
            
            FirebaseDB.checkKeyExist(self.myModel.userId!, addingKey,
                                                  {(keyModel, status) in
                            if status == Status.Success && keyModel != nil{
            
                                if (self.txtWrapperMemberName.getText().isEmpty){
                                    self.txtWrapperMemberName.setText(keyModel?.userName)
                                }
                                
                                self.checkBothWayLinkExist(self.myModel.userId!, (keyModel?.userId)!, {(bothLinkExist) in
                                
                                    //already exist link and friend added
                                    if bothLinkExist && self.myModel.checkFriendExist((keyModel?.userId)!){
                                        self.errorOccured(AddUserErrorType.UserAlreadyAdded)
                                        return
                                    }
                                    
                                    FirebaseDB.addNewLink(self.myModel.userId!, (keyModel?.userId)!, self.myName!, self.txtWrapperMemberName.getText(), {(status) in
                                        
                                        guard status == Status.Success else{
                                            self.errorOccured(AddUserErrorType.UnknownError)
                                            return
                                        }
                                        
                                        self.successAddUser((keyModel?.userId)!, self.txtWrapperMemberName.getText(), self.myName!);
                                        
                                    })

                                
                                })
            
                                
                            }
                            else{
                                self.errorOccured(AddUserErrorType.KeyNotExistOrOutdated)
                            }
                            
                            
                                                
                    })
        
            
            
        }
    }
    
    
    public func checkBothWayLinkExist(_ myUserId:String, _ targetUserId:String,
                                             _ callback:@escaping (Bool) -> Void){
        
        Threadings.runInBackground {
            
            var counter:Int = 0
            var linkExist:Int = 0
            
            FirebaseDB.checkLinkExist(myUserId, targetUserId,
                                      {(result, status) in
                                        if status == Status.Success && result{
                                            linkExist += 1
                                        }
                                        counter += 1
            })
            
            
            FirebaseDB.checkLinkExist(targetUserId, myUserId,
                                      {(result, status) in
                                        if status == Status.Success && result{
                                            linkExist += 1
                                        }
                                        counter += 1
            })
            
            
            while counter < 2{
                Threadings.sleep(500)
            }
            
            callback(linkExist >= 2)
            
        }
        
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
        AnimateBuilder.fadeIn(labelError)
        
    }
    
    
    private func successAddUser(_ addingUserId:String, _ addingUsername:String, _ myName:String){
        let friendModel:FriendModel = FriendModel()
        friendModel.userId = addingUserId
        friendModel.username = addingUsername.isEmpty ? "No Name" : addingUsername
        
        self.myModel.addFriendModel(friendModel)
        friendModel.save()
        self.myModel.sortFriendModels()
        self.myModel.commitFriendUserIds()
        
        hideLoading()
        _ = self.navigationController?.popViewController(animated: true)
        
        //todo: send notification
    }

    
    
    @IBAction func onConfirmBtnTapped(_ sender: AnyObject) {
        validateAndSubmit()
    }
    
    
    enum AddUserErrorType{
        case KeyNotExistOrOutdated
        case UnknownError
        case UserAlreadyAdded
    }
    
}
