//
//  MapViewController.swift
//  itslitt
//
//  Created by Gage Swenson on 3/25/17.
//  Copyright © 2017 juicyasf. All rights reserved.
//

import UIKit
import GoogleMaps
import MessageUI

class MapViewController: UIViewController, GMSMapViewDelegate {
    
    var mapView: GMSMapView!
    var timer: Timer?
    var oldMarkers: [String: GMSMarker]?
    
    override func loadView() {
        let camera = GMSCameraPosition.camera(withLatitude: 0.0, longitude: 0.0, zoom: 1.0)
        mapView = GMSMapView.map(withFrame: CGRect.zero, camera: camera)
        mapView.delegate = self
        view = mapView
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        // Do any additional setup after loading the view, typically from a nib.
        timer = Timer.scheduledTimer(withTimeInterval: 2.0, repeats: true, block: {(olTimer) in
            self.getlit()
        })
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        if let timr = timer {
            timr.invalidate()
            timer = nil
        }
    }
    
    func mapView(_ mapView: GMSMapView, didTapInfoWindowOf marker: GMSMarker) {
        mapView.selectedMarker = nil
    }
    
    func mapView(_ mapView: GMSMapView, didLongPressInfoWindowOf marker: GMSMarker) {
        let fname = marker.snippet?.components(separatedBy: "\n\n")[0]
        if (MFMessageComposeViewController.canSendText()) {
            let viewController = MFMessageComposeViewController()
            viewController.recipients = [fname!]
            self.present(viewController, animated: true, completion: nil)
        }
    }
    
    func mapView(_ mapView: GMSMapView, didTap marker: GMSMarker) -> Bool {
        var newZoom = mapView.camera.zoom
        if newZoom < 7.2 {
            newZoom = 10.0
        }
        else if newZoom < 10.3 {
            newZoom = 18.0
        }
        mapView.moveCamera(GMSCameraUpdate.setTarget(marker.position, zoom: newZoom))
        mapView.selectedMarker = marker
        return true
    }
    
    func mapView(_ mapView: GMSMapView, didLongPressAt coordinate: CLLocationCoordinate2D) {
        mapView.moveCamera(GMSCameraUpdate.setTarget(coordinate, zoom: 3.0))
    }
    func mapView(_ mapView: GMSMapView, markerInfoContents marker: GMSMarker) -> UIView? {
        return nil
    }
    func mapView(_ mapView: GMSMapView, markerInfoWindow marker: GMSMarker) -> UIView? {
        let nib = UINib(nibName: "InfoWindo", bundle: nil).instantiate(withOwner: nil, options: nil)[0] as! InfoWindo
        let nameFname = marker.title?.components(separatedBy: "\n")
        nib.tvName.text = nameFname?[0]
        nib.tvFname.text = nameFname?[1]
        nib.tvStatus.text = marker.snippet
        return nib
    }
    
    func getlit() {
        DispatchQueue.global(qos: DispatchQoS.background.qosClass).async {
            let json = ["uname": Const.uname, "passwd": Const.passwd]
            
            let auth = try? JSONSerialization.data(withJSONObject: json)
            let url = URL(string: Const.server("getlit"))
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
                    else if let friends = response["friends"] as? [[String: Any]] {
                        DispatchQueue.main.async {
                            
                            var markers: [String: GMSMarker] = [:]
                            for friend in friends {
                                let name = friend["name"] as! String
                                let phone = friend["fname"] as! String
                                var icon: UIImage!
                                if let img = Const.faces[phone] {
                                    icon = img
                                }
                                else {
                                    icon = UIImage(named: "nullpicsmall")
                                    
                                    let json = ["uname": Const.uname, "passwd": Const.passwd, "fname": phone]
                                    
                                    let auth = try? JSONSerialization.data(withJSONObject: json)
                                    let url = URL(string: Const.server("getpic"))
                                    var request = URLRequest(url: url!)
                                    request.httpMethod = "POST"
                                    request.httpBody = auth
                                    request.addValue("application/json", forHTTPHeaderField: "Content-Type")
                                    request.addValue("application/octet-stream", forHTTPHeaderField: "Accept")
                                    
                                    let task = URLSession.shared.dataTask(with: request) { data, resp, error in
                                        guard let data = data, error == nil else {
                                            print(error?.localizedDescription ?? "No data")
                                            return
                                        }
                                        if let img = UIImage(data: data) {
                                            Const.faces[phone] = img
                                        }
                                    }
                                    task.resume()
                                }
                                icon = icon.scaleFor(zoom: self.mapView.camera.zoom)!.circleMasked!
                                let status = friend["status"] as! String
                                let lat = friend["lat"] as! Double
                                let lon = friend["lon"] as! Double
                                var queued = true
                                if let oldLit = self.oldMarkers {
                                    for fren in oldLit {
                                        if fren.key == phone {
                                            let oldLat = fren.value.position.latitude
                                            let oldLon = fren.value.position.longitude
                                            let newLat = friend["lat"] as! Double
                                            let newLon = friend["lon"] as! Double
                                            if oldLat == newLat && oldLon == newLon {
                                                var iconAfterZoom: UIImage!
                                                if let img = Const.faces[phone] {
                                                    iconAfterZoom = img
                                                }
                                                else {
                                                    iconAfterZoom = UIImage(named: "nullpicsmall")
                                                }
                                                fren.value.icon = iconAfterZoom.scaleFor(zoom: self.mapView.camera.zoom)!.circleMasked!
                                                markers[phone] = fren.value
                                                queued = false
                                            }
                                            else {
                                                fren.value.map = nil
                                            }
                                        }
                                    }
                                }
                                
                                if queued {
                                    let marker = GMSMarker()
                                    marker.position = CLLocationCoordinate2D(latitude: lat, longitude: lon)
                                    marker.title = name + "\n" + phone
                                    marker.snippet = status
                                    marker.icon = icon
                                    markers[phone] = marker
                                }
                                
                                var indx = 0
                                while indx < Const.friends.count {
                                    if Const.friends[indx]["fname"] as! String == phone {
                                        Const.friends[indx]["status"] = status
                                    }
                                    indx = indx + 1
                                }
                            }
                            
                            if let oldLit = self.oldMarkers {
                                for oldMarker in oldLit {
                                    var burn = true
                                    for marker in markers {
                                        if oldMarker.key == marker.key {
                                            burn = false
                                        }
                                    }
                                    if burn {
                                        oldMarker.value.map = nil
                                    }
                                }
                            }
                            
                            for mrkr in markers.values {
                                mrkr.map = self.mapView
                            }
                            self.oldMarkers = markers
                        }
                    }
                    else {
                        print("error getlit(): server responded without friends")
                    }
                } else {
                    print("error getlit(): response was not json")
                }
            }
            task.resume()
        }
    }
}
