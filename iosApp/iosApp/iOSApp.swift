import SwiftUI
import ComposeApp
import FirebaseCore
import FirebaseMessaging
import UserNotifications

class AppDelegate: NSObject, UIApplicationDelegate, MessagingDelegate, UNUserNotificationCenterDelegate {
    
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        FirebaseApp.configure()

        // Инициализируем Koin
        IOSKoinAppKt.doInitIOSKoinApp()
        
        // Инициализируем зависимости (Clean Architecture подход)
        IOSKoinAppKt.doInitIOSKoinAppDependencies()

        // Запускаем Firebase Token Service
        IOSKoinAppKt.startIOSFirebaseTokenService()

        // Настройка Firebase Messaging
        Messaging.messaging().delegate = self

        // Настройка уведомлений
        UNUserNotificationCenter.current().delegate = self

        // Регистрация для удаленных уведомлений (ВАЖНО: сначала APNS, потом FCM)
        application.registerForRemoteNotifications()

        return true
    }

    // MARK: - MessagingDelegate

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        if let token = fcmToken {
            print("iOS: Firebase registration token: \(token)")

            // Передаем токен в KMP модуль
            IOSKoinAppKt.handleIOSNewToken(token: token)
        }
    }
    
    // MARK: - UNUserNotificationCenterDelegate
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                              willPresent notification: UNNotification,
                              withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo
        print("iOS: Received notification in foreground: \(userInfo)")
        
        // Передаем payload в KMP модуль
        IOSKoinAppKt.handleIOSPushNotification(payload: userInfo.toStringMap())
        
        completionHandler([[.alert, .sound, .badge]])
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                              didReceive response: UNNotificationResponse,
                              withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        print("iOS: User tapped notification: \(userInfo)")
        
        // Передаем payload в KMP модуль
        IOSKoinAppKt.handleIOSPushNotification(payload: userInfo.toStringMap())
        
        completionHandler()
    }
    
    // MARK: - Remote Notifications
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        print("iOS: Successfully registered for remote notifications")
        Messaging.messaging().apnsToken = deviceToken
        
        // Теперь, когда APNS токен установлен, получаем FCM токен
        Messaging.messaging().token { token, error in
            if let error = error {
                print("iOS: Error fetching FCM registration token: \(error)")
            } else if let token = token {
                print("iOS: FCM registration token: \(token)")
                IOSKoinAppKt.handleIOSCurrentToken(token: token)
            }
        }
    }
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("iOS: Failed to register for remote notifications: \(error.localizedDescription)")
    }
}

private extension Dictionary where Key == AnyHashable, Value == Any {
    func toStringMap() -> [String: String] {
        var result: [String: String] = [:]
        for (key, value) in self {
            guard let keyString = key as? String else { continue }
            if let valueString = value as? String {
                result[keyString] = valueString
            } else if let number = value as? NSNumber {
                result[keyString] = number.stringValue
            } else {
                result[keyString] = "\(value)"
            }
        }
        return result
    }
}

@main
struct iOSApp: App {
    // register app delegate for Firebase setup
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}