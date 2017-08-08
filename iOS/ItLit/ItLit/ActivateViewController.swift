//
//  ActivateViewController.swift
//  itslitt
//
//  Created by Gage Swenson on 5/7/17.
//  Copyright © 2017 juicyasf. All rights reserved.
//

import Foundation
import UIKit

class ActivateViewController : UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    @IBOutlet weak var etCode: UITextField!
    
    @IBAction func activate(sender: UIButton) {
        let code = etCode.text ?? ""
        
        if code.characters.count != 5 {
            self.view.makeToast("Please try a 5 digit code", duration: Const.tt(), position: .top)
            return
        }
        guard Int(code) != nil else {
            self.view.makeToast("Code must be all digits")
            return
        }
        
        let json: [String: Any] = ["uname": Const.uname, "passwd": Const.passwd, "code": Int(code)!]
        
        let auth = try? JSONSerialization.data(withJSONObject: json)
        let url = URL(string: Const.server("activate"))
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
                    Const.activateAttempts += 1
                    if Const.activateAttempts >= 3 {
                        DispatchQueue.main.async {
                            self.view.makeToast(Const.ptal, duration: Const.tt(), position: .top)
                        }
                        sleep(2)
                        DispatchQueue.main.async {
                            self.dismiss(animated: true, completion: nil)
                        }
                    }
                    else {
                        DispatchQueue.main.async {
                            self.view.makeToast(err as! String, duration: Const.tt(), position: .top)
                        }
                    }
                }
                else {
                    DispatchQueue.main.async {
                        self.view.makeToast("Welcome, please log in", duration: Const.tt(), position: .top)
                    }
                    sleep(2)
                    DispatchQueue.main.async {
                        self.dismiss(animated: true, completion: nil)
                    }
                }
            } else {
                print("error activate(): response was not json")
                DispatchQueue.main.async {
                    self.view.makeToast(Const.ptal, duration: Const.tt(), position: .top)
                }
            }
        }
        task.resume()
    }
    
    @IBAction func cancel(sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
    }
    
    @IBAction func resignKeyboard(sender: UITextField) {
        sender.resignFirstResponder()
    }
}
