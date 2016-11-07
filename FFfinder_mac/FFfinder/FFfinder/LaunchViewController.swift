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

class LaunchViewController: UIViewController, CLLocationManagerDelegate {
    
    @IBOutlet weak var introductionTextView: UITextView!
    @IBOutlet weak var nextButton: UIButton!
    @IBOutlet var mainView: UIView!
    var locationManager:CLLocationManager!
    var myModel:MyModel!;
    var locationUpdater:LocationUpdater!
   
    override func viewDidLoad() {
        super.viewDidLoad()

        print("instanceToken: \(FIRInstanceID.instanceID().token())")
        
        myModel = MyModel()
        myModel.load()
        myModel.loadAllFriendModels()
        
//        locationManager = CLLocationManager()
//        locationManager.delegate = self
//        locationManager.desiredAccuracy = kCLLocationAccuracyBest
//        if #available(iOS 9.0, *) {
//            locationManager.allowsBackgroundLocationUpdates = true
//        } else {
//           // Fallback on earlier versions
//        }
//        locationManager.startUpdatingLocation()

//        locationUpdater = LocationUpdater()
//        locationUpdater.startMonitoring()
        
        
        NotificationCenter.default.addObserver(self, selector: #selector(didBecomeActive), name:NSNotification.Name(rawValue: "UIApplicationDidBecomeActiveNotification"), object: nil)
    }
    
    //must check at viewDidAppear since we cannot go to another view controller in viewDidLoad
    override func viewDidAppear(_ animated: Bool) {
        checkNeedToShowIntroduction();
    }
    
    func didBecomeActive(){
        //locationManager.requestAlwaysAuthorization()
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let location = locations.last
        print("\(location?.coordinate.latitude), \(location?.coordinate.longitude)")
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
        introductionTextView.text = "first_time_msg_content".localized
        mainView.isHidden = false
    }
    
    func goToNextScreen(){
        
        
        let vc = self.storyboard?.instantiateViewController(withIdentifier: SetupViewController.getMyClassName()) as! SetupViewController
        vc.myModel = self.myModel
        self.present(vc, animated: false, completion: nil)
        
        
        
        
        
        
        
        
        
//        //userId is not empty, can straight go to main page
//        if self.myModel.userId != nil{
//            let vc = self.storyboard?.instantiateViewController(withIdentifier: MainPageViewController.getMyClassName()) as! MainPageViewController
//            vc.myModel = self.myModel
//            let navController = UINavigationController(rootViewController: vc)
//            self.present(navController, animated: false, completion: nil)
//
//        }
//        //userId is empty, go to setup page
//        else{
//            let vc = self.storyboard?.instantiateViewController(withIdentifier: SetupViewController.getMyClassName()) as! SetupViewController
//            vc.myModel = self.myModel
//            self.present(vc, animated: false, completion: nil)
//        }
        
    }
    
    @IBAction func onNextButtonTapped(_ sender: AnyObject) {
        Preferences.put(PreferenceType.SeenIntroduction, "1")
        goToNextScreen()
    }
    
    
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
