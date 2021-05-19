#import "OpeninstallFlutterV2Plugin.h"
#if __has_include(<openinstall_flutter_v2/openinstall_flutter_v2-Swift.h>)
#import <openinstall_flutter_v2/openinstall_flutter_v2-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "openinstall_flutter_v2-Swift.h"
#endif

@implementation OpeninstallFlutterV2Plugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftOpeninstallFlutterV2Plugin registerWithRegistrar:registrar];
}
@end
