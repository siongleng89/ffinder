//
//  IOSUtils.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 15/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
import CoreLocation

class IOSUtils{

    public static func getUsername() -> String{
        
        let deviceName = UIDevice.current.name
        let expression = "^(?:iPhone|phone|iPad|iPod)\\s+(?:de\\s+)?(?:[1-9]?S?\\s+)?|(\\S+?)(?:['']?s)?(?:\\s+(?:iPhone|phone|iPad|iPod)\\s+(?:[1-9]?S?\\s+)?)?$|(\\S+?)(?:['']?的)?(?:\\s*(?:iPhone|phone|iPad|iPod))?$|(\\S+)\\s+"
        
        var username = deviceName
        
        do {
            let regex = try NSRegularExpression(pattern: expression, options: .caseInsensitive)
            let matches = regex.matches(in: deviceName as String,
                                                options: NSRegularExpression.MatchingOptions.init(rawValue: 0),
                                                range: NSMakeRange(0, deviceName.characters.count))
            let rangeNotFound = NSMakeRange(NSNotFound, 0)
            
            var nameParts = [String]()
            for result in matches {
                for i in 1..<result.numberOfRanges {
                    if !NSEqualRanges(result.rangeAt(i), rangeNotFound) {
                        nameParts.append((deviceName as NSString).substring(with: result.rangeAt(i)).capitalized)
                    }
                }
            }
            
            if nameParts.count > 0 {
                username = nameParts.joined(separator: " ")
            }
        }
        catch {
        }
        username = username.replacingOccurrences(of: "\'s", with: "")
        return username
    
    }
    
    
    public static func geoDecode(_ latitude:String, _ longitude:String,
                                 _ callback:@escaping (String?) -> Void){
        
        Threadings.runInBackground {
            var completeCount = 0
            
            var address1:String?
            var address2:String?
            
            geoDecodeByGeocoder(latitude, longitude,
                                {(address) in
                                    if let address = address{
                                        address1 = address
                                    }
                                    completeCount += 1
            })
            
            geoDecodeByGoogleApi(latitude, longitude,
                                {(address) in
                                    if let address = address{
                                        address2 = address
                                    }
                                    completeCount += 1
            })
            
            while completeCount < 2{
                Threadings.sleep(500)
            }
            
            //compare see which address give more detailed address
            
            if address1 != nil && address2 != nil{
                if address1!.characters.count > address2!.characters.count{
                    callback(address1)
                }
                else{
                    callback(address2)
                }
            }
            else if address1 == nil && address2 != nil{
                callback(address2)
            }
            else if address1 != nil && address2 == nil{
                callback(address1)
            }
            else{
                callback(nil)
            }
            
        }
    }
    
    private static func geoDecodeByGeocoder(_ latitude:String, _ longitude:String,
                                             _ callback:@escaping (String?) -> Void){
        let reverseGeocoder = CLGeocoder()
        let location:CLLocation = CLLocation(latitude: CLLocationDegrees(latitude)!, longitude: CLLocationDegrees(longitude)!)
        
        reverseGeocoder.reverseGeocodeLocation(location) { (placemarks, error) in
            if let placemark = placemarks?.first{
                let lines = placemark.addressDictionary?["FormattedAddressLines"]
                let addressString = (lines as! NSArray).componentsJoined(by: ", ")
                callback("\(addressString)")
            }
            else{
                callback(nil)
            }
        }
    }
    
    
    private static func geoDecodeByGoogleApi(_ latitude:String, _ longitude:String,
                                             _ callback:@escaping (String?) -> Void){
    
        var lookUpString = "http://maps.googleapis.com/maps/api/geocode/json?latlng=\(latitude),\(longitude)&amp;sensor=true"
        
        lookUpString = lookUpString.replacingOccurrences(of: " ", with: "+")
        
        do{
            let jsonResponse = try Data(contentsOf: NSURL(string: lookUpString)! as URL)
            var jsonDict = try! JSONSerialization.jsonObject(with: jsonResponse,
                                                             options: .allowFragments)
                                                                as! [String:Any]
            
            
            let status = jsonDict["status"] as! String
            
            if status.uppercased() == "OK"{
                if let results = jsonDict["results"] as? NSArray{
                    if let firstResult = results[0] as? NSDictionary {
                        if let address = firstResult["formatted_address"] as? String{
                            callback(address)
                            return
                        }
                    }
                }
            }
        }
        catch{
        }
        
        callback(nil)
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}
