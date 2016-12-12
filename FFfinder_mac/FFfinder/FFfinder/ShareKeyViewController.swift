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
    
    private func checkKey(){
        
        //check for validity userkey
        if self.myModel.userKey != nil{
            if self.myModel.userKeyGeneratedUnixTime != nil{
                if (DateTimeUtils.getCurrentUnixSecs()
                    - UInt64(myModel.userKeyGeneratedUnixTime!)!) < Constants.KeyExpiredTotalSecs{
                    
                    labelKey.text = myModel.userKey
                    labelExpiredAt.text =  "expired_at_title".localized.format(String(DateTimeUtils.convertUnixTimeToDateTime(
                        UInt64(myModel.userKeyGeneratedUnixTime!)! + Constants.KeyExpiredTotalSecs)))
                        
                    
                        
                    
                    
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
    
    func onHelpTapped(){
        self.performSegue(withIdentifier: "ShareKeyToPagingSegue", sender: nil)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "ShareKeyToPagingSegue"{
            (segue.destination as! PagingViewController).type = PagingType.TutorialShareKey
        }
    }
    
    
}















