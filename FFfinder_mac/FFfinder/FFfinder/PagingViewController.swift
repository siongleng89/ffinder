//
//  PagingViewController.swift
//  FFfinder
//
//  Created by Siong Leng Ho on 10/12/16.
//  Copyright © 2016 Lightweight Studio Apps. All rights reserved.
//

import Foundation
class PagingViewController:UIPageViewController{
    
    var type:PagingType?
    var orderedViewControllers:[SinglePageViewController]?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.navigationController?.setNavigationBarHidden(false, animated: false)
        dataSource = self
        stylePageControl()
        
        if type == PagingType.AppsIntro{
            initAppsIntro()
            self.navigationItem.hidesBackButton = true
            let button1 = UIBarButtonItem(image: UIImage(named: "SaveIcon"), style: .plain, target: self, action: #selector(closeTapped))
            button1.tintColor = UIColor.colorContrast()
            self.navigationItem.rightBarButtonItem  = button1
            
             Analytics.setScreen(name: "ActivityIntro")
        }
        else if type == PagingType.TutorialShareKey{
            initTutorialShareKey()
        }
        else if type == PagingType.TutorialKey{
            initAddManually()
        }
        
        if let firstViewController = orderedViewControllers?.first {
            setViewControllers([firstViewController],
                               direction: .forward,
                               animated: true,
                               completion: nil)
        }
        
       //
        
        
        
    }
    
    
    private func initAppsIntro(){
        orderedViewControllers = []
        
        var models:[PageModel] = Array()
        
        models.append(PageModel("apps_intro1".localized, UIImage(named: "intro_1")))
        models.append(PageModel("apps_intro2".localized, UIImage(named: "intro_2")))
        models.append(PageModel("apps_intro3".localized, UIImage(named: "intro_3")))
        models.append(PageModel("apps_intro4".localized, UIImage(named: "intro_4")))
        models.append(PageModel("apps_intro5".localized, UIImage(named: "intro_5")))
        models.append(PageModel("apps_intro6".localized, UIImage(named: "intro_6")))
        
        for pageModel in models{
            let singlePageViewController:SinglePageViewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "SinglePageViewController") as! SinglePageViewController
            
            singlePageViewController.labelText = pageModel.text
            singlePageViewController.uiImage = pageModel.image
            orderedViewControllers?.append(singlePageViewController)
        }
    }

    
    
    private func initTutorialShareKey(){
        orderedViewControllers = []
        
        var models:[PageModel] = Array()
        
        models.append(PageModel("share_passcode_s1".localized, UIImage(named:"share_passcode_s1")))
        models.append(PageModel("share_passcode_s2".localized, UIImage(named:"share_passcode_s2")))
        models.append(PageModel("share_passcode_s3".localized, UIImage(named:"share_passcode_s3")))
        models.append(PageModel("share_passcode_s4".localized, UIImage(named:"intro_5")))
        
        
        for pageModel in models{
            let singlePageViewController:SinglePageViewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "SinglePageViewController") as! SinglePageViewController
            
            singlePageViewController.labelText = pageModel.text
            singlePageViewController.uiImage = pageModel.image
            orderedViewControllers?.append(singlePageViewController)
        }
    }
    
    
    private func initAddManually(){
        orderedViewControllers = []
        
        var models:[PageModel] = Array()
        
        models.append(PageModel("add_manually_a1".localized, UIImage(named:"add_manually_a1")))
        models.append(PageModel("add_manually_a2".localized, UIImage(named:"add_manually_a2")))
        models.append(PageModel("add_manually_a3".localized, UIImage(named:"add_manually_a3")))
        models.append(PageModel("add_manually_a4".localized, UIImage(named:"intro_5")))
        
        
        for pageModel in models{
            let singlePageViewController:SinglePageViewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "SinglePageViewController") as! SinglePageViewController
            
            singlePageViewController.labelText = pageModel.text
            singlePageViewController.uiImage = pageModel.image
            orderedViewControllers?.append(singlePageViewController)
        }
    }
    
    
    func reachedLastPage(){
//        let button1 = UIBarButtonItem(image: UIImage(named: "SaveIcon"), style: .plain, target: self, action: #selector(closeTapped))
//        self.navigationItem.rightBarButtonItem  = button1
    }
    
    func closeTapped(){
        if type == PagingType.AppsIntro{
            Preferences.put(PreferenceType.SeenIntroduction, "1")
            let vc = self.storyboard?.instantiateViewController(withIdentifier: SetupViewController.getMyClassName()) as! SetupViewController
            self.present(vc, animated: false, completion: nil)
        }
    }
    
    
    private func stylePageControl() {
        self.view.backgroundColor = UIColor.colorNavBar()
    }
    
    
    private class PageModel{
        var text:String?
        var image:UIImage?
    
        init(_ text:String?, _ image:UIImage?){
            self.text = text
            self.image = image
        }
        
    }
    
    
    
    
    
}

extension PagingViewController: UIPageViewControllerDataSource {
    
    func pageViewController(_ pageViewController: UIPageViewController,
                            viewControllerBefore viewController: UIViewController) -> UIViewController? {
        
        guard let viewControllerIndex = orderedViewControllers?.index(of: viewController as! SinglePageViewController) else {
            return nil
        }
        
        let previousIndex = viewControllerIndex - 1
        
        guard previousIndex >= 0 else {
            return nil
        }
        
        guard (orderedViewControllers?.count)! > previousIndex else {
            return nil
        }
        
        return orderedViewControllers?[previousIndex]
        
    }
    
    func pageViewController(_ pageViewController: UIPageViewController,
                            viewControllerAfter viewController: UIViewController) -> UIViewController? {
        
        guard let viewControllerIndex = orderedViewControllers?.index(of: viewController as! SinglePageViewController) else {
            return nil
        }
        
        let nextIndex = viewControllerIndex + 1
        let orderedViewControllersCount = orderedViewControllers?.count
        
        guard orderedViewControllersCount != nextIndex else {
            reachedLastPage()
            return nil
        }
        
        guard orderedViewControllersCount! > nextIndex else {
            reachedLastPage()
            return nil
        }
        
        if nextIndex + 1 > orderedViewControllersCount! - 1{
            reachedLastPage()
        }
        
        return orderedViewControllers?[nextIndex]
        
    }
    
    func presentationCount(for pageViewController: UIPageViewController) -> Int {
        return orderedViewControllers!.count
    }
    
    func presentationIndex(for pageViewController: UIPageViewController) -> Int {
        guard let firstViewController = viewControllers?.first,
            let firstViewControllerIndex = orderedViewControllers?.index(of: firstViewController as! SinglePageViewController) else {
                return 0
        }
        
        return firstViewControllerIndex

    }
    
    
}

public enum PagingType{
    case AppsIntro
    case TutorialShareKey
    case TutorialKey
}


