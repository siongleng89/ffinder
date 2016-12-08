//
//  NotificationExtension.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 16/10/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
extension Notification.Name {
    static let friendModelChanged = Notification.Name("friendModelChanged")
    static let needToReloadFriendModel = Notification.Name("needToReloadFriendModel")
    static let needToReloadWholeFriendsList = Notification.Name("needToReloadWholeFriendsList")
}
