//
//  TextFieldWrapper.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 28/11/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
@IBDesignable class TextFieldWrapper:UIView, UITextFieldDelegate{
   
    @IBOutlet weak var labelTitle: UILabel!
    var view: UIView!
    @IBOutlet weak var textField: BottomBorderTextField!
    @IBOutlet weak var labelError: UILabel!
    @IBInspectable var title: String?
    
    override func awakeFromNib() {
        self.labelTitle.text = title?.localized
    }
    
    func xibSetup() {
        view = loadViewFromNib()
        
        // use bounds not frame or it'll be offset
        view.frame = bounds
        
        // Make the view stretch with containing view
        view.autoresizingMask = [UIViewAutoresizing.flexibleWidth, UIViewAutoresizing.flexibleHeight]
        // Adding custom subview on top of our view (over any custom drawing > see note below)
        addSubview(view)
    }
    
    func loadViewFromNib() -> UIView {
        
        let bundle = Bundle(for: type(of: self))
        let nib = UINib(nibName: "TextFieldWrapper", bundle: bundle)
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

    
    public func setText(_ text:String?){
        textField.text = text
    }
    
    public func getText() -> String{
        if let text = textField.text{
            return text
        }
        
        return ""
    }
    
    public func getTextField() -> UITextField{
        return textField;
    }
    
    
    public func validateNotEmpty(_ errorMsg:String) -> Bool{
        if StringsHelper.isEmpty(textField.text){
            setError(errorMsg)
            return false
        }
        else{
            clearError()
            return true
        }
    }
    
    public func enableNumericMode(){
        textField.delegate = self
        textField.keyboardType = UIKeyboardType.numbersAndPunctuation
    }
    
    
    public func setError(_ errorMsg:String){
        labelError.text = errorMsg
        AnimateBuilder.fadeIn(labelError)
    }
    
    public func clearError(){
        AnimateBuilder.fadeOut(labelError)
    }    
    

    @IBAction func onTextFieldFocus(_ sender: AnyObject) {
        textField.tintColor = UIColor.colorPrimaryDark()
        labelTitle.textColor = UIColor.colorPrimaryDark()
        textField.setNeedsDisplay()
    }
    
    @IBAction func onTextFieldLostFocus(_ sender: AnyObject) {
        textField.tintColor = UIColor.colorNormalText()
        labelTitle.textColor = UIColor.colorNormalText()
        textField.setNeedsDisplay()
    }
    

    func textField(_ textField: UITextField,
                   shouldChangeCharactersIn range: NSRange,
                   replacementString string: String) -> Bool {
        
        if range.length == 0{
            
            if !StringsHelper.isNumeric(string){
                return false
            }
            
            var strText: String? = textField.text
            if strText == nil {
                strText = ""
            }
            
            if  strText!.characters.count == 3 ||  strText!.characters.count == 8 {
                textField.text = "\(textField.text!)\(string)-"
                return false
            }
            
            
            return true
            
        }
        else if range.length == 1{
            
            if let char = textField.text?.characters.last{
                if char == "-"{
                    let currentString:String = textField.text!
                    let truncated = currentString.substring(to: currentString.index(before: currentString.endIndex))
                    textField.text = truncated
                }
            }
            
            return true
        }
    
        
       return true

    }
  
    
}
