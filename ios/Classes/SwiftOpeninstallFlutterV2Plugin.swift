import Flutter
import UIKit

public class SwiftOpeninstallFlutterV2Plugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "openinstall_flutter_v2", binaryMessenger: registrar.messenger())
    let instance = SwiftOpeninstallFlutterV2Plugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
