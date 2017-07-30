//
//  ContactsViewController.swift
//  itslitt
//
//  Created by Gage Swenson on 5/17/17.
//  Copyright © 2017 juicyasf. All rights reserved.
//

import Foundation
import UIKit
import Contacts

class ContactsViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        loadContacts(search: "")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let tapRecognizer = UITapGestureRecognizer(target: self, action: #selector(shortPress(tapGestureRecognizer:)))
        self.view.addGestureRecognizer(tapRecognizer)
    }
    
    @IBOutlet weak var lvContacts: UITableView!
    @IBOutlet weak var etSearch: UITextField!
    
    func shortPress(tapGestureRecognizer: UITapGestureRecognizer) {
        let touchPoint = tapGestureRecognizer.location(in: self.lvContacts)
        if let indexPath = lvContacts.indexPathForRow(at: touchPoint) {
            let cell = tableView(lvContacts, cellForRowAt: indexPath) as! FriendViewCell
            let newLit: Bool = !(Const.contacts[indexPath.row]["lit"] as! Bool)
            var contact: [String: Any] = [:]
            contact["name"] = cell.tvName.text
            contact["fname"] = cell.tvFname.text
            contact["lit"] = newLit
            let json: [String: Any] = ["uname": Const.uname, "passwd": Const.passwd, "friend": contact]
            let auth = try? JSONSerialization.data(withJSONObject: json)
            
            var url: URL!
            if newLit {
                url = URL(string: Const.server("setfriend"))
            }
            else {
                url = URL(string: Const.server("delfriend"))
            }
            var request = URLRequest(url: url)
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
                        if newLit {
                            var i = 0
                            while i < Const.friends.count {
                                if Const.friends[i]["fname"] as! String == contact["fname"] as! String {
                                    Const.friends.remove(at: i)
                                }
                                i = i + 1
                            }
                            contact["lit"] = false
                            Const.friends.append(contact)
                            cell.ivLit.image = UIImage(named: "candleon")
                        }
                        else {
                            var i = 0
                            while i < Const.friends.count {
                                if Const.friends[i]["fname"] as! String == contact["fname"] as! String {
                                    Const.friends.remove(at: i)
                                }
                                i = i + 1
                            }
                            cell.ivLit.image = UIImage(named: "candleoff")
                        }
                        DispatchQueue.main.async {
                            self.loadContacts(search: self.etSearch.text ?? "")
                        }
                    }
                } else {
                    print("error shortPress(): Contacts response was not json")
                    DispatchQueue.main.async {
                        self.view.makeToast(Const.ptal, duration: Const.tt(), position: .top)
                    }
                }
            }
            task.resume()
        }
    }
    
    @IBAction func search(sender: UITextField) {
        loadContacts(search: sender.text!)
    }
    
    func loadContacts(search: String) {
        Const.contacts = []
        
        let store = CNContactStore()
        
        self.retrieveContactsWith(store: store, search: search)
    }
    
    func retrieveContactsWith(store: CNContactStore, search: String) {
        do {
            let keysToFetch: [CNKeyDescriptor] = [
                CNContactFormatter.descriptorForRequiredKeys(for: .fullName),
                CNContactPhoneNumbersKey
            ] as! [CNKeyDescriptor]
            var predicate: NSPredicate!
            if search != "" {
                predicate = CNContact.predicateForContacts(matchingName: search)
                let contacts = try store.unifiedContacts(matching: predicate, keysToFetch: keysToFetch)
                for contact in contacts {
                    var friend: [String: Any] = [:]
                    friend["name"] = contact.givenName + " " + contact.familyName
                    friend["fname"] = Const.phonify(number: contact.phoneNumbers[0].value.stringValue)
                    var lit = false
                    for fren in Const.friends {
                        if fren["fname"] as! String == friend["fname"] as! String {
                            lit = true
                        }
                    }
                    friend["lit"] = lit
                    Const.contacts.append(friend)
                }
            }
            else {
                let allContainers = try store.containers(matching: nil)
                var results: [CNContact] = []
                
                for container in allContainers {
                    let fetchPredicate = CNContact.predicateForContactsInContainer(withIdentifier: container.identifier)
                    let cResults = try store.unifiedContacts(matching: fetchPredicate, keysToFetch: keysToFetch)
                    results.append(contentsOf: cResults)
                }
                for fren in results {
                    var friend: [String: Any] = [:]
                    friend["name"] = fren.givenName + " " + fren.familyName
                    friend["fname"] = allDigits(fren.phoneNumbers[0].value.stringValue)
                    var lit = false
                    for fren in Const.friends {
                        if fren["fname"] as! String == friend["fname"] as! String {
                            lit = true
                        }
                    }
                    friend["lit"] = lit
                    Const.contacts.append(friend)
                }
            }
            self.lvContacts.reloadData()
            
        } catch {
            print(error)
            DispatchQueue.main.async {
                self.view.makeToast("Failed to read contacts", duration: Const.tt(), position: .top)
            }
        }
    }
    
    func allDigits(_ num: String) -> String {
        var res = ""
        for ch in num.characters {
            if ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"].contains(ch) {
                res.append(ch)
            }
        }
        return res
    }
    
    @IBAction func done(sender: UIButton) {
        let json: [String: Any] = ["uname": Const.uname, "passwd": Const.passwd, "friends": Const.friends]
        
        let auth = try? JSONSerialization.data(withJSONObject: json)
        let url = URL(string: Const.server("setfriends"))
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
            } else {
                print("error done(): response was not json")
                DispatchQueue.main.async {
                    self.view.makeToast(Const.ptal, duration: Const.tt(), position: .top)
                }
            }
        }
        task.resume()
        self.dismiss(animated: true, completion: nil)
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return Const.contacts.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "idContact") as! FriendViewCell
        let contact: [String: Any] = Const.contacts[indexPath.row]
        cell.tvName?.text = contact["name"] as? String
        cell.tvFname?.text = contact["fname"] as? String
        if contact["lit"] as! Bool {
            cell.ivLit?.image = UIImage(named: "candleon")
        }
        else {
            cell.ivLit?.image = UIImage(named: "candleoff")
        }
        cell.selectionStyle = UITableViewCellSelectionStyle.none
        return cell
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 70.0
    }
    
    @IBAction func resignKeyboard(sender: UITextField) {
        sender.resignFirstResponder()
    }
}
