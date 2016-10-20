//
//  MainPageViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 14/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit

class MainPageViewController: MyViewController, UITableViewDelegate, UITableViewDataSource {
    
    @IBOutlet weak var friendsTableView: UITableView!
    let friendTableCellIdentifier:String = "FriendTableViewCell"
    

    override func viewDidLoad() {
        super.viewDidLoad()

        myModel.loginFirebase(0, nil)
        
        // Do any additional setup after loading the view.
        self.navigationItem.title = "app_name".localized
        
        let shareButton:UIBarButtonItem = UIBarButtonItem(title: "Share", style: UIBarButtonItemStyle.plain, target: self, action: #selector(shareButtonTapped))
        self.navigationItem.leftBarButtonItem = shareButton
        
        
        let addManuallyButton:UIBarButtonItem = UIBarButtonItem(title: "Add Manually", style: UIBarButtonItemStyle.plain, target: self, action: #selector(addManuallyButtonTapped))
        self.navigationItem.rightBarButtonItem = addManuallyButton
        
        
        let yourNibName = UINib(nibName: friendTableCellIdentifier, bundle: nil)
        friendsTableView.register(yourNibName, forCellReuseIdentifier: friendTableCellIdentifier)
        
        NotificationCenter.default.addObserver(self, selector: #selector(friendModelChanged),
                                               name: .friendModelChanged, object: nil)
        
        NotificationCenter.default.addObserver(self,
                                           selector: #selector(onNeedToReloadFriendModel),                                               name: .needToReloadFriendModel, object: nil)

        
    }

    func shareButtonTapped(){
        self.performSegue(withIdentifier: "MainToShareKeySegue", sender: nil)
    }
    
    func addManuallyButtonTapped(){
        self.performSegue(withIdentifier: "ShareToAddManuallySegue", sender: nil)
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 161
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.myModel.friendModels.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) ->       UITableViewCell{
        
        let cell:FriendTableViewCell! = tableView.dequeueReusableCell(withIdentifier: friendTableCellIdentifier, for:indexPath)as! FriendTableViewCell
        let friendModel:FriendModel = self.myModel.friendModels[indexPath.row]
        cell.update(friendModel)
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let cell:FriendTableViewCell! = tableView.cellForRow(at: indexPath) as! FriendTableViewCell!
        cell.tapped(self.myModel.userId!)
        
    }
    
    func friendModelChanged(notification:NSNotification){
        let friendModel:FriendModel = notification.userInfo!["friendModel"] as! FriendModel
        onUpdateRowRequired(friendModel)
    }
    
    func onNeedToReloadFriendModel(notification:NSNotification){
        let reloadFriendId = notification.userInfo!["reloadFriendId"] as! String
        if let reloadFriendModel = self.myModel.getFriendModelById(reloadFriendId) {
            reloadFriendModel.load()
            onUpdateRowRequired(reloadFriendModel)
        }
    }
    
    func onUpdateRowRequired(_ friendModel:FriendModel){
        var finalIndex = -1
        
        for i in 0 ..< self.myModel.friendModels.count {
            if self.myModel.friendModels[i].userId == friendModel.userId{
                finalIndex = i
                break
            }
        }
        
        if finalIndex >= 0{
            let indexPath = IndexPath(row: finalIndex, section: 0)
            Threadings.postMainThread {
                self.friendsTableView.reloadRows(at: [indexPath], with: UITableViewRowAnimation.automatic)
            }
        }

    }
    
    
}
