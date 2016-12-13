//
//  ShareKeyViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 14/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit

class ShareKeyViewController: MyViewController {

    @IBOutlet weak var labelMessage: AutoHeightLabel!
    @IBOutlet weak var labelKey: UILabel!
    @IBOutlet weak var labelExpiredAt: UILabel!
    @IBOutlet weak var imageViewHelp: UIImageView!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = "share_key_activity_title".localized
        
        // Do any additional setup after loading the view.
        checkKey()
    
        self.imageViewHelp?.isUserInteractionEnabled = true
        self.imageViewHelp?.addGestureRecognizer(UITapGestureRecognizer(target: self,
                                                                    action: #selector(onHelpTapped)))
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        Analytics.setScreen(name: "ActivityShareKey")
    }
    
    private func checkKey(){
        
        //check for validity userkey
        if self.myModel.userKey != nil{
            if self.myModel.userKeyGeneratedUnixTime != nil{
                if (DateTimeUtils.getCurrentUnixSecs()
                    - UInt64(myModel.userKeyGeneratedUnixTime!)!) < Constants.KeyExpiredTotalSecs{
                    
                    labelKey.text = myModel.userKey
                    labelExpiredAt.text =  "expired_at_title".localized.format(String(DateTimeUtils.convertUnixTimeToDateTime(
                        UInt64(myModel.userKeyGeneratedUnixTime!)! + Constants.KeyExpiredTotalSecs)))
                    
                    self.checkShouldShowTutorial()
                    
                    
                    //no need for regenerate key as it is still valid
                    return
                
                }
            }
        }
        
        startGenerateNewKey()
    }
    
    
    //generate new key
    private func startGenerateNewKey(){
        
        OverlayBuilder.build().setOverlayType(OverlayType.Loading)
                .setMessage("regenerating_key_msg".localized)
                .show()
        
        
        Threadings.runInBackground {
            var success:Bool = false
            while !success {
                var finish:Bool = false;
                let key:String = StringsHelper.generateUserKey()
                FirebaseDB.tryInsertKey(self.myModel.userId!, IOSUtils.getUsername(), key,
                                        {(successInsertKey, status) in
                                            
                                            //successfully insert the new key
                                            if status == Status.Success && successInsertKey{
                                                self.myModel.userKey = key
                                                self.myModel.resetUserKeyGeneratedTime()
                                                self.myModel.save()
                                                success = true
                                                finish = true
                                            }
                })
                
                while !finish{
                    Threadings.sleep(500)
                }
            }
            
            Threadings.postMainThread {
                OverlayBuilder.forceCloseLoading()
                self.checkKey()
            }
        }
    }
    
    private func checkShouldShowTutorial(){
        let seen:String? = Preferences.get(PreferenceType.SeenShareKeyTutorial)
        if seen == nil || (seen != nil && seen != "1"){
            
            OverlayBuilder.build().setOverlayType(OverlayType.OkOrCancel)
                .setMessage("first_time_see_passcode".localized)
                .setOnChoices ({
                    Preferences.put(PreferenceType.SeenShareKeyTutorial, "1")
                    self.performSegue(withIdentifier: "ShareKeyToPagingSegue", sender: nil)
                    }, {
                        Preferences.put(PreferenceType.SeenShareKeyTutorial, "1")
                })
                .setVc(self)
                .show()
        }
    }
    
    
    @IBAction func onBtnRefreshTapped(_ sender: AnyObject) {
        
        OverlayBuilder.build().setMessage("regen_key_confirm_msg".localized)
                .setOverlayType(OverlayType.OkOrCancel)
            .setOnChoices({
                OverlayBuilder.forceCloseAllOverlays()
                self.startGenerateNewKey()
            }).show()
    }
    
    @IBAction func onBtnShareTapped(_ sender: AnyObject) {
        
        var shareContent = "share_msg".localized.format(self.myModel.userKey!, self.myModel.userKey!)
        shareContent = shareContent.replacingOccurrences(of: "\n", with: "<br/>")
        
        let activityViewController = UIActivityViewController(activityItems: [shareContent as NSString], applicationActivities: nil)
        activityViewController.popoverPresentationController?.sourceView = self.view
        
        present(activityViewController, animated: true, completion: nil)
        
        Analytics.trackEvent(AnalyticEvent.Share_Key_Button_Clicked)
        
    }
    
    func onHelpTapped(){
        self.performSegue(withIdentifier: "ShareKeyToPagingSegue", sender: nil)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "ShareKeyToPagingSegue"{
            (segue.destination as! PagingViewController).type = PagingType.TutorialShareKey
        }
    }
    
    
}















