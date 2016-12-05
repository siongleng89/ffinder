//
//  LanguageViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 30/11/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class LanguageViewController:MyViewController, UITableViewDelegate, UITableViewDataSource{

    @IBOutlet weak var tableViewLanguage: UITableView!
    private var languagesArr:Array<LanguageModel>?
    private var selectedLanguageModel:LanguageModel?
    
    
    override func viewDidLoad() {
        languagesArr = Array<LanguageModel>()
        
        for language:String in LanguageViewController.availableLanguages(true){
            languagesArr?.append(LanguageModel(language,
                                               LanguageViewController.displayNameForLanguage(language)))
        }
    
        
        let yourNibName = UINib(nibName: "NormalTableViewCell", bundle: nil)
        tableViewLanguage.register(yourNibName, forCellReuseIdentifier: "NormalTableViewCell")
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return (languagesArr?.count)!
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell{
        let cell:NormalTableViewCell! = tableView.dequeueReusableCell(withIdentifier: "NormalTableViewCell", for:indexPath)as! NormalTableViewCell
        let languageModel = self.languagesArr?[indexPath.row]
        cell.setItemText((languageModel?.fullName)!)
        cell.enableRadioButton()
        
        if LanguageViewController.currentLanguage() == languageModel?.name{
            cell.changeRadioButtonState(true)
            selectedLanguageModel = languageModel
        }
        
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        
        for cell in tableView.visibleCells as! [NormalTableViewCell] {
            cell.changeRadioButtonState(false)
        }
        
        let cell:NormalTableViewCell = tableView.cellForRow(at: indexPath) as! NormalTableViewCell
        cell.changeRadioButtonState(true)
        selectedLanguageModel = self.languagesArr?[indexPath.row]
    }
    
    private func save(){
        Preferences.put(PreferenceType.Language, (selectedLanguageModel?.name)!)
        OverlayBuilder.build(self)
            .setMessage("language_take_effect_after_restart_toast".localized)
            .setOverlayType(OverlayType.OkOnly)
            .setOnDismiss {
                _ = self.navigationController?.popViewController(animated: true)
            }
            .show()
    }
    
    
    
    @IBAction func onSaveTapped(_ sender: AnyObject) {
        save()
    }
    
    
    public static func availableLanguages(_ excludeBase: Bool = false) -> [String] {
        var availableLanguages = Bundle.main.localizations
        // If excludeBase = true, don't include "Base" in available languages
        if let indexOfBase = availableLanguages.index(of: "Base") , excludeBase == true {
            availableLanguages.remove(at: indexOfBase)
        }
        return availableLanguages
    }
    
    public static func currentLanguage() -> String {
        if let currentLanguage = Preferences.get(PreferenceType.Language) {
            return currentLanguage
        }
        return defaultLanguage()
    }
    
    
    public static func defaultLanguage() -> String {
        var defaultLanguage: String = String()
        guard let preferredLanguage = Bundle.main.preferredLocalizations.first else {
            return "en"
        }
        let availableLanguages: [String] = self.availableLanguages()
        if (availableLanguages.contains(preferredLanguage)) {
            defaultLanguage = preferredLanguage
        }
        else {
            defaultLanguage = "en"
        }
        return defaultLanguage
    }
    
    public static func displayNameForLanguage(_ language: String) -> String {
        let locale : NSLocale = NSLocale(localeIdentifier: currentLanguage())
        if let displayName = locale.displayName(forKey: NSLocale.Key.identifier, value: language) {
            return displayName
        }
        return String()
    }
    
    
    private class LanguageModel{
        var name:String
        var fullName:String
        
        init(_ name:String, _ fullName:String){
            self.name = name
            self.fullName = fullName
        }
        
    }
    
    
    
}
