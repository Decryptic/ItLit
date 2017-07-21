//
//  Constants.swift
//  itslitt
//
//  Created by Gage Swenson on 5/14/17.
//  Copyright © 2017 juicyasf. All rights reserved.
//
//
//  5.5" display landscape: 1242 x 2208 px
//  12.9" display landscpe: 2048 x 2732 px
//

import Foundation
import UIKit
import CoreLocation

class Const {
    public static func server(_ ext: String) -> String {
        return "https://www.itlit.io/" + ext
    }
    
    //toast time
    public static func tt() -> Double {
        return 2.0
    }
    
    public static var uname: String = "0000000000"
    public static var passwd: String = "0000000000000000000000000000000000000000000000000000000000000000"
    public static var lit: Bool = false
    
    public static func selfieDir() -> String {
        let dir = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0]
        let url = URL(fileURLWithPath: dir)
        let fil = url.appendingPathComponent("littselfie.png")
        return fil.absoluteString
    }
    
    public static var contacts: [[String: Any]] = []
    public static var friends: [[String: Any]] = []
    public static var faces: [String: UIImage?] = [:]
    
    // possible initializers to addfriendvc
    public static var ecName: String?
    public static var ecFname: String?
    public static var ecOldIndex: Int?
    
    public static var locationManager: CLLocationManager?
    public static var lastLocation: CLLocation?
    
    public static func phonify(number: String) -> String {
        var result = ""
        for c in number.characters {
            if "0123456789".contains(String(c)) {
                result.append(c)
            }
        }
        if result.characters.first == "1" {
            result.remove(at: result.startIndex)
        }
        return result
    }
    
    public static func quickSort() {
        if friends.count > 0 {
            friends = quickSortAux(friends)
        }
    }
    
    public static func quickSortAux(_ frens: [[String: Any]]) -> [[String: Any]] {
        if frens.count <= 1 {
            return frens
        }
        else {
            let mid = frens.count / 2
            var pre: [[String: Any]] = []
            var post: [[String: Any]] = []
            var i = 0
            let midName = (frens[mid]["name"] as! String).lowercased()
            while i < frens.count {
                if i != mid {
                    let elemName = (frens[i]["name"] as! String).lowercased()
                    if elemName < midName {
                        pre.append(frens[i])
                    }
                    else {
                        post.append(frens[i])
                    }
                }
                i = i + 1
            }
            return quickSortAux(pre) + [frens[mid]] + quickSortAux(post)
        }
    }
}
