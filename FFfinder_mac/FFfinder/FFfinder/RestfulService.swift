//
//  RestfulService.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 12/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class RestfulService{
    
    
    public static func getToken(_ userId:String, _ callback: @escaping (String?, Status) -> Void){
        var dict = [String:String]()
        dict["userId"] = userId
        callApi("login_user", dict, callback);
    }
    
    
    public static func usePromoCode(_ userId:String, _ promoCode:String,
                                _ callback: @escaping (String?, Status) -> Void){
        var dict = [String:String]()
        dict["userId"] = userId
        dict["promoCode"] = promoCode
        callApi("use_promo", dict, callback);
    }
    
   
    
    private static func callApi(_ name:String, _ dict:[String: String],
                                _ callback: @escaping (String?, Status) -> Void){
        var dict = dict
        
        dict["restSecret"] = Constants.RestfulKey;
        
        let myUrl = URL(string: "\(Constants.RestfulUrl)/\(name)")
        var request = URLRequest(url:myUrl!)
        request.httpMethod = "POST"
        
        // Compose a query string
        var postString:String = ""
        for (key, value) in dict{
            postString += "\(key)=\(value)&"
        }
        if dict.count > 0 {
            postString = postString.substring(to: postString.index(before: postString.endIndex))
        }
        
        request.httpBody = postString.data(using: String.Encoding.utf8)
        
        let task = URLSession.shared.dataTask(with: request) {
            data, response, error in
            
            if error != nil
            {
                callback(error?.localizedDescription, Status.Failed)
                return
            }
            
            let responseData = String(data: data!, encoding: String.Encoding.utf8)
            callback(responseData, Status.Success)
        }
        
        task.resume()
    }
    
}
