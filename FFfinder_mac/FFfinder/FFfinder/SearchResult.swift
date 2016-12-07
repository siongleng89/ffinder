//
//  SearchResult.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import EVReflection

enum SearchResult:String, EVRawString{
    
    case Normal = "Normal"
    case ErrorNoLink = "ErrorNoLink"
    case ErrorTimeoutUnknownReason = "ErrorTimeoutUnknownReason"
    case ErrorTimeoutLocationDisabled = "ErrorTimeoutLocationDisabled"
    case ErrorTimeoutNoConnection = "ErrorTimeoutNoConnection"
    case ErrorUserBlocked = "ErrorUserBlocked"
    
    public static func getMessage(_ result:SearchResult) -> String{
        switch result {
        case .ErrorNoLink:
            return "error_no_link_msg".localized
        case .ErrorTimeoutUnknownReason:
            return "error_timeout_msg".localized
        case .ErrorTimeoutLocationDisabled:
            return "error_timeout_msg".localized
        case .ErrorTimeoutNoConnection:
            return "error_timeout_no_connection_msg".localized
        case .ErrorUserBlocked:
            return "error_user_blocked_msg".localized
        default:
            return ""
        }
    }
    
    public func isError() -> Bool{
        return self.rawValue.contains("Error")
    }
    
    
    public func errorTriggeredAutoNotification() -> Bool{
        return self == SearchResult.ErrorTimeoutUnknownReason
    }
   
    
    
    
}
