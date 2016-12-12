//
//  TableViewProtocol.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 8/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
protocol FriendTableViewProtocol {
     func onRequestPickImage(_ friendModel:FriendModel)
     func onRequestChangeName(_ friendModel:FriendModel)
     func onRequestShowMap(_ friendModel:FriendModel)
    func onRequestShareKey()
}
