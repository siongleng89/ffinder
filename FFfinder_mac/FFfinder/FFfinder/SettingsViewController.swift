//
//  SettingsViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 28/11/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class SettingsViewController:MyViewController, UITableViewDelegate, UITableViewDataSource {
    
    @IBOutlet weak var tableViewSettings: UITableView!
    private var settingsList:Array<SettingItem>?
    
    override func viewDidLoad() {
        self.title = "settings_activity_title".localized
        
        settingsList = Array<SettingItem>()
        settingsList?.append(SettingItem("add_new_member_manually_title".localized, SettingType.AddNewMember))
         settingsList?.append(SettingItem("known_issues_title".localized, SettingType.KnownIssue))
         settingsList?.append(SettingItem("vip_title".localized, SettingType.Vip))
         settingsList?.append(SettingItem("settings_item_language_title".localized, SettingType.Language))
        
        let yourNibName = UINib(nibName: "NormalTableViewCell", bundle: nil)
        tableViewSettings.register(yourNibName, forCellReuseIdentifier: "NormalTableViewCell")
        
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.settingsList!.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) ->       UITableViewCell{
        
        let cell:NormalTableViewCell! = tableView.dequeueReusableCell(withIdentifier: "NormalTableViewCell", for:indexPath)as! NormalTableViewCell
        let settingItem:SettingItem = self.settingsList![indexPath.row]
        cell.setItemText(settingItem.name)
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        
        let selected:SettingItem = settingsList![indexPath.row]
        if selected.type == SettingType.AddNewMember{
            self.performSegue(withIdentifier: "SettingsToAddMemberSegue", sender: nil)
        }
        else if selected.type == SettingType.Language{
            self.performSegue(withIdentifier: "SettingsToLanguageSegue", sender: nil)
        }
        
    }
    
    
    
    
    private class SettingItem{
        var name:String
        var type:SettingType
        
        init(_ name:String, _ type:SettingType){
            self.name = name
            self.type = type
        }
        
    }
    
    private enum SettingType{
        case AddNewMember
        case KnownIssue
        case Vip
        case Language
    }
    
}
