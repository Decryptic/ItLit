//
//  SecondViewController.swift
//  itslitt
//
//  Created by Gage Swenson on 3/19/17.
//  Copyright © 2017 juicyasf. All rights reserved.
//

import UIKit
import Foundation
import CoreLocation

class LightViewController: UIViewController, CLLocationManagerDelegate {
    
    @IBOutlet weak var tvTalk: UILabel!
    @IBOutlet weak var btnLight: UIButton!
    @IBOutlet weak var etStatus: UITextField!
    @IBOutlet weak var tvChars: UILabel!

    override func viewDidLoad() {
        super.viewDidLoad()
        
        initBulb()
        initStatus()
        initFriends()
        
        //init selfie
        let path = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)[0] as String
        let url = NSURL(fileURLWithPath: path)
        let filePath = url.appendingPathComponent("selfie.png")?.path
        let fileMan = FileManager.default
        if fileMan.fileExists(atPath: filePath!) {
            Const.faces[Const.uname] = UIImage(contentsOfFile: filePath!)
        }
        else {
            Const.faces[Const.uname] = UIImage(named: "nullpicbig")
        }
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        statusChange(sender: etStatus)
    }
    
    func initBulb() {
        let json: [String: Any] = ["uname": Const.uname, "passwd": Const.passwd]
        
        let auth = try? JSONSerialization.data(withJSONObject: json)
        let url = URL(string: Const.server("light"))
        var request = URLRequest(url: url!)
        request.httpMethod = "POST"
        request.httpBody = auth
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let task = URLSession.shared.dataTask(with: request) { data, resp, error in
            guard let data = data, error == nil else {
                print(error?.localizedDescription ?? "No data")
                return
            }
            let response = try? JSONSerialization.jsonObject(with: data)
            if let response = response as? [String: Any] {
                if let err = response["error"] {
                    print("error initBulb: " + (err as! String))
                }
                else if let lit = response["lit"] {
                    var imgName: String
                    if lit as! Bool {
                        imgName = "lighton"
                    }
                    else {
                        imgName = "lightoff"
                    }
                    self.btnLight.setImage(UIImage(named:imgName), for: .normal)
                }
                else {
                    print("error /light must not be working right")
                }
            }
            else {
                print("InitBulb: repsonse was not json")
            }
        }
        task.resume()
    }
    
    func initStatus() {
        let json: [String: Any] = ["uname": Const.uname, "passwd": Const.passwd]
        
        let auth = try? JSONSerialization.data(withJSONObject: json)
        let url = URL(string: Const.server("statusget"))
        var request = URLRequest(url: url!)
        request.httpMethod = "POST"
        request.httpBody = auth
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let task = URLSession.shared.dataTask(with: request) { data, resp, error in
            guard let data = data, error == nil else {
                print(error?.localizedDescription ?? "No data")
                return
            }
            let response = try? JSONSerialization.jsonObject(with: data)
            if let response = response as? [String: Any] {
                if let err = response["error"] {
                    print("error in initStatus: " + (err as! String))
                }
                else if let status = response["status"] {
                    self.etStatus.text = status as? String ?? ""
                    self.tvChars.text = String((status as! String).characters.count) + " characters"
                }
                else {
                    print("What happened here")
                }
            } else {
                print("InitStatus: response was not json")
            }
        }
        task.resume()
    }
    
    func initFriends() {
        let json: [String: Any] = ["uname": Const.uname, "passwd": Const.passwd]
        
        let auth = try? JSONSerialization.data(withJSONObject: json)
        let url = URL(string: Const.server("getfriends"))
        var request = URLRequest(url: url!)
        request.httpMethod = "POST"
        request.httpBody = auth
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let task = URLSession.shared.dataTask(with: request) { data, resp, error in
            guard let data = data, error == nil else {
                print(error?.localizedDescription ?? "No data")
                return
            }
            let response = try? JSONSerialization.jsonObject(with: data)
            if let response = response as? [String: Any] {
                if let err = response["error"] {
                    DispatchQueue.main.async {
                        self.view.makeToast(err as! String, duration: Const.tt(), position: .top)
                    }
                }
                else if let friends = response["friends"] {
                    Const.friends = friends as! [[String: Any]]
                }
                else {
                    print("What happened here")
                }
            } else {
                print("InitFriends: response was not json")
            }
        }
        task.resume()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func lightSwitch(sender: UIButton) {
        let json: [String: Any] = ["uname": Const.uname, "passwd": Const.passwd, "lit": !Const.lit]
        
        let auth = try? JSONSerialization.data(withJSONObject: json)
        let url = URL(string: Const.server("light"))
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
                    Const.lit = !Const.lit
                    DispatchQueue.main.async {
                        if Const.lit {
                            if CLLocationManager.locationServicesEnabled() {
                                Const.locationManager = CLLocationManager()
                                
                                let status = CLLocationManager.authorizationStatus()
                                if status == .notDetermined {
                                    Const.locationManager?.requestAlwaysAuthorization()
                                }
                                else if status == .authorizedWhenInUse || status == .restricted || status == .denied {
                                    let alertController = UIAlertController(
                                        title: "Enable Background Location",
                                        message: "In order to use ItLit, please enable 'Always' location in settings.",
                                        preferredStyle: .alert)
                                    
                                    let cancelAction = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)
                                    alertController.addAction(cancelAction)
                                    
                                    let openAction = UIAlertAction(title: "Open Settings", style: .default) { action in
                                        if let url = URL(string: UIApplicationOpenSettingsURLString) {
                                            UIApplication.shared.open(url, options: [:], completionHandler: nil)
                                        }
                                    }
                                    alertController.addAction(openAction)
                                    self.present(alertController, animated: true, completion: nil)
                                    
                                }
                                else if status == .authorizedAlways {
                                    self.tvTalk.text = "friends can see you"
                                    self.btnLight.setImage(UIImage(named: "lighton"), for: .normal)
                                    
                                    Const.locationManager?.delegate = self
                                    Const.locationManager?.desiredAccuracy = kCLLocationAccuracyBest
                                    Const.locationManager?.allowsBackgroundLocationUpdates = true;
                                    Const.locationManager?.startUpdatingLocation()
                                }
                            }
                            else {
                                self.view.makeToast("Location services disabled", duration: Const.tt(), position: .top)
                            }
                        }
                        else {
                            self.tvTalk.text = "offline"
                            self.btnLight.setImage(UIImage(named: "lightoff"), for: .normal)
                            if let man = Const.locationManager {
                                man.stopUpdatingLocation()
                            }
                            Const.locationManager = nil
                            Const.lastLocation = nil
                        }
                    }
                }
            } else {
                print("error lightSwitch(): response was not json")
                DispatchQueue.main.async {
                    self.view.makeToast(Const.ptal, duration: Const.tt(), position: .top)
                }
            }
        }
        task.resume()
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if locations.count > 0 {
            Const.lastLocation = locations[0]
            
            let json: [String: Any] = [
                "uname": Const.uname,
                "passwd": Const.passwd,
                "lat": locations[0].coordinate.latitude,
                "lon": locations[0].coordinate.longitude
            ]
            
            let auth = try? JSONSerialization.data(withJSONObject: json)
            let url = URL(string: Const.server("move"))
            var request = URLRequest(url: url!)
            request.httpMethod = "POST"
            request.httpBody = auth
            request.addValue("application/json", forHTTPHeaderField: "Content-Type")
            request.addValue("application/json", forHTTPHeaderField: "Accept")
            
            let task = URLSession.shared.dataTask(with: request) { data, resp, error in
                guard let data = data, error == nil else {
                    print(error?.localizedDescription ?? "No data")
                    return
                }
                let response = try? JSONSerialization.jsonObject(with: data)
                if let response = response as? [String: Any] {
                    if let err = response["error"] as? String {
                        print("location update error: " + err)
                    }
                    else {
                        // moved
                    }
                } else {
                    print("error locationManager(): Light response was not json")
                }
            }
            task.resume()
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print(error)
    }
    
    @IBAction func statusChange(sender: UITextField) {
        var num: String = "0"
        if let txt = etStatus.text {
            let leng = txt.characters.count
            if leng > 50 {
                etStatus.text = txt.substring(to: txt.index(before: txt.endIndex))
                return
            }
            num = String(leng)
        }
        tvChars.text = num + " characters"
        
        let status: String = etStatus.text ?? ""
        let json: [String: Any] = ["uname": Const.uname, "passwd": Const.passwd, "status": status]
        
        let auth = try? JSONSerialization.data(withJSONObject: json)
        let url = URL(string: Const.server("status"))
        var request = URLRequest(url: url!)
        request.httpMethod = "POST"
        request.httpBody = auth
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        request.addValue("application/json", forHTTPHeaderField: "Accept")
        
        let task = URLSession.shared.dataTask(with: request) { data, resp, error in
            guard let data = data, error == nil else {
                print(error?.localizedDescription ?? "No data")
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
                    // status changed
                }
            } else {
                print("error statusChange(): response was not json")
            }
        }
        task.resume()
    }

    @IBAction func logOut(sender: UIButton) {
        UserDefaults.standard.set(nil, forKey: "uname")
        UserDefaults.standard.set(nil, forKey: "passwd")
        self.dismiss(animated: true, completion: nil)
        
        let json: [String: Any] = ["uname": Const.uname, "passwd": Const.passwd]
        
        let auth = try? JSONSerialization.data(withJSONObject: json)
        let url = URL(string: Const.server("logout"))
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
                    // logged out
                }
            } else {
                print("error logOut(): response was not json")
            }
        }
        task.resume()
    }
    
    @IBAction func resignKeyboard(sender: UITextField) {
        sender.resignFirstResponder()
    }
}

