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
    @IBOutlet weak var btnShare: UIButton!
    @IBOutlet weak var btnRefresh: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        
        labelMessage.text = "your_key_title".localized
        btnShare.setTitle("Share", for: .normal)
        btnRefresh.setTitle("Refresh", for: .normal)

        checkKey()
    }
    
    private func checkKey(){
        
        //check for validity userkey
        if self.myModel.userKey != nil{
            if self.myModel.userKeyGeneratedUnixTime != nil{
                if (DateTimeUtils.getCurrentUnixSecs()
                    - UInt64(myModel.userKeyGeneratedUnixTime!)!) < Constants.KeyExpiredTotalSecs{
                    
                    labelKey.text = myModel.userKey
                    labelExpiredAt.text = DateTimeUtils.convertUnixTimeToDateTime(
                        UInt64(myModel.userKeyGeneratedUnixTime!)! + Constants.KeyExpiredTotalSecs)
                    
                    //no need for regenerate key as it is still valid
                    return
                
                }
            }
        }
        
        startGenerateNewKey()
    }
    
    
    //generate new key
    private func startGenerateNewKey(){
        showLoading(Message: "regenerating_key_msg".localized)
        
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
                self.hideLoading()
                self.checkKey()
            }
        }
    }
    
    
    @IBAction func onBtnRefreshTapped(_ sender: AnyObject) {
        showConfirmDialog(message: "regen_key_confirm_msg".localized, positiveToRun: {
            self.startGenerateNewKey()
        })
    }
    
    @IBAction func onBtnShareTapped(_ sender: AnyObject) {
        
        let shareContent = "share_msg".localized
        
        let activityViewController = UIActivityViewController(activityItems: [shareContent as NSString], applicationActivities: nil)
        present(activityViewController, animated: true, completion: nil)
        
    }
    
}















