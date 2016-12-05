//
//  PromoViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 29/11/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class PromoViewController:MyViewController{
    
    @IBOutlet weak var txtWrapperPromoCode: TextFieldWrapper!
    @IBOutlet weak var labelError: UILabel!
    
    override func viewDidLoad() {
        self.title = "promo_code_activity_title".localized
        self.hideKeyboardWhenTappedAround()
    }

    private func validateAndSubmit(){
        self.dismissKeyboard()
        AnimateBuilder.fadeOut(labelError)
        
        if txtWrapperPromoCode.validateNotEmpty("empty_promo_code_error_msg".localized){
            showLoading(Message: "checking_promo_code_msg".localized)
            
            RestfulService.usePromoCode(self.myModel.userId!, txtWrapperPromoCode.getText(), {(result, status) in
                
                Threadings.postMainThread {
                    self.hideLoading(onComplete: {
                        if status == Status.Success && StringsHelper.isNumeric(result)
                            && Int(result!)! > 0{
                            self.promoCodeSuccess(result!)
                        }
                        else{
                            self.promoCodeInvalid()
                        }
                    })
                    
                }
            
            })
            
        }
    
    }
    
    private func promoCodeInvalid(){
        labelError.text = "promo_code_failed_error_msg".localized
        AnimateBuilder.fadeIn(labelError)
    }
    
    private func promoCodeSuccess(_ addedCount:String){
        
        OverlayBuilder.build(self).setOverlayType(OverlayType.OkOnly)
            .setMessage("promo_code_success_msg".localized.format(addedCount))
            .setOnDismiss({
               // _ = self.navigationController?.popViewController(animated: true)
            }).show()
    }
    
    
    @IBAction func onSubmit(_ sender: AnyObject) {
        validateAndSubmit()
    }
    
    
    
    
    
    
    
    
    
}
