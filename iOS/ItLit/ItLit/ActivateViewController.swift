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
        
        let json = ["uname": Const.uname, "passwd": Const.passwd, code: code]
        
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
                    let err = error?.localizedDescription ?? "Please try again later"
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
                    DispatchQueue.main.async {
                        self.view.makeToast("Welcome, please log in", duration: Const.tt(), position: .top)
                    }
                    self.dismiss(animated: true, completion: nil)
                }
            } else {
                print("Activate: response was not json")
                DispatchQueue.main.async {
                    self.view.makeToast("Please try again later", duration: Const.tt(), position: .top)
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
