//
//  FirstViewController.swift
//  itslitt
//
//  Created by Gage Swenson on 3/19/17.
//  Copyright © 2017 juicyasf. All rights reserved.
//

import Foundation
import UIKit
import MobileCoreServices

class FriendsViewController: UIViewController, UITableViewDataSource, UITableViewDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let longPressRecognizer = UILongPressGestureRecognizer(target: self, action: #selector(longPress(longPressGestureRecognizer:)))
        let tapRecognizer = UITapGestureRecognizer(target: self, action: #selector(shortPress(tapGestureRecognizer:)))
        
        self.view.addGestureRecognizer(longPressRecognizer)
        self.view.addGestureRecognizer(tapRecognizer)
    }
    override func viewDidAppear(_ animated: Bool) {
        Const.quickSort()
        self.lvFriends.reloadData()
    }

    @IBOutlet weak var lvFriends: UITableView!
    var btnSelfie: UIButton?
    
    func addFriend() {
        let viewController = self.storyboard?.instantiateViewController(withIdentifier: "AddFriendViewController")
        self.present(viewController!, animated: true, completion: nil)
    }
    
    func importContacts() {
        let viewController = self.storyboard?.instantiateViewController(withIdentifier: "ContactsViewController")
        self.present(viewController!, animated: true, completion: nil)
    }
    
    func pictureTime() {
        if UIImagePickerController.isSourceTypeAvailable(.camera) {
            let viewController = UIImagePickerController()
            viewController.delegate = self
            viewController.sourceType = .camera
            self.present(viewController, animated: true, completion: nil)
        }
        else {
            self.view.makeToast("Camera not available", duration: 2.0, position: .top)
        }
    }
    
    func shortPress(tapGestureRecognizer: UITapGestureRecognizer) {
        let touchPoint = tapGestureRecognizer.location(in: self.view)
        if let indexPath = lvFriends.indexPathForRow(at: touchPoint) {
            if let cell = tableView(lvFriends, cellForRowAt: indexPath) as? FriendViewCell {
                var friend = Const.friends[indexPath.row - 1]
                friend["lit"] = !(friend["lit"] as! Bool)
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
                                Const.friends[indexPath.row - 1]["lit"] = friend["lit"]
                                if friend["lit"] as! Bool {
                                    cell.ivLit?.image = UIImage(named: "candleon")
                                }
                                else {
                                    cell.ivLit?.image = UIImage(named: "candleoff")
                                }
                                self.lvFriends.reloadData()
                            }
                        }
                    } else {
                        print("SetFriend: response was not json")
                    }
                }
                task.resume()
            }
        }
    }
    
    func longPress(longPressGestureRecognizer: UILongPressGestureRecognizer) {
        let touchPoint = longPressGestureRecognizer.location(in: self.view)
        if let indexPath = lvFriends.indexPathForRow(at: touchPoint) {
            if let cell = tableView(lvFriends, cellForRowAt: indexPath) as? FriendViewCell {
                let friend = Const.friends[indexPath.row - 1]
            
                let name = friend["name"] as! String
                let fname = friend["fname"] as! String
                let addDelete = UIAlertController(title: name, message: fname, preferredStyle: .alert)
                let actionDelete = UIAlertAction(title: "Delete", style: .default, handler: {(uiaa: UIAlertAction) in
                
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
                                    Const.friends.remove(at: indexPath.row - 1)
                                    self.lvFriends.reloadData()
                                }
                            }
                        } else {
                            DispatchQueue.main.async {
                                self.view.makeToast("Please try again later", duration: 2.0, position: .top)
                            }
                            print("DelFriend: response was not json")
                        }
                    }
                    task.resume()
                })
                let actionEdit = UIAlertAction(title: "Edit", style: .default, handler: {(uiaa: UIAlertAction) in
                    Const.ecName = name
                    Const.ecFname = fname
                    Const.ecOldIndex = indexPath.row - 1
                    let viewController = self.storyboard?.instantiateViewController(withIdentifier: "AddFriendViewController") as! AddFriendViewController
                    self.present(viewController, animated: true, completion: nil)
                })
                let actionCancel = UIAlertAction(title: "Cancel", style: .default, handler: nil)
                addDelete.addAction(actionDelete)
                addDelete.addAction(actionEdit)
                addDelete.addAction(actionCancel)
                self.present(addDelete, animated: true, completion: nil)
            }
        }
    }
    
    //imgage upload
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
        if let image = info[UIImagePickerControllerOriginalImage] as? UIImage {
            btnSelfie?.setImage(image, for: .normal)
            Const.faces[Const.uname] = image
            var pngImage = UIImagePNGRepresentation(image)
            do {
                try pngImage!.write(to: URL(string: Const.selfieDir())!)
                pngImage = UIImagePNGRepresentation(image.resized(toWidth: 100.0)!)
                
                let border = "--- BUILD A WALL ---"
                let url = URL(string: Const.server("setpic"))
                var request = URLRequest(url: url!)
                request.httpMethod = "POST"
                request.addValue("multipart/form-data; boundary=\(border)", forHTTPHeaderField: "Content-Type")
                request.addValue("application/json", forHTTPHeaderField: "Accept")
                
                var body = Data()
                body.append("--\(border)\r\n".data(using: String.Encoding.utf8)!)
                body.append("Content-Disposition: form-data; name=\"uname\"\r\n\r\n".data(using: String.Encoding.utf8)!)
                body.append(Const.uname.data(using: String.Encoding.utf8)!)
                body.append("\r\n--\(border)\r\n".data(using: String.Encoding.utf8)!)
                body.append("Content-Disposition: form-data; name=\"passwd\"\r\n\r\n".data(using: String.Encoding.utf8)!)
                body.append(Const.passwd.data(using: String.Encoding.utf8)!)
                body.append("\r\n--\(border)\r\n".data(using: String.Encoding.utf8)!)
                let imgHeader = "Content-Disposition: form-data; name=\"file\"; filename=\"\(Const.uname).png\"\r\n"
                body.append(imgHeader.data(using: String.Encoding.utf8)!)
                body.append("Content-Type: application/octet-stream\r\n\r\n".data(using: String.Encoding.utf8)!)
                body.append(pngImage!)
                body.append("\r\n--\(border)--\r\n".data(using: String.Encoding.utf8)!)
                request.httpBody = body
                
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
                        if let err = response["error"] as? String {
                            print("setpic error: " + err)
                        }
                        else {
                            // pic set
                        }
                    } else {
                        print("SetPic: something went wrong")
                        DispatchQueue.main.async {
                            self.view.makeToast("Please try again later", duration: Const.tt(), position: .top)
                        }
                    }
                }
                task.resume()
            } catch {
                self.view.makeToast("Failed to save image", duration: Const.tt(), position: .top)
            }
        }
        picker.dismiss(animated: true, completion: nil)
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return Const.friends.count + 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if indexPath.row == 0 {
            let cell = tableView.dequeueReusableCell(withIdentifier: "idSelfie") as! SelfieViewCell
            cell.tvPhone?.text = Const.uname
            btnSelfie = cell.btnSelfie
            btnSelfie?.setImage(Const.faces[Const.uname]!, for: .normal)
            btnSelfie?.setRounded()
            btnSelfie?.addTarget(self, action: #selector(pictureTime), for: .touchUpInside)
            cell.btnAddFriend.addTarget(self, action: #selector(addFriend), for: .touchUpInside)
            cell.btnImportContacts.addTarget(self, action: #selector(importContacts), for: .touchUpInside)
            cell.selectionStyle = UITableViewCellSelectionStyle.none
            return cell
        }
        else {
            let cell = tableView.dequeueReusableCell(withIdentifier: "idFriend") as! FriendViewCell
            let friend: [String: Any] = Const.friends[indexPath.row - 1]
            cell.tvName?.text = friend["name"] as? String
            cell.tvFname?.text = friend["fname"] as? String
            if friend["lit"] as! Bool {
                cell.ivLit?.image = UIImage(named: "candleon")
            }
            else {
                cell.ivLit?.image = UIImage(named: "candleoff")
            }
            cell.selectionStyle = UITableViewCellSelectionStyle.none
            return cell
        }
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        if indexPath.row == 0 {
            return 220.0
        }
        else {
            return 70.0
        }
    }
}

