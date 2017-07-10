//
//  RIV.swift
//  itslitt
//
//  Created by Gage Swenson on 5/18/17.
//  Copyright © 2017 juicyasf. All rights reserved.
//

import Foundation
import UIKit
import GoogleMaps

extension UIButton {
    func setRounded() {
        let radius = self.frame.width / 2
        self.layer.cornerRadius = radius
        self.layer.masksToBounds = true
    }
}

extension UIImage {
    var isPortrait:  Bool    { return size.height >= size.width }
    var isLandscape: Bool    { return size.width > size.height }
    var breadth:     CGFloat { return min(size.width, size.height) }
    var breadthSize: CGSize  { return CGSize(width: breadth, height: breadth) }
    var breadthRect: CGRect  { return CGRect(origin: .zero, size: breadthSize) }
    var circleMasked: UIImage? {
        UIGraphicsBeginImageContextWithOptions(breadthSize, false, scale)
        defer { UIGraphicsEndImageContext() }
        guard let cgImage = cgImage?.cropping(to: CGRect(origin: CGPoint(x: isLandscape ? floor((size.width - size.height) / 2) : 0, y: isPortrait  ? floor((size.height - size.width) / 2) : 0), size: breadthSize)) else { return nil }
        UIBezierPath(ovalIn: breadthRect).addClip()
        UIImage(cgImage: cgImage).draw(in: breadthRect)
        
        let ctx = UIGraphicsGetCurrentContext()!
        ctx.setStrokeColor(UIColor.blue.cgColor)
        ctx.setLineWidth(CGFloat(1))
        ctx.strokeEllipse(in: breadthRect)
        
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        
        return newImage
    }
    func scaleFor(zoom: Float) -> UIImage? {
        let size = CGFloat(15 + zoom * zoom / 10)
        
        let rect = CGRect(x: 0, y: 0, width: size, height: size)
        UIGraphicsBeginImageContext(CGSize(width: size, height: size))
        self.draw(in: rect)
        
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return newImage
    }
}
