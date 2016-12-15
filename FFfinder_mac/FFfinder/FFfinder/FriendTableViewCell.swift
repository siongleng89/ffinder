//
//  FriendTableViewCell.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
import FirebaseInstanceID
import Kingfisher

class FriendTableViewCell: UITableViewCell {
    private var friendModel:FriendModel?
    private var myModel:MyModel?

    @IBOutlet weak var searchButtonContainer: UIView!
    @IBOutlet weak var imageViewProfile: UIImageView!
    
    @IBOutlet weak var labelShortName: UILabel!
    @IBOutlet weak var labelName: UILabel!
    @IBOutlet weak var labelAddress: UILabel!
    
    @IBOutlet weak var labelStatus: UILabel!
    @IBOutlet weak var labelError: UILabel!
    @IBOutlet weak var errorViewHeight: NSLayoutConstraint!

    @IBOutlet weak var errorView: UIView!
    
    @IBOutlet weak var buttonDelete: FFIconButton!
    @IBOutlet weak var buttonMap: FFIconButton!
    @IBOutlet weak var buttonBlock: FFIconButton!
    
    private var searchButton:SearchButton?
    private var friendTableViewProtocol:FriendTableViewProtocol?
    private var friendTableProtocol:FriendTableViewProtocol?
    
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        self.imageViewProfile.layoutIfNeeded()
        self.imageViewProfile.layer.cornerRadius = self.imageViewProfile.frame.size.width / 2
        self.imageViewProfile.clipsToBounds = true
        self.imageViewProfile.layer.borderWidth = 1.0
        self.imageViewProfile.layer.borderColor = UIColor.colorPrimaryDark().cgColor
        self.imageViewProfile.isUserInteractionEnabled = true
        
        self.labelName.isUserInteractionEnabled = true
        
