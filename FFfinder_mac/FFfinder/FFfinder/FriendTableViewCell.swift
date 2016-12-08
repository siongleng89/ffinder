//
//  FriendTableViewCell.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
import FirebaseInstanceID

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
    private var searchButton:SearchButton?
    private var friendTableViewProtocol:FriendTableViewProtocol?
    
    
    
    override func awakeFromNib() {
       
        super.awakeFromNib()
        // Initialization code
        
        self.imageViewProfile.layoutIfNeeded()
        self.imageViewProfile.layer.cornerRadius = self.imageViewProfile.frame.size.width / 2
        self.imageViewProfile.clipsToBounds = true
        self.imageViewProfile.layer.borderWidth = 1.0
        self.imageViewProfile.layer.borderColor = UIColor.colorPrimaryDark().cgColor
        
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
    
    public func update(_ friendModel:FriendModel, _ myModel:MyModel){
        Logs.show("Updating friend...")
        
        self.searchButton = SearchButtonPools.getSearchButton(friendModel.userId!)
        
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
                    self.searchButton?.setFlower(FlowerType.Ending, extra: "searchSuccess")
                }
            }
            else{
                if let searchResult = self.friendModel?.searchResult{
                    if (searchResult.errorTriggeredAutoNotification()){
                        self.searchButton?.setFlower(FlowerType.AutoSearching, animate: false)
                    }
                    else{
                        self.searchButton?.setFlower(FlowerType.Satisfied)
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
                
                self.myModel?.deleteFriend((self.friendModel)!)
                self.friendModel?.delete()
                self.myModel?.commitFriendUserIds()
                
                NotificationCenter.default.post(name: .needToReloadWholeFriendsList, object: nil)
        })
        .show()
        
       
    }
    
    @IBAction func onCloseErrorTapped(_ sender: AnyObject) {
        self.friendModel?.hideErrorMsg = true
        self.friendModel?.save()
        self.friendModel?.notificateChanged()
    }
    
    @objc private func startSearch(){
        SearchPools.newTask(friendModel!, myModel!)
    }
    
    private func setListeners(){
        if let recognizers = self.buttonDelete?.gestureRecognizers{
            for recognizer: UIGestureRecognizer in recognizers {
                self.buttonDelete?.removeGestureRecognizer(recognizer)
            }
        }
        
        self.buttonDelete?.addGestureRecognizer(UITapGestureRecognizer(target: self,
                                                                       action: #selector(onDeleteTapped)))
    }
   
    
    
    
    
}
