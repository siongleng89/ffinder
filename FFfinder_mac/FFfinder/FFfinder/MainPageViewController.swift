//
//  MainPageViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 14/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
import PopupDialog
import Firebase

class MainPageViewController: MyViewController, UITableViewDelegate, UITableViewDataSource,
                                FriendTableViewProtocol{
    
    @IBOutlet weak var friendsTableView: UITableView!
    let friendTableCellIdentifier:String = "FriendTableViewCell"
    let tapToAddCellIdentifier:String = "TapToAddTableViewCell"
    var firstTimeRun:Bool? = false
    var selectedFriendModel:FriendModel!
   
    
    override func viewWillAppear(_ animated: Bool) {
        SearchButtonPools.mainPageAppearing()
    }
    
    
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
        let yourNibName2 = UINib(nibName: tapToAddCellIdentifier, bundle: nil)
        friendsTableView.register(yourNibName2, forCellReuseIdentifier: tapToAddCellIdentifier)
        
        self.friendsTableView.tableFooterView = UIView()
        
        NotificationCenter.default.addObserver(self, selector: #selector(friendModelChanged),
                                               name: .friendModelChanged, object: nil)
        
        NotificationCenter.default.addObserver(self,
                                           selector: #selector(onNeedToReloadFriendModel),                                               name: .needToReloadFriendModel, object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(onNeedToRefreshWholeFriendList),                                               name: .needToReloadWholeFriendsList, object: nil)
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(viewShowing),                                               name: .UIApplicationDidBecomeActive, object: nil)
        
        refreshFriendList()
        checkNeedToShowNoFriendReminder()
        setAddFriendReminderAlarm()
     
    }
    
    func viewShowing(){
        checkHasPendingAddUser()
    }
    
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.myModel.friendModels.count + 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) ->       UITableViewCell{
        
        if indexPath.row <= self.myModel.friendModels.count - 1{
            let cell:FriendTableViewCell! = tableView.dequeueReusableCell(withIdentifier: friendTableCellIdentifier, for:indexPath)as! FriendTableViewCell
            let friendModel:FriendModel = self.myModel.friendModels[indexPath.row]
            cell.update(friendModel, self.myModel, self)
            cell.contentView.isUserInteractionEnabled = false
            return cell
        
        }
        else{
            let cell:TapToAddTableViewCell! = tableView.dequeueReusableCell(withIdentifier: tapToAddCellIdentifier, for:indexPath)as! TapToAddTableViewCell
            cell.contentView.isUserInteractionEnabled = false
            cell.update(self)
            return cell
        }
        
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
    
    func onNeedToRefreshWholeFriendList(notification:NSNotification){
        Logs.show("Reloading all rows: onNeedToRefreshWholeFriendList")
        self.myModel.loadAllFriendModels()
        self.myModel.sortFriendModels()
        Threadings.postMainThread {
            self.friendsTableView.reloadData()
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
    
    //refresh friends list from firebase database
    private func refreshFriendList(){
        FirebaseDB.getAllMyLinks(self.myModel.userId!, {
            (results, status) in
            
            if let results = results, status == Status.Success{
                var foundNew:Bool = false
                
                for snapshot:FIRDataSnapshot in results{
                    let userId:String = snapshot.key
                    let name:String? = snapshot.value as! String?
                    
                    if !self.myModel.checkFriendExist(userId){
                        let friendModel:FriendModel = FriendModel()
                        friendModel.userId = userId
                        friendModel.username = name
                        friendModel.save()
                        
                        self.myModel.addFriendModel(friendModel)
                        foundNew = true
                    }
                }
            
                if foundNew{
                    self.myModel.sortFriendModels()
                    self.myModel.commitFriendUserIds()
                    Threadings.postMainThread {
                        self.friendsTableView.reloadData()
                    }
                }
            }
        })
    }
    
    private func checkHasPendingAddUser(){
        if let _ = Vars.pendingUserKey{
            self.performSegue(withIdentifier: "MainToAddMemberSegue", sender: nil)
        }
    }
    
    private func checkNeedToShowNoFriendReminder(){
        if !firstTimeRun!{
            if self.myModel.getNonSelfFriendModelsCount() == 0{
                OverlayBuilder.build().setMessage("no_friend_popup_msg".localized)
                    .setOverlayType(OverlayType.OkOrCancel)
                    .setOnChoices {
                        self.onAddButtonTapped(nil)
                    }.show()
            }
        }
    }
    
    private func setAddFriendReminderAlarm(){
        
    }
    
    func onRequestPickImage(_ friendModel: FriendModel) {
        self.selectedFriendModel = friendModel
        self.performSegue(withIdentifier: "MainToPickImageSegue", sender: nil)
    }
    
    func onRequestChangeName(_ requestingFriend: FriendModel) {
        OverlayBuilder.build().setOverlayType(OverlayType.OkOrCancel)
            .setTitle("edit_name_title".localized).setTextFieldText((requestingFriend.username!))
            .setOnChoices {
                let newName = OverlayBuilder.getTextFieldText()
                
                if let friendModel = self.myModel.getFriendModelById(requestingFriend.userId!){
                    if !StringsHelper.isEmpty(newName)
                        && newName != (friendModel.username!){
                        friendModel.username = newName
                        friendModel.save()
                        
                        requestingFriend.username = newName
                        
                        self.myModel.sortFriendModels()
                        self.myModel.commitFriendUserIds()
                        
                        Threadings.postMainThread {
                            self.friendsTableView.reloadData()
                        }
                        
                        FirebaseDB.editLinkName((self.myModel?.userId)!,
                                                (friendModel.userId)!, newName, nil)
                    }

                }
                
                
            }.show()
    }
    
    func onRequestShowMap(_ friendModel: FriendModel) {
        self.selectedFriendModel = friendModel
        self.performSegue(withIdentifier: "MainToMapSegue", sender: nil)
    }
    
    func onRequestShareKey() {
        onAddButtonTapped(nil)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "MainToPickImageSegue"{
            let theDestination = (segue.destination as! ImagePickerViewController)
            theDestination.friendModel = self.selectedFriendModel
        }
        else if segue.identifier == "MainToMapSegue"{
            let theDestination = (segue.destination as! MapViewController)
            theDestination.friendModel = self.selectedFriendModel
        }
    }
    
    
    
    @IBAction func onAddButtonTapped(_ sender: AnyObject?) {
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