        setListeners()
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        //super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    override var layoutMargins: UIEdgeInsets {
        get { return UIEdgeInsets.zero }
        set(newVal) {}
    }
    
    public func update(_ friendModel:FriendModel, _ myModel:MyModel,
                       _ friendProtocol:FriendTableViewProtocol){
        Logs.show("Updating friend...")
        self.friendTableProtocol = friendProtocol
        
        self.searchButton = SearchButtonPools.getSearchButton(friendModel.userId!)
        self.searchButton?.onAppearing()
        
        if let recognizers = self.searchButton?.gestureRecognizers{
            for recognizer: UIGestureRecognizer in recognizers {
                self.searchButton?.removeGestureRecognizer(recognizer)
            }
        }
        
        self.searchButton?.addGestureRecognizer(UITapGestureRecognizer(target: self,
                                                                 action: #selector(startSearch)))
        self.searchButtonContainer.addSubview(self.searchButton!)
        
        self.friendModel = friendModel
        self.myModel = myModel
        
        let filename = IOSUtils.getDocumentsDirectory()
            .appendingPathComponent("\(friendModel.userId!).png")
        
        
        self.imageViewProfile.kf.setImage(with: filename)
        
        labelName.text = friendModel.username
        labelShortName.text = StringsHelper.safeSubstring(friendModel.username, 2).uppercased()
        
        
        //still searching
        if let searchStatus = friendModel.searchStatus, searchStatus != SearchStatus.End{
    
            setErrorViewVisibility(false)
            labelAddress.isHidden = true
            labelStatus.isHidden = false
            labelStatus.text = SearchStatus.getMessage(searchStatus)
            
            if friendModel.timeoutPhase == 0{
                self.searchButton?.setFlower(FlowerType.Starting)
            }
            else if friendModel.timeoutPhase == 1{
                self.searchButton?.setFlower(FlowerType.Troubling)
            }
            else if friendModel.timeoutPhase == 2{
                self.searchButton?.setFlower(FlowerType.Confusing)
            }
            
        }
        //search ended
        else{
            labelAddress.isHidden = false
            labelStatus.isHidden = true
            
            //set address
            if let address = friendModel.locationModel?.address{
                labelAddress.text = address
            }
            else{
                labelAddress.text = "never_locate_user_msg".localized
            }
            
            //set last updated
            if let timestamp:String = friendModel.locationModel?.timestampLastUpdated,
                !timestamp.isEmpty{
                searchButton?.setLastUpdated(DateTimeUtils
                    .convertUnixTimeToDateTime(UInt64(timestamp)!))
            }
            else{
                searchButton?.setLastUpdated("never".localized)
            }
            
            if self.friendModel?.recentlyFinished == true{
                self.friendModel?.recentlyFinished = false
                
                if (self.friendModel?.searchResult?.errorTriggeredAutoNotification())!{
                    self.searchButton?.setFlower(FlowerType.Ending, extra: "autoSearching")
                }
                else{
                    
                    if (self.friendModel?.searchResult?.isError())!{
                        self.searchButton?.setFlower(FlowerType.Ending, extra: "error")
                    }
                    else{
                        self.searchButton?.setFlower(FlowerType.Ending, extra: "searchSuccess")
                    }
                    
                    
                }
            }
            else{
                if let searchResult = self.friendModel?.searchResult{
                    if (searchResult.errorTriggeredAutoNotification()){
                        self.searchButton?.setFlower(FlowerType.AutoSearching, animate: false)
                    }
                    else{
                        if (searchResult.isError()){
                            self.searchButton?.setFlower(FlowerType.Sleeping)
                        }
                        else{
                            
                            if let timestamp:String = friendModel.locationModel?.timestampLastUpdated,
                                !timestamp.isEmpty{
                                
                                if DateTimeUtils.checkIsOlderThanYesterday(UInt64(timestamp)!){
                                    self.searchButton?.setFlower(FlowerType.Sleeping)
                                }
                                else{
                                    self.searchButton?.setFlower(FlowerType.Satisfied)
                                }
                            }
                            else{
                                 self.searchButton?.setFlower(FlowerType.Sleeping)
                            }
                        }
                    }
                }
                else{
                    self.searchButton?.setFlower(FlowerType.Sleeping)
                }
               
            }
            
            //set error if exist
            if let searchResult = self.friendModel?.searchResult{
                if (searchResult.isError() && self.friendModel?.hideErrorMsg == false){
                    setErrorViewVisibility(true)
                    labelError.text = SearchResult.getMessage(searchResult)
                }
                else{
                    setErrorViewVisibility(false)                }
            }
            else{
                setErrorViewVisibility(false)
            }
            
        }
        
        
        if (self.friendModel?.blocked)!{
            self.buttonBlock.setIsBtnSelected(true)
        }
        else{
            self.buttonBlock.setIsBtnSelected(false)
        }
        
        if let locationModel = self.friendModel?.locationModel{
            if let latitude = locationModel.latitude{
                if !StringsHelper.isEmpty(latitude){
                    self.buttonMap.isEnabled = true
                }
                else{
                    self.buttonMap.isEnabled = false
                }
            }
            else{
                self.buttonMap.isEnabled = false
            }
        }
        else{
            self.buttonMap.isEnabled = false
        }
        
        
       
       
    }
    
    private func setErrorViewVisibility(_ show:Bool){
        if show{
            for view in self.errorView.subviews{
                view.isHidden = false
            }
            errorView.isHidden = false
            errorViewHeight.constant = 40
        }
        else{
            for view in self.errorView.subviews{
                view.isHidden = true
            }
            errorView.isHidden = true
            errorViewHeight.constant = 0
        }
    }
    
    public func tapped(_ myUserId:String){
        
    }
    
    func onDeleteTapped() {
        
       OverlayBuilder.build().setOverlayType(OverlayType.OkOrCancel)
        .setTitle("delete_user_title".localized.format((self.friendModel?.username!)!))
        .setMessage("confirm_delete_user_msg".localized)
        .setOnChoices ({
                FirebaseDB.deleteLink((self.myModel?.userId)!, (self.friendModel?.userId!)!)
                
                //remove blocking user since already deleted
                FirebaseDB.changeBlockUser((self.myModel?.userId)!, (self.friendModel?.userId!)!,
                                           false, nil);
            
                SearchButtonPools.removeSearchButton((self.friendModel?.userId!)!)
                self.myModel?.deleteFriend((self.friendModel)!)
                self.friendModel?.delete()
                self.myModel?.commitFriendUserIds()
            
                NotificationCenter.default.post(name: .needToReloadWholeFriendsList, object: nil)
        
            
        })
        .show()
        
       
    }
    
    func onBlockTapped(){
        
        if (self.friendModel?.blocked)!{
            self.friendModel?.blocked = false
            self.friendModel?.save()
            self.updateBlockUserDatabase()
            self.friendModel?.notificateChanged()
        }
        else{
            
            if let _ = Preferences.get(PreferenceType.DontRemindBlockSearch){
                self.friendModel?.blocked = true
                self.friendModel?.save()
                self.updateBlockUserDatabase()
                self.friendModel?.notificateChanged()
            }
            else{
                OverlayBuilder.build().setOverlayType(OverlayType.OkOrCancel)
                    .setTitle("block_dialog_title".localized.format((self.friendModel?.username)!))
                    .setMessage("block_dialog_content".localized.format((self.friendModel?.username)! , (self.friendModel?.username)!))
                    .setCheckboxTitle("dont_show_this_again".localized)
                    .setOnChoices ({
                        self.checkUserTickDontShowBlockMessageAgain()
                        self.friendModel?.blocked = true
                        self.friendModel?.save()
                        self.updateBlockUserDatabase()
                        self.friendModel?.notificateChanged()
                        }, {
                            self.checkUserTickDontShowBlockMessageAgain()
                            
                    })
                    .show()

            }
            
        }
    
    }
    
    func updateBlockUserDatabase(){
     FirebaseDB.changeBlockUser((self.myModel?.userId)!, (self.friendModel?.userId)!, (self.friendModel?.blocked)!, nil)
    }
    
    func checkUserTickDontShowBlockMessageAgain(){
        if OverlayBuilder.isChecked(){
            Preferences.put(PreferenceType.DontRemindBlockSearch, "1")
        }
    }
    
    func onProfileImageTapped(){
        friendTableProtocol?.onRequestPickImage(self.friendModel!)
    }
    
    func onNameTapped(){
        friendTableProtocol?.onRequestChangeName(self.friendModel!)
    }
    
    func onMapTapped(){
        friendTableProtocol?.onRequestShowMap(self.friendModel!)
    }
    
    @IBAction func onCloseErrorTapped(_ sender: AnyObject) {
        self.friendModel?.hideErrorMsg = true
        self.friendModel?.save()
        self.friendModel?.notificateChanged()
    }
    
    @objc public func startSearch(){
        
        if let searchResult = self.friendModel?.searchResult{
            if searchResult.errorTriggeredAutoNotification(){
                
                let autoSearchVc = WaitAutoNotificationPopupViewController(nibName: "WaitAutoNotificationPopupView", bundle: nil)
                
                OverlayBuilder.build().setCustomVc(autoSearchVc)
                .setOverlayType(OverlayType.Nothing).show()
                
                autoSearchVc.onSearchAgainChosen = {
                    OverlayBuilder.forceCloseAllOverlays()
                    SearchPools.newTask(self.friendModel!, self.myModel!)
                }
                
                autoSearchVc.onWaitAutoNotificationChosen = {
                    OverlayBuilder.forceCloseAllOverlays()
                }
                
                return
            }
        }
        
       SearchPools.newTask(friendModel!, myModel!)
        
        
    }
    
    private func setListeners(){
        
        if let recognizers = self.buttonMap?.gestureRecognizers{
            for recognizer: UIGestureRecognizer in recognizers {
                self.buttonMap?.removeGestureRecognizer(recognizer)
            }
        }
        
        self.buttonMap?.addGestureRecognizer(UITapGestureRecognizer(target: self,
                                                                       action: #selector(onMapTapped)))
        
        
        if let recognizers = self.buttonDelete?.gestureRecognizers{
            for recognizer: UIGestureRecognizer in recognizers {
                self.buttonDelete?.removeGestureRecognizer(recognizer)
            }
        }
        
        self.buttonDelete?.addGestureRecognizer(UITapGestureRecognizer(target: self,
                                                                       action: #selector(onDeleteTapped)))
        
        
        
        if let recognizers = self.buttonDelete?.gestureRecognizers{
            for recognizer: UIGestureRecognizer in recognizers {
                self.buttonBlock?.removeGestureRecognizer(recognizer)
            }
        }
        
        self.buttonBlock?.addGestureRecognizer(UITapGestureRecognizer(target: self,
                                                                       action: #selector(onBlockTapped)))
        
        if let recognizers = self.imageViewProfile?.gestureRecognizers{
            for recognizer: UIGestureRecognizer in recognizers {
                self.imageViewProfile?.removeGestureRecognizer(recognizer)
            }
        }
        
        self.imageViewProfile?.addGestureRecognizer(UITapGestureRecognizer(target: self,
                                                                      action: #selector(onProfileImageTapped)))
        
        if let recognizers = self.labelName?.gestureRecognizers{
            for recognizer: UIGestureRecognizer in recognizers {
                self.labelName?.removeGestureRecognizer(recognizer)
            }
        }
        
        self.labelName?.addGestureRecognizer(UITapGestureRecognizer(target: self,
                                                                           action: #selector(onNameTapped)))
    }
   
    
    
    
    
}
