//
//  ALCNavigationBridge.m
//  router
//
//  Created by Skylar on 2020/8/15.
//

#import "ALCNavigationBridge.h"
#import <React/RCTConvert.h>
#import "ALCNavigationManager.h"
#import <React/RCTView.h>
#import "UIViewController+ALC.h"
#import "UITabBar+DotBadge.h"
#import "ALCNavigationController.h"
#import "ALCReactViewController.h"
#import "ALCTabBarViewController.h"
#import "ALCStackModel.h"
#import "ALCGlobalStyle.h"
#import "ALCConstants.h"
#import "ALCNavigatorHelper.h"

@interface Promiss : NSObject

@property(nonatomic, copy) RCTPromiseResolveBlock resolve;
@property(nonatomic, copy) RCTPromiseRejectBlock reject;

@end

@implementation Promiss

- (instancetype)initWithResolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter {
    if (self = [super init]) {
        _resolve = resolver;
        _reject = rejecter;
    }
    return self;
}

@end

@interface  ALCNavigationBridge ()

@property (nonatomic, strong) ALCNavigationManager *manager;
@property (nonatomic, strong) ALCNavigatorHelper *helper;

@end

@implementation ALCNavigationBridge

RCT_EXPORT_MODULE(ALCNavigationBridge)

- (instancetype)init {
    if (self = [super init]) {
        _manager = [ALCNavigationManager shared];
        _helper = [[ALCNavigatorHelper alloc] init];
    }
    return self;
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

- (NSArray<NSString *> *)supportedEvents {
    return @[NAVIGATION_EVENT];
}

RCT_EXPORT_METHOD(setRoot:(NSDictionary *)rootTree) {
    [self.manager clearData];
    NSDictionary *root = rootTree[@"root"];
    UIViewController *viewController;
    if (root[@"tabs"]) {
        viewController = [self.helper createTabBarControllerWithLayout:root[@"tabs"]];
        self.helper.layoutType = ALCLayoutTypeTabs;
    } else if (root[@"stack"]) {
        viewController = [self.helper createNavigationControllerWithLayout:root[@"stack"]];
        self.helper.layoutType = ALCLayoutTypeStack;
    } else if (root[@"screen"]) {
        viewController = [self.helper createScreenControllerWithLayout:root[@"screen"]];
        self.helper.layoutType = ALCLayoutTypeScreen;
    } else {
        NSAssert(false, @"root should be tabs、 stack or screen");
    }
    UIWindow *window = RCTSharedApplication().delegate.window;
    window.rootViewController = viewController;
}

RCT_EXPORT_METHOD(setStyle:(NSDictionary *)styles) {
    [ALCGlobalStyle globalStyle].style = styles;
}

RCT_EXPORT_METHOD(signalFirstRenderComplete:(NSString *)screenID ) {
    [[NSNotificationCenter defaultCenter] postNotificationName:@"FirstRenderComplete" object:nil userInfo:@{@"screenID": screenID}];
}

RCT_EXPORT_METHOD(currentRoute:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    UINavigationController *nav = [self.helper getNavigationController];
    resolve(@{@"screenID" : nav.topViewController.screenID});
}

RCT_EXPORT_METHOD(setResult:(NSDictionary *)data) {
    self.manager.resultData = data;
}

RCT_EXPORT_METHOD(dispatch:(NSString *)screenID action:(NSString *)action page:(NSString *)pageName params:(NSDictionary *)params) {
    [self.helper handleDispatch:screenID action:action page:pageName params:params];
}

RCT_EXPORT_METHOD(registerReactComponent:(NSString *)appKey options:(NSDictionary *)options) {
    [self.manager registerReactModule:appKey options:options];
}

RCT_EXPORT_METHOD(setTabBadge:(NSArray<NSDictionary *> *)options) {
    ALCTabBarViewController *tbc = [self.helper getTabBarController];
    [tbc setTabBadge:options];
}

- (void)currentRouteWithPromiss:(Promiss *)promiss {
    UINavigationController *nav = [self.helper getNavigationController];
    if (nav) {
        promiss.resolve(@{@"screenID" : nav.topViewController.screenID});
    } else {
        NSLog(@"looping for route");
        [self performSelector:@selector(currentRouteWithPromiss:) withObject:promiss afterDelay:0.016];
    }
}

@end
