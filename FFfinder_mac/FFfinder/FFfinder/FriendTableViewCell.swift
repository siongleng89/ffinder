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

    @IBOutlet weak var imageViewProfile: UIImageView!
    
    @IBOutlet weak var labelShortName: UILabel!
    @IBOutlet weak var labelName: UILabel!
    @IBOutlet weak var labelAddress: UILabel!
    
    @IBOutlet weak var labelStatus: UILabel!
    @IBOutlet weak var labelUpdated: UILabel!

    @IBOutlet weak var labelError: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        
        self.imageViewProfile.layoutIfNeeded()
        self.imageViewProfile.layer.cornerRadius = self.imageViewProfile.frame.size.width / 2
        self.imageViewProfile.clipsToBounds = true
        self.imageViewProfile.layer.borderWidth = 1.0
        self.imageViewProfile.layer.borderColor = UIColor.colorPrimaryDark().cgColor
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        //super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    public func update(_ friendModel:FriendModel, _ myModel:MyModel){
        self.friendModel = friendModel
        self.myModel = myModel
        
        labelName.text = friendModel.username
        labelShortName.text = StringsHelper.safeSubstring(friendModel.username, 2).uppercased()
        
//        
//        if let address = friendModel.locationModel?.address{
//            labelAddress.text = address
//        }
//        else{
//            labelAddress.text = "never_locate_user_msg".localized
//        }
//        
//        if let timestamp:String = friendModel.locationModel?.timestampLastUpdated,
//            !timestamp.isEmpty{
//            labelUpdated.text = DateTimeUtils.convertUnixTimeToDateTime(UInt64(timestamp)!)
//        }
//        else{
//            labelUpdated.text = ""
//        }
//        
//        if let searchResult = friendModel.searchResult, searchResult != SearchResult.Normal{
//            labelError.text = SearchResult.getMessage(searchResult)
//        }
//        else{
//            labelError.text = ""
//        }
//        
//        if let searchStatus = friendModel.searchStatus, searchStatus != SearchStatus.End{
//            labelAddress.text = SearchStatus.getMessage(searchStatus)
//        }
    }
    
    public func tapped(_ myUserId:String){
        if let friendModel:FriendModel = self.friendModel,
                (friendModel.searchStatus == SearchStatus.End || friendModel.searchStatus == nil){
            startSearch()
        }
        
        print(friendModel?.searchStatus)
    }
    
    private func startSearch(){
        SearchPools.newTask(friendModel!, myModel!)
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
