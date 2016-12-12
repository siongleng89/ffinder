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
    
    private var locationTask: LocationUpdateTask!
    private var locationManager: CLLocationManager?
    
    
    //must check at viewDidAppear since we cannot go to another view controller in viewDidLoad
    override func viewDidAppear(_ animated: Bool) {
        locationManager = CLLocationManager()
        if !IOSUtils.checkLocationServiceEnabledAndRequestIfNeeded(locationManager){
            
            OverlayBuilder.build().setMessage("location_service_change_denied".localized)
                    .setOverlayType(OverlayType.OkOrCancel)
                .setOnChoices({
                     UIApplication.shared.openURL(NSURL(string: UIApplicationOpenSettingsURLString)! as URL)
                        self.checkNeedToShowIntroduction()
                    }, {
                        self.checkNeedToShowIntroduction()
                }).show()
            
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
        self.performSegue(withIdentifier: "LaunchToPagingSegue", sender: nil)
    }
    
    func goToNextScreen(){
        //userId is not empty, can straight go to main page
        if self.myModel.userId != nil{
            
            let vc = self.storyboard?.instantiateViewController(withIdentifier: MainPageViewController.getMyClassName()) as! MainPageViewController
            self.navigationController!.pushViewController(vc, animated: false)
            
            locationTask = LocationUpdateTask(nil, nil, nil)
            Logs.show("token is \(FIRInstanceID.instanceID().token())")
        
        }
        //userId is empty, go to setup page
        else{
            let vc = self.storyboard?.instantiateViewController(withIdentifier: SetupViewController.getMyClassName()) as! SetupViewController
            self.present(vc, animated: false, completion: nil)
        }
        
    }
  
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "LaunchToPagingSegue"{
            (segue.destination as! PagingViewController).type = PagingType.AppsIntro
        }
    }
    
}
