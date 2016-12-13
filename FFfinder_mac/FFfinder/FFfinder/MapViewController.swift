//
//  MapViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 9/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import GoogleMaps
import MapKit

class MapViewController:MyViewController, GMSMapViewDelegate{
    
    @IBOutlet weak var viewMap: GMSMapView!
    @IBOutlet weak var viewPanorama: GMSPanoramaView!
    
    @IBOutlet weak var btnDirections: TabButton!
    @IBOutlet weak var buttonStreetView: TabButton!
    
    @IBOutlet weak var buttonGps: TabButton!
    
    var friendModel:FriendModel?
    var friendLatitude:Double!
    var friendLongitude:Double!
    
    override func viewDidLoad() {
        self.title = "map_activity_title".localized
        
        
        friendLatitude = Double((friendModel?.locationModel?.latitude)!)
        friendLongitude = Double((friendModel?.locationModel?.longitude)!)
        
        let camera = GMSCameraPosition.camera(withLatitude: friendLatitude, longitude: friendLongitude, zoom: 15)
        self.viewMap.camera = camera
        self.viewMap.delegate = self
        
        
        viewPanorama.moveNearCoordinate(CLLocationCoordinate2D(
                                    latitude: friendLatitude, longitude: friendLongitude))
        
        //        map = GMSMapView.map(withFrame: self.viewMap.bounds, camera:camera)
        //        self.viewMap.addSubview(map)
        //
        //
        //        let marker = GMSMarker()
        //        marker.position = CLLocationCoordinate2DMake(latitude!, longitude!)
        //        marker.title = "Sydney"
        //        marker.snippet = "Australia"
        //        marker.map = map
        
        self.buttonStreetView?.addGestureRecognizer(UITapGestureRecognizer(target: self,
                                                                           action: #selector(onStreetViewTapped)))
//        self.btnDirections?.addGestureRecognizer(UITapGestureRecognizer(target: self,
//                                                                        action: #selector(onDirectionsTapped)))
        self.buttonGps?.addGestureRecognizer(UITapGestureRecognizer(target: self,
                                                                        action: #selector(onGpsTapped)))
        
        onStreetViewTapped()
        
        showMarker(friendLatitude, friendLongitude)
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        Analytics.setScreen(name: "ActivityMap")
    }
    
    func showMarker(_ latitude:Double, _ longitude:Double){
    
        let position = CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
        let marker = GMSMarker(position: position)
        marker.icon = GMSMarker.markerImage(with: UIColor.colorPrimaryDark())
        marker.title = "2"
        marker.snippet = "1"
        //marker.iconView = markerView
        marker.map = viewMap
        
    }
    
    
    
    func onStreetViewTapped(){
        buttonStreetView.selected = true
       // btnDirections.selected = false
    }
    
    func onDirectionsTapped(){
        buttonStreetView.selected = false
       // btnDirections.selected = true
    }
    
    func onGpsTapped(){
        let coordinate = CLLocationCoordinate2DMake(friendLatitude,friendLongitude)
        let mapItem = MKMapItem(placemark: MKPlacemark(coordinate: coordinate, addressDictionary:nil))
        mapItem.name = self.friendModel?.username
        mapItem.openInMaps(launchOptions: [MKLaunchOptionsDirectionsModeKey : MKLaunchOptionsDirectionsModeDriving])
        
    }
   
    
    
    func mapView(_ mapView: GMSMapView, markerInfoWindow marker: GMSMarker) -> UIView? {
        let infoWindow = Bundle.main.loadNibNamed("CustomInfoWindow", owner: self, options: nil)?.first! as! CustomInfoWindow
        
        infoWindow.layer.cornerRadius = 6.0
        infoWindow.layer.borderWidth = 2.0
        infoWindow.layer.borderColor = UIColor.colorPrimaryDark().cgColor

        
        //infoWindow.labelAddress.text = "test"
        for subview in infoWindow.subviews as [UIView]
        {
            if subview.tag == 0{
                for subview2 in subview.subviews as [UIView]{
                    if subview2.tag == 10
                    {
                        if let label = subview2 as? UILabel {
                            label.text = friendModel?.username
                        }
                    }
                    else if subview2.tag == 11
                    {
                        if let label = subview2 as? UILabel {
                            label.text = DateTimeUtils.convertUnixTimeToDateTime(UInt64((friendModel?.locationModel?.timestampLastUpdated)!)!)
                        }
                    }
                }
            }
            
            
            else if subview.tag == 12
            {
                if let label = subview as? UILabel {
                    label.text = friendModel?.locationModel?.address
                }
            }
        
        }
        
        return infoWindow
    }
        
    
    
    
    
    
    
    
    
}
