//
//  ImagePickerViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 9/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
import TOCropViewController
import Kingfisher


class ImagePickerViewController:UIViewController, UIImagePickerControllerDelegate, UINavigationControllerDelegate, TOCropViewControllerDelegate{
    
    @IBOutlet weak var dummayImageView: UIImageView!
    var friendModel:FriendModel!
    let imagePicker = UIImagePickerController()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.navigationItem.setHidesBackButton(true, animated:false)
        
        imagePicker.delegate = self
        
        imagePicker.allowsEditing = false
        imagePicker.sourceType = .photoLibrary
        
        present(imagePicker, animated: false, completion: nil)
        
        
    }
    
    
    func imagePickerController(_ picker: UIImagePickerController,
                               didFinishPickingMediaWithInfo info: [String : AnyObject])
    {
        dismiss(animated: true, completion: nil)
        if let pickedImage = info[UIImagePickerControllerOriginalImage] as? UIImage {
            let cropViewController = TOCropViewController(
                                image: pickedImage)
            cropViewController.delegate = self
            present(cropViewController, animated: false, completion: nil)
        }
        
       // _ = self.navigationController?.popViewController(animated: true)
    }
    
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        dismiss(animated: false, completion: nil)
        _ = self.navigationController?.popViewController(animated: false)
    }
    
    func cropViewController(_ cropViewController: TOCropViewController, didCropTo image: UIImage, with cropRect: CGRect, angle: Int) {
        
        let croppedImage = resizeImage(image: image, newWidth: 200)
        if let data = UIImagePNGRepresentation(croppedImage) {
            let filename = IOSUtils.getDocumentsDirectory()
                .appendingPathComponent("\(friendModel.userId!).png")
            Logs.show("Saving to: \(filename)")
            try? data.write(to: filename)
            
            dummayImageView.kf.setImage(with: filename, options:[.forceRefresh],
                                        completionHandler: {
                                            (image, error, cacheType, imageUrl) in
                                            
                                            self.friendModel.notificateChanged()
                                            self.dismiss(animated: false, completion: {
                                                _ = self.navigationController?.popViewController(animated: false)
                                            })

                                            
            })

            
            
        }
        else{
            dismiss(animated: false, completion: {
                _ = self.navigationController?.popViewController(animated: false)
            })

        
        }
    }
    
    func cropViewController(_ cropViewController: TOCropViewController, didFinishCancelled cancelled: Bool) {
        dismiss(animated: false, completion: nil)
        _ = self.navigationController?.popViewController(animated: false)
    }

    
    func resizeImage(image: UIImage, newWidth: CGFloat) -> UIImage {
        let scale = newWidth / image.size.width
        let newHeight = image.size.height * scale
        UIGraphicsBeginImageContext(CGSize(width: newWidth, height: newHeight))
        image.draw(in: CGRect(x: 0, y: 0, width: newWidth, height: newHeight))
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return newImage!
    }

    
    
}
