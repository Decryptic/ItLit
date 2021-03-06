//
//  LoginViewController.swift
//  itslitt
//
//  Created by Gage Swenson on 5/7/17.
//  Copyright © 2017 juicyasf. All rights reserved.
//

import Foundation
import UIKit

class LoginViewController: UIViewController {
    override func viewWillAppear(_ animated: Bool) {
        if  let uname  = UserDefaults.standard.string(forKey: "uname") {
            etPhone.text = uname
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        
        if  let uname  = UserDefaults.standard.string(forKey: "uname"),
            let passwd = UserDefaults.standard.string(forKey: "passwd") {
            etPhone.text = uname
            loginAux(uname: uname, passHash: passwd)
        }
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
    
    @IBOutlet weak var etPhone: UITextField!
    @IBOutlet weak var etPasswd: UITextField!
    
    func errorless(username: String, password: String) -> String? {
        if username == "" {
            return "Please try a phone number"
        }
        if password == "" {
            return "Please try a password"
        }
        if username.characters.count > 13 || username.characters.count < 10 {
            return "Phone number must be 10 to 13 digits"
        }
        return nil
    }
    
    @IBAction func register(sender: UIButton) {
        let uname = Const.phonify(number: etPhone.text ?? "")
        let passwd = etPasswd.text ?? ""
        let passHash: String = Const.sha256(input: passwd)
        let json = ["uname": uname, "passwd": passHash]
        
        if let err = errorless(username: uname, password: passwd) {
            self.view.makeToast(err, duration: Const.tt(), position: .top)
            return
        }
        
        Const.activateAttempts = 0
        
        var registrationAttempts = UserDefaults.standard.integer(forKey: "registrationAttempts")
        let registrationTime = UserDefaults.standard.double(forKey: "lastRegistrationAttempt")
        if registrationAttempts >= 3 {
            if Date().timeIntervalSince1970 - registrationTime > 24 * 60 * 60 {
                UserDefaults.standard.set(0, forKey: "registrationAttempts")
                registrationAttempts = 0
            }
            else {
                self.view.makeToast("Only 3 registrations per day", duration: Const.tt(), position: .top)
                return
            }
        }
        UserDefaults.standard.set(registrationAttempts+1, forKey: "registrationAttempts")
        UserDefaults.standard.set(Date().timeIntervalSince1970, forKey: "lastRegistrationAttempt")
        
        let auth = try? JSONSerialization.data(withJSONObject: json)
        let url = URL(string: Const.server("register"))
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
                        // milestone
                    }
                }
                else {
                    Const.uname = uname
                    Const.passwd = passHash
                    
                    DispatchQueue.main.async {
                        let viewController = self.storyboard?.instantiateViewController(
                            withIdentifier: "ActivateViewController")
                        self.present(viewController!, animated: true, completion: nil)
                    }
                }
            } else {
                print("error register(): response was not json")
                DispatchQueue.main.async {
                    self.view.makeToast(Const.ptal, duration: Const.tt(), position: .top)
                }
            }
        }
        task.resume()
    }
    
    @IBAction func login(sender: UIButton) {
        let uname = Const.phonify(number: etPhone.text ?? "")
        let passwd = etPasswd.text ?? ""
        
        if let err = errorless(username: uname, password: passwd) {
            self.view.makeToast(err, duration: Const.tt(), position: .top)
            return
        }
        
        loginAux(uname: uname, passHash: Const.sha256(input: passwd))
    }
    
    func loginAux(uname: String, passHash: String) {
        let json = ["uname": uname, "passwd": passHash]
        
        let auth = try? JSONSerialization.data(withJSONObject: json)
        let url = URL(string: Const.server("login"))
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
                    Const.uname = uname
                    Const.passwd = passHash
                    UserDefaults.standard.set(uname, forKey: "uname")
                    UserDefaults.standard.set(passHash, forKey: "passwd")
                    
                    DispatchQueue.main.async {
                        let viewController = self.storyboard?.instantiateViewController(
                            withIdentifier: "TabBarViewController") as! UITabBarController
                        viewController.selectedIndex = 1
                        self.present(viewController, animated: true, completion: nil)
                    }
                }
            } else {
                print("error loginAux(): response was not json")
                DispatchQueue.main.async {
                    self.view.makeToast(Const.ptal, duration: Const.tt(), position: .top)
                }
            }
        }
        task.resume()
    }
    
    @IBAction func resignKeyboard(sender: UITextField) {
        sender.resignFirstResponder()
    }
}
