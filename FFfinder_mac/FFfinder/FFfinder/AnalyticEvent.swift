//
//  AnalyticEvent.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 13/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
public enum AnalyticEvent:String{

    case Search_Deduct_Credit = "Search_Deduct_Credit"
    case Search_Failed_No_Credit = "Search_Failed_No_Credit"
    case Search_Using_VIP = "Search_Using_VIP"
    
    case Search_Result = "Search_Result"
    case Search_Timeout = "Search_Timeout"
    case Search_Failed_And_Decide_To_Search_Anyway = "Search_Failed_And_Decide_To_Search_Anyway"
    case Search_Failed_And_Decide_To_Wait_Notification = "Search_Failed_And_Decide_To_Wait_Notification"
    
    case Click_Subscribe = "Click_Subscribe"
    case User_Subscribed = "User_Subscribed"
    case Watch_Ads = "Watch_Ads"
    case No_Ads_Available = "No_Ads_Available"
    case Add_Friend_Failed = "Add_Friend_Failed"
    case Add_Friend_Success = "Add_Friend_Success"


    case Change_Block_User = "Change_Block_User"
    
    case Share_Key_Button_Clicked = "Share_Key_Button_Clicked"
    
    case Change_Language = "Change_Language"
    
    case ChangeScreen = "ChangeScreen"


}
