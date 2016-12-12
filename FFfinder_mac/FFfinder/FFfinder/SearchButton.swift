//
//  SearchButton.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 1/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation

class SearchButton:UIView{

    @IBOutlet weak var labelLastUpdated: UILabel!
    @IBOutlet weak var buttonContainer: UIView!
    @IBOutlet weak var imageViewFlower: UIImageView!
    var view: UIView!
    private var animationDict: [FlowerType: FlowerModel]!
    private var currentFlowerType: FlowerType?
    private var flowerColor:UIColor? = UIColor.colorPrimaryDark()
    @IBOutlet weak var buttonTopMargin: NSLayoutConstraint!
    @IBOutlet weak var labelStatus: UILabel!
    
    private var animating:Bool = false
    
    override func awakeFromNib() {
        
        
        
    }
    
    func xibSetup() {
        view = loadViewFromNib()
        
        // use bounds not frame or it'll be offset
        view.frame = bounds
        
        // Make the view stretch with containing view
        view.autoresizingMask = [UIViewAutoresizing.flexibleWidth, UIViewAutoresizing.flexibleHeight]
        // Adding custom subview on top of our view (over any custom drawing > see note below)
        addSubview(view)
        
        animationDict = [FlowerType: FlowerModel]()
        for type in FlowerType.allValues{
            animationDict[type] = FlowerModel(type)
        }
        
        buttonContainer.layer.cornerRadius = 6.0
        buttonContainer.layer.borderWidth = 1.0
        buttonContainer.layer.borderColor = UIColor.colorPrimaryDark().cgColor
        
        
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(onAppearing),                                               name: .UIApplicationDidBecomeActive, object: nil)

    }
    
    func onAppearing(){
        if animating{
         imageViewFlower.startAnimating()
        }
    }
    
    func loadViewFromNib() -> UIView {
        
        let bundle = Bundle(for: type(of: self))
        let nib = UINib(nibName: "SearchButton", bundle: bundle)
        let view = nib.instantiate(withOwner: self, options: nil)[0] as! UIView
        
        return view
    }
    
    
    override init(frame: CGRect) {
        // 1. setup any properties here
        
        // 2. call super.init(frame:)
        super.init(frame: frame)
        
        // 3. Setup view from .xib file
        xibSetup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        // 1. setup any properties here
        
        // 2. call super.init(coder:)
        super.init(coder: aDecoder)
        
        // 3. Setup view from .xib file
        xibSetup()
    }
    
  
   
    
    public func setFlower(_ flowerType:FlowerType, extra:String? = nil, animate:Bool = true){
        if flowerType == FlowerType.Starting{
            setStatus("searching".localized)
            self.colorDown()
            setFlowerAnimation(FlowerType.Starting, repeating: false, callback:{
                self.setFlowerAnimation(FlowerType.HappySwinging)
            })
        }
        else if flowerType == FlowerType.Ending{
            
            if extra == "autoSearching"{
                setFlowerAnimation(FlowerType.Ending, repeating: false, callback:{
                    self.colorUp()
                    self.setFlower(FlowerType.AutoSearching)
                })
            }
            else if extra == "error"{
                setFlowerAnimation(FlowerType.Ending, repeating: false, callback:{
                    self.colorUp()
                    self.setFlower(FlowerType.Sleeping)
                    self.setStatus("search".localized)
                })
            }
            else{
                setFlowerAnimation(FlowerType.SearchSuccess, repeating: false,
                                   animateRepeatCount: 2, callback:{
                    self.setFlowerAnimation(FlowerType.Ending, repeating: false, callback:{
                        self.colorUp()
                        self.setStatus("search".localized)
                        self.setFlowerAnimation(FlowerType.Satisfied)
                    })
                })
            
            }
        }
        else if flowerType == FlowerType.AutoSearching{
            setStatus("auto_searching".localized)
            self.setFlowerAnimation(FlowerType.AutoSearching, animate: animate, repeating: false)
        }
        else{
            self.setFlowerAnimation(flowerType)
        }
    
    }
    
    
    private func setFlowerAnimation(_ flowerType:FlowerType,
                                    animate:Bool = true,
                                    frameRateMs:Double = 50,
                                   repeating:Bool = true,
                                   animateRepeatCount:Int = 1,
                                   callback:(()->Void)? = nil){
        
        //no need reanimate
        if flowerType == currentFlowerType{
            return
        }
        
        if currentFlowerType == FlowerType.HappySwinging && flowerType == FlowerType.Starting{
            return
        }
        
        currentFlowerType = flowerType
        var animationImages: [UIImage] = []
        
        let flowerModel = animationDict[flowerType]
        
        var i:Int = 0
        while i < (flowerModel?.totalFrame)!{
            let imageName = "\((flowerModel?.fileName)!)\(String(format: "%02d", i)).png"
            animationImages += [(UIImage(named: imageName)!)
                                    .tintImage(color: flowerColor!)]
            i+=1
        }
        
        let totalDurationMs:Double = Double((flowerModel?.totalFrame)!) * frameRateMs
        
        imageViewFlower.stopAnimating()
        
        
        if animationImages.count > 1 && animate{
            animating = true
            
            imageViewFlower.animationImages = animationImages
            imageViewFlower.animationDuration = TimeInterval(totalDurationMs / 1000)
            if !repeating{
                imageViewFlower.animationRepeatCount = animateRepeatCount
                imageViewFlower.image = animationImages.last
            }
            else{
                imageViewFlower.animationRepeatCount = 1000
                imageViewFlower.image = nil
            }
            
            
            if let callback = callback{
                let completion: Block = {_ in
                    callback()
                }
                imageViewFlower.startAnimating(completionBlock: completion)
            }
            else{
                imageViewFlower.startAnimating()
            }
        }
        else{
            animating = false
            
            imageViewFlower.image = animationImages.first
        }
     
    }
    
    private func colorUp(){
        flowerColor = UIColor.colorPrimaryDark()
        labelStatus.textColor = flowerColor
        buttonContainer.backgroundColor = UIColor.colorContrast()
        UIView.animate(withDuration: 0.3, animations: {
            self.buttonTopMargin.constant = 8
            self.view.layoutIfNeeded()
        })
    }
    
    private func colorDown(){
        flowerColor = UIColor.colorContrast()
        labelStatus.textColor = flowerColor
        buttonContainer.backgroundColor = UIColor.colorPrimaryDark()
        UIView.animate(withDuration: 0.3, animations: {
            self.buttonTopMargin.constant = -15
            self.view.layoutIfNeeded()
        })
    }
    
    public func setStatus(_ text:String){
        labelStatus.text = text
    }
    
    public func setLastUpdated(_ text:String){
        labelLastUpdated.text = text
    }
    
    private class FlowerModel{
        var fileName:String
        var totalFrame:Int
        
        init(_ type:FlowerType){
            
            if type == FlowerType.AutoSearching{
                fileName = "flower_auto_searching_0"
                totalFrame = 15
            }
            else if type == FlowerType.Starting{
                fileName = "flower_starting_0"
                totalFrame = 12
            }
            else if type == FlowerType.Confusing{
                fileName = "flower_confusing_0"
                totalFrame = 14
            }
            else if type == FlowerType.Ending{
                fileName = "flower_ending_0"
                totalFrame = 12
            }
            else if type == FlowerType.HappySwinging{
                fileName = "flower_happy_swing_0"
                totalFrame = 14
            }
            else if type == FlowerType.SearchSuccess{
                fileName = "flower_search_success_0"
                totalFrame = 14
            }
            else if type == FlowerType.Sleeping{
                fileName = "flower_sleeping_0"
                totalFrame = 1
            }
            else if type == FlowerType.Troubling{
                fileName = "flower_troubling_0"
                totalFrame = 14
            }
            else if type == FlowerType.Satisfied{
                fileName = "flower_satisfied_0"
                totalFrame = 1
            }
            else{
                fileName = ""
                totalFrame = 0
            }
            
        }
    }
    
   
    

}
