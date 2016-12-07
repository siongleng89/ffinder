//
//  DateTimeUtils.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 15/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class DateTimeUtils{

    public static func getCurrentUnixSecs() -> UInt64{
        return UInt64(NSDate().timeIntervalSince1970)
    }
    
    
    public static func convertUnixTimeToDateTime(_ unixTime:UInt64) -> String{
        
        if unixTime == 0{
            return "never".localized
        }
        
        var dateFormatter = DateFormatter()
        dateFormatter.dateStyle = DateFormatter.Style.short
        let dateformat = dateFormatter.dateFormat
        //M/d/yy
        dateFormatter = DateFormatter()
        dateFormatter.timeStyle = DateFormatter.Style.short
        let timeformat = dateFormatter.dateFormat
        //h:mm a }
        
        let date = Date(timeIntervalSince1970: Double(unixTime))
        let currentDate = Date()
        
        if date.timeIntervalSince1970 > currentDate.timeIntervalSince1970{
            let dayTimePeriodFormatter = DateFormatter()
            dayTimePeriodFormatter.dateFormat = dateformat! + " " + timeformat!
            let dateString = dayTimePeriodFormatter.string(from: date as Date)
            return dateString

        }
        else{
            let dayDiff = daysBetweenDates(date, currentDate)
            
            if dayDiff == 0{
                let dayTimePeriodFormatter = DateFormatter()
                dayTimePeriodFormatter.dateFormat = timeformat!
                let dateString = dayTimePeriodFormatter.string(from: date as Date)
                return dateString
                
            }
            else if dayDiff == 1{
                return "yesterday".localized
                
            }
            else{
                let dayTimePeriodFormatter = DateFormatter()
                dayTimePeriodFormatter.dateFormat = dateformat!
                let dateString = dayTimePeriodFormatter.string(from: date as Date)
                return dateString
                
            }

        }
    }

    
    private static func daysBetweenDates(_ startDate: Date, _ endDate: Date) -> Int
    {
        return Calendar.current.dateComponents([.day], from: startDate, to: endDate).day!
    }
    
    
 
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
