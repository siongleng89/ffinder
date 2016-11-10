//
//  AnimateBuilder.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 8/11/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import UIKit
class AnimateBuilder{
    
    private var view:UIView?
    private var value:CGFloat?
    private var animateType:AnimateType?
    private var durationMs:Int?
    private var repeatable:Bool?
    private var finishCallback:(() -> Void)?
    public static var Fast:Int = 300
    public static var Medium:Int = 500
    public static var Slow:Int = 700
    
    public static func build(_ view:UIView) -> AnimateBuilder{
        return AnimateBuilder(view)
    }
    
    private init(_ view:UIView){
        self.view = view
    }
    
    public func setAnimateType(_ animateType:AnimateType) -> AnimateBuilder{
        self.animateType = animateType
        return self
    }
    
    public func setValue(_ value:CGFloat) -> AnimateBuilder{
        self.value = value
        return self
    }
    
    public func setDurationMs(_ durationMs:Int) -> AnimateBuilder{
        self.durationMs = durationMs
        return self
    }
    
    public func setRepeat(_ repeatable:Bool) -> AnimateBuilder{
        self.repeatable = repeatable
        return self
    }
    
    public func setFinishCallback(_ callback:@escaping (() -> Void)) -> AnimateBuilder{
        self.finishCallback = callback
        return self
    }
  
    
    
    public func start(){
        Threadings.postMainThread {
            var animationClosure:(() -> Void)?
            
            switch self.animateType!{
                case AnimateType.Alpha:
                    animationClosure = {
                        self.view?.alpha = self.value!
                    }
                break
                case AnimateType.MoveToY:
                    animationClosure = {
                        self.view?.center.y = self.value!
                    }
                break
            case AnimateType.MoveByX:
                animationClosure = {
                    self.view?.center.x += self.value!
                }
                break
            case AnimateType.RotateBy:
                animationClosure = {
                   self.view?.transform = (self.view?.transform.rotated(by: self.value!))!
                }
                break
            }
            
            var options:UIViewAnimationOptions = [];
            if let repeatable = self.repeatable{
                if repeatable{
                    options = [.repeat, .autoreverse]
                }
            }
            
            UIView.animate(withDuration: (TimeInterval(Float(self.durationMs!) / 1000)),
                           delay: 0,
                           options: options,
                           animations: animationClosure!,
                           completion:{ (finished) in
                                if let callback = self.finishCallback{
                                    callback()
                                }
                            }
                        )
            
            
            
            
        }
        
    }
    
    public static func fadeIn(_ view:UIView,
                              speed:Int? = Fast,
                              _ callback:(() -> Void)? = nil){
        let builder = AnimateBuilder.build(view)
            .setDurationMs(speed!).setAnimateType(AnimateType.Alpha)
            .setValue(1)
        
        if let callback = callback{
            _ = builder.setFinishCallback(callback)
        }
        
        builder.start()
        
    }
    
    public static func fadeOut(_ view:UIView,
                              speed:Int? = Fast,
                              _ callback:(() -> Void)? = nil){
        let builder = AnimateBuilder.build(view)
            .setDurationMs(speed!).setAnimateType(AnimateType.Alpha)
            .setValue(0)
        
        if let callback = callback{
            _ = builder.setFinishCallback(callback)
        }
        
        builder.start()
        
    }
    
    
    

}
