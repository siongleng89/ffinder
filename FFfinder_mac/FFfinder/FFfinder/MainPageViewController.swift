//
//  MainPageViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 14/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
import PopupDialog

class MainPageViewController: MyViewController, UITableViewDelegate, UITableViewDataSource {
    
    @IBOutlet weak var friendsTableView: UITableView!
    let friendTableCellIdentifier:String = "FriendTableViewCell"
    var firstTimeRun:Bool?

    override func viewDidLoad() {
        super.viewDidLoad()

        friendsTableView.rowHeight = UITableViewAutomaticDimension
        friendsTableView.estimatedRowHeight = 150
        
        self.navigationController?.setNavigationBarHidden(false, animated: false)
        FirebaseDB.saveToIdentifier(self.myModel!.userId!, nil)
        
        // Do any additional setup after loading the view.
        self.navigationItem.title = "app_name".localized
        
        self.addActionToNav(true, NavItemActionType.Settings)
        
        self.navigationItem.leftBarButtonItem = nil;
        self.navigationItem.hidesBackButton = true;
        
        let yourNibName = UINib(nibName: friendTableCellIdentifier, bundle: nil)
        friendsTableView.register(yourNibName, forCellReuseIdentifier: friendTableCellIdentifier)
        
        NotificationCenter.default.addObserver(self, selector: #selector(friendModelChanged),
                                               name: .friendModelChanged, object: nil)
        
        NotificationCenter.default.addObserver(self,
                                           selector: #selector(onNeedToReloadFriendModel),                                               name: .needToReloadFriendModel, object: nil)
        
        
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.myModel.friendModels.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) ->       UITableViewCell{
        
        let cell:FriendTableViewCell! = tableView.dequeueReusableCell(withIdentifier: friendTableCellIdentifier, for:indexPath)as! FriendTableViewCell
        let friendModel:FriendModel = self.myModel.friendModels[indexPath.row]
        cell.update(friendModel, self.myModel)
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let cell:FriendTableViewCell! = tableView.cellForRow(at: indexPath) as! FriendTableViewCell!
        cell.tapped(self.myModel.userId!)
        
    }
    
    func friendModelChanged(notification:NSNotification){
        Logs.show("Reloading row: friendModelChanged" )
        let friendModel:FriendModel = notification.userInfo!["friendModel"] as! FriendModel
        onUpdateRowRequired(friendModel)
    }
    
    func onNeedToReloadFriendModel(notification:NSNotification){
        Logs.show("Reloading row: onNeedToReloadFriendModel")
        let reloadFriendId = notification.userInfo!["reloadFriendId"] as! String
        if let reloadFriendModel = self.myModel.getFriendModelById(reloadFriendId) {
            reloadFriendModel.load()
            
            //search success
            if reloadFriendModel.searchStatus == SearchStatus.End{
                SearchPools.taskFinish(reloadFriendModel)
            }
            //alive msg
            else{
                onUpdateRowRequired(reloadFriendModel)
            }
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
                self.friendsTableView.reloadRows(at: [indexPath], with: UITableViewRowAnimation.none)
            }
        }

    }
    
    @IBAction func onAddButtonTapped(_ sender: AnyObject) {
        self.performSegue(withIdentifier: "MainToShareKeySegue", sender: nil)
    }

    
    @IBAction func onSettingsButtonTapped(_ sender: AnyObject) {
        self.performSegue(withIdentifier: "MainToSettingsSegue", sender: nil)

        
        
//        // Create a custom view controller
//        let ratingVC = DialogLayoutViewController(nibName: "DialogLayout", bundle: nil)
//        
//        // Create the dialog
//        let popup = PopupDialog(viewController: ratingVC, buttonAlignment: .horizontal, transitionStyle: .bounceDown, gestureDismissal: true)
//        
//        // Create first button
//        let buttonOne = CancelButton(title: "CANCEL") {
//        }
//        
//        // Create second button
//        let buttonTwo = DefaultButton(title: "RATE") {
//        }
//        
//        // Add buttons to dialog
//        popup.addButtons([buttonOne, buttonTwo])
//        
//        // Present dialog
//        present(popup, animated: true, completion: nil)
        
        
//        let title = "THIS IS THE DIALOG TITLE"
//        let message = "This is the message section of the popup dialog default view"
//        let image = UIImage(named: "pexels-photo-103290")
//        
//        // Create the dialog
//        let popup = PopupDialog(title: title, message: message, image: image)
//        
//        // Create buttons
//        let buttonOne = CancelButton(title: "CANCEL") {
//            print("You canceled the car dialog.")
//        }
//        
//        let buttonTwo = DefaultButton(title: "ADMIRE CAR") {
//            print("What a beauty!")
//        }
//        
//        let buttonThree = DefaultButton(title: "BUY CAR") {
//            print("Ah, maybe next time :)")
//        }
//        
//        // Add buttons to dialog
//        // Alternatively, you can use popup.addButton(buttonOne)
//        // to add a single button
//        popup.addButtons([buttonOne, buttonTwo, buttonThree])
//        
//        // Present dialog
//        self.present(popup, animated: true, completion: nil)
        
    }
}
