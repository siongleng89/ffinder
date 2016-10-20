//
//  LocationModel.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import EVReflection


class LocationModel : EVObject, FirebaseModelProtocol {
    
    
    var latitude:String?
    var longitude:String?
    var address:String?
    var timestampLastUpdated:String?
    
    func fromAnyObject(_ value: Any?) {
        if let result = value as? [String:Any]{
            latitude = result["latitude"] as! String?
            longitude = result["longitude"] as! String?
        }
    }
    
    func toAnyObject() -> [AnyHashable : Any] {
        var result = [String:Any]()
        result["latitude"] = latitude
        result["longitude"] = longitude
        
        return result
    }

    
    public func geodecodeCoordinatesIfNeeded(_ callback:@escaping () -> Void){
        if let _ = self.latitude, self.address == nil{
            IOSUtils.geoDecode(self.latitude!, self.longitude!,
                               {(address) in
                                    self.address = address
                                    callback()
            })
        }
        else{
            callback()
        
        }
    
    }
    
    
}
