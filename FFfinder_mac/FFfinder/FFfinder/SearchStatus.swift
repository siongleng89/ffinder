//
//  SearchStatus.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import EVReflection
enum SearchStatus:String, EVRawString{
    
    case Starting = "Starting"
    case CheckingData = "CheckingData"
    case WaitingUserRespond = "WaitingUserRespond"
    case WaitingUserLocation = "WaitingUserLocation"
    case End = "End"
    
    public static func getMessage(_ status:SearchStatus) -> String{
        switch status {
            case .Starting:
                return "search_status_starting_msg".localized
            case .CheckingData:
                return "search_status_checking_data_msg".localized
            case .WaitingUserRespond:
                return "search_status_waiting_user_respond_msg".localized
            case .WaitingUserLocation:
                return "search_status_waiting_user_location_msg".localized
            default:
                return ""
        }
    }
    
    
}
