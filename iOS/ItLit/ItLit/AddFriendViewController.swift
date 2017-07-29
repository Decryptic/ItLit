//
//  AddFriendViewController.swift
//  itslitt
//
//  Created by Gage Swenson on 5/17/17.
//  Copyright © 2017 juicyasf. All rights reserved.
//

import Foundation
import UIKit

class AddFriendViewController: UIViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.etName.text = Const.ecName ?? ""
        self.etFname.text = Const.ecFname ?? ""
    }
    
    @IBOutlet weak var etName: UITextField!
    @IBOutlet weak var etFname: UITextField!
    
    func errorless(_ name: String, _ fname: String) -> String? {
        if name.characters.count > 30 {
            return "Name may not exceed 30 characters"
        }
        if fname == "" {
            return "Please try a phone number"
        }
        if fname.characters.count > 13 || fname.characters.count < 10 {
            return "Phone number must be 10 to 13 digits"
        }
        return nil
    }
    
    @IBAction func addFriend(sender: UIButton) {
        let name = self.etName?.text ?? ""
        let fname = Const.phonify(number: self.etFname?.text ?? "")
        
        if let err = errorless(name, fname) {
            self.view.makeToast(err, duration: Const.tt(), position: .top)
            return
        }
        
        // If they change the number, you gotta delfriend before you setfriend
        var deleteOld = false
        var oldUname: String?
        if let index = Const.ecOldIndex {
            oldUname = Const.friends[index]["fname"] as! String
            deleteOld = oldUname != fname
        }
        
        if deleteOld {
            let friend: [String: Any] = ["name": name, "fname": oldUname!, "lit": false]
            let json: [String: Any] = ["uname": Const.uname, "passwd": Const.passwd, "friend": friend]
            
            let auth = try? JSONSerialization.data(withJSONObject: json)
            let url = URL(string: Const.server("delfriend"))
            var request = URLRequest(url: url!)
            request.httpMethod = "POST"
            request.httpBody = auth
            request.addValue("application/json", forHTTPHeaderField: "Content-Type")
            request.addValue("application/json", forHTTPHeaderField: "Accept")
            
            let task = URLSession.shared.dataTask(with: request) { data, resp, error in
                guard let data = data, error == nil else {
                    let err = error?.localizedDescription ?? Const.ptal
                    print("error addFriend(): deleteOld " + err)
                    return
                }
                let response = try? JSONSerialization.jsonObject(with: data)
                if let response = response as? [String: Any] {
                    if let err = response["error"] {
                        print("error addFriend(): deleteOld " + (err as! String))
                    }
                    else {
                        Const.friends.remove(at: Const.ecOldIndex!)
                    }
                } else {
                    print("error addFreind(): deleteOld response was not json")
                }
            }
            task.resume()
        }
        
        let friend: [String: Any] = ["name": name, "fname": fname, "lit": false]
        let json: [String: Any] = ["uname": Const.uname, "passwd": Const.passwd, "friend": friend]
        
        let auth = try? JSONSerialization.data(withJSONObject: json)
        let url = URL(string: Const.server("setfriend"))
        var request = URLRequest(url: url!)
        request.httpMethod = "POST"
        request.httpBody = auth
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let task = URLSession.shared.dataTask(with: request) { data, resp, error in
            guard let data = data, error == nil else {
                DispatchQueue.main.async {
                    let err = error?.localizedDescription ?? Const.ptal
                    self.view.makeToast(err, duration: Const.tt(), position: .top)
                }
                return
            }
            let response = try? JSONSerialization.jsonObject(with: data)
            if let response = response as? [String: Any] {
                if let err = response["error"] {
                    DispatchQueue.main.async {
                        self.view.makeToast(err as! String, duration: Const.tt(), position: .top)
                    }
                }
                else {
                    Const.friends.append(friend)
                }
            } else {
                print("error addFriend(): response was not json")
                DispatchQueue.main.async {
                    self.view.makeToast(Const.ptal, duration: Const.tt(), position: .top)
                }
            }
            Const.ecName = nil
            Const.ecFname = nil
            Const.ecOldIndex = nil
            self.dismiss(animated: true, completion: nil)
        }
        task.resume()
    }
    
    @IBAction func cancel(sender: UIButton) {
        Const.ecName = nil
        Const.ecFname = nil
        Const.ecOldIndex = nil
        self.dismiss(animated: true, completion: nil)
    }
    
    @IBAction func resignKeyboard(sender: UITextField) {
        sender.resignFirstResponder()
    }
}
