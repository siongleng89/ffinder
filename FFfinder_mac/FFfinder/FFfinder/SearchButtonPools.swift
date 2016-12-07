//
//  SearchButtonPools.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 6/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation


class SearchButtonPools{
    
    private static var pool:[String: SearchButton] = [String: SearchButton]()
    
    public static func getSearchButton(_ id:String) -> SearchButton{
        
        if let value:SearchButton = pool[id]{
            //button already exist
            return value
        }
        else{
            let newView = SearchButton(frame: CGRect(x: 0, y: 0, width: 80, height: 120))
            pool[id] = newView
            Logs.show("Creating new button search view")
            return newView
        }
    }
    
    
    
}
