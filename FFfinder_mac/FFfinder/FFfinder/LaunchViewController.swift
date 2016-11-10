//
//  LaunchViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 11/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
import CoreLocation
import Firebase

class LaunchViewController: MyViewController, CLLocationManagerDelegate {
    
    @IBOutlet weak var layoutNext: UIView!
    @IBOutlet weak var layoutIntro: UIView!
    @IBOutlet weak var layoutWelcome: UIView!
    @IBOutlet weak var imageViewNextIcon: UIImageView!
    @IBOutlet weak var imageViewSplash: UIImageView!
    @IBOutlet weak var labelIntro: LocalizedLabel!
    @IBOutlet weak var constraintHeightLayoutIntro: NSLayoutConstraint!
    private var locationManager: CLLocationManager?
    
    //must check at viewDidAppear since we cannot go to another view controller in viewDidLoad
    override func viewDidAppear(_ animated: Bool) {
        locationManager = CLLocationManager()
        if !IOSUtils.checkLocationServiceEnabledAndRequestIfNeeded(locationManager){
            showConfirmDialog(title: "", message: "some_permission_denied_toast_msg".localized, positiveText: "ok", positiveToRun: {
                    UIApplication.shared.openURL(NSURL(string: UIApplicationOpenSettingsURLString)! as URL)
                    self.checkNeedToShowIntroduction();
                }, negativeToRun: {
                    self.checkNeedToShowIntroduction();
            })
            
        }
        else{
            checkNeedToShowIntroduction();
        }
    }
   
    func checkNeedToShowIntroduction(){
        if let seenIntroduction = Preferences.get(PreferenceType.SeenIntroduction){
            if seenIntroduction == "1"{
                goToNextScreen();
            }
            else{
                showIntroduction();
            }
        }
        else{
            showIntroduction();
        }
    }
    
    func showIntroduction(){
        let toY:CGFloat =  layoutWelcome.bounds.height +
                            layoutWelcome.frame.origin.y +
                            self.imageViewSplash.bounds.height / 2 + 15
        
        AnimateBuilder.build(self.imageViewSplash)
            .setAnimateType(AnimateType.MoveToY).setValue(toY)
            .setDurationMs(1000).setFinishCallback ({
                AnimateBuilder.fadeIn(self.layoutWelcome, speed:AnimateBuilder.Slow, {
                    AnimateBuilder.fadeIn(self.layoutIntro, speed:AnimateBuilder.Slow, {
                        AnimateBuilder.fadeIn(self.layoutNext, speed:AnimateBuilder.Fast, {
                            AnimateBuilder.build(self.imageViewNextIcon)
                                .setAnimateType(AnimateType.MoveByX).setDurationMs(400)
                                .setRepeat(true).setValue(3).start()
                        })
                    })
                })
            }).start()
        
         constraintHeightLayoutIntro.constant = labelIntro.frame.height + 40
        
        
        let gesture = UITapGestureRecognizer(target: self, action: #selector(LaunchViewController.onNextLayoutTapped(_:)))
        self.layoutNext.addGestureRecognizer(gesture)
    }
    
    func goToNextScreen(){
        //userId is not empty, can straight go to main page
        if self.myModel.userId != nil{
            let vc = self.storyboard?.instantiateViewController(withIdentifier: MainPageViewController.getMyClassName()) as! MainPageViewController
            let navController = UINavigationController(rootViewController: vc)
            self.present(navController, animated: false, completion: nil)

        }
        //userId is empty, go to setup page
        else{
            let vc = self.storyboard?.instantiateViewController(withIdentifier: SetupViewController.getMyClassName()) as! SetupViewController
            self.present(vc, animated: false, completion: nil)
        }
        
    }
  
    
    func onNextLayoutTapped(_ sender:UITapGestureRecognizer){
        Preferences.put(PreferenceType.SeenIntroduction, "1")
        let vc = self.storyboard?.instantiateViewController(withIdentifier: SetupViewController.getMyClassName()) as! SetupViewController
        self.present(vc, animated: false, completion: nil)
    }
    
}
