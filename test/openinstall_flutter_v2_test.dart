import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:openinstall_flutter_v2/openinstall_flutter_v2.dart';

void main() {
  const MethodChannel channel = MethodChannel('openinstall_flutter_v2');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await OpeninstallFlutterV2.platformVersion, '42');
  });
}
