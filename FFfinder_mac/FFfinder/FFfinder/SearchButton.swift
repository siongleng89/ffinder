//
//  SearchButton.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 1/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class SearchButton:UIView{

    @IBOutlet weak var buttonContainer: UIView!
    @IBOutlet weak var imageViewFlower: UIImageView!
    var view: UIView!
    private var animationDict: [FlowerType: FlowerModel]!
    
    @IBOutlet weak var labelStatus: UILabel!
    
    override func awakeFromNib() {
        
        buttonContainer.layer.cornerRadius = 6.0
        buttonContainer.layer.borderWidth = 1.0
        buttonContainer.layer.borderColor = UIColor.colorPrimaryDark().cgColor
        
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
    
    
    private func setFlowerAnimation(_ flowerType:FlowerType){
        
        var animationImages: [UIImage] = []
        
        let flowerModel = animationDict[flowerType]
        
        var i:Int = 0
        while i < (flowerModel?.totalFrame)!{
            let imageName = "\((flowerModel?.fileName)!)\(String(format: "%02d", i)).png"
            animationImages += [(UIImage(named: imageName)!)
                                    .tintImage(color: UIColor.colorPrimaryDark())]
            i+=1
        }
        
        let totalDurationMs:Double = Double((flowerModel?.totalFrame)!) * 50
        
        imageViewFlower.animationImages = animationImages
        imageViewFlower.animationDuration = TimeInterval(totalDurationMs / 1000)
        imageViewFlower.startAnimating()
    
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
            else{
                fileName = ""
                totalFrame = 0
            }
            
        }
    }
    
    private enum FlowerType{
        case Starting
        case AutoSearching
        case Confusing
        case Ending
        case HappySwinging
        case SearchSuccess
        case Sleeping
        case Troubling
        
        static let allValues = [Starting, AutoSearching, Confusing,
                                Ending, HappySwinging, SearchSuccess,
                                Sleeping, Troubling]
    }
    

}
