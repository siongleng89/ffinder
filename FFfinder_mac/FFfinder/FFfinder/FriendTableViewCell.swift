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
    
    @IBOutlet weak var labelName: UILabel!
    
    @IBOutlet weak var labelAddress: UILabel!
    
    @IBOutlet weak var labelUpdated: UILabel!

    @IBOutlet weak var labelError: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        //super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    public func update(_ friendModel:FriendModel){
        self.friendModel = friendModel
        
        labelName.text = friendModel.username
        
        if let address = friendModel.locationModel?.address{
            labelAddress.text = address
        }
        else{
            labelAddress.text = "never_locate_user_msg".localized
        }
        
        if let timestamp:String = friendModel.locationModel?.timestampLastUpdated,
            !timestamp.isEmpty{
            labelUpdated.text = DateTimeUtils.convertUnixTimeToDateTime(UInt64(timestamp)!)
        }
        else{
            labelUpdated.text = ""
        }
        
        if let searchResult = friendModel.searchResult, searchResult != SearchResult.Normal{
            labelError.text = SearchResult.getMessage(searchResult)
        }
        else{
            labelError.text = ""
        }
        
        if let searchStatus = friendModel.searchStatus, searchStatus != SearchStatus.End{
            labelAddress.text = SearchStatus.getMessage(searchStatus)
        }
    }
    
    public func tapped(_ myUserId:String){
        if let friendModel:FriendModel = self.friendModel,
                (friendModel.searchStatus == SearchStatus.End || friendModel.searchStatus == nil){
            startSearch(friendModel, myUserId)
        }
        
        print(friendModel?.searchStatus)
    }
    
    private func startSearch(_ friendModel:FriendModel, _ myUserId:String){
        friendModel.searchStatus = SearchStatus.Starting
        
        friendModel.searchStatus = SearchStatus.CheckingData
        
        FirebaseDB.getUserToken(myUserId,
                                {(token, status) in
                                    if token != nil && status == Status.Success{
                                        
                                        var dict = [String:String]()
                                        dict["senderToken"] = token
                                        
                                        NotificationSender.sendWithUserId(myUserId, friendModel.userId!, FCMMessageType.UpdateLocation, NotificationSender.TTL_INSTANT, dict:dict)
                                        
                                        friendModel.searchStatus = SearchStatus.WaitingUserRespond
                                        
                                    }
                                    
        
        })
        
       
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
