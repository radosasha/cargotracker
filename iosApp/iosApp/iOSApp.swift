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

        // Получаем текущий токен и передаем в KMP модуль
        Messaging.messaging().token { token, error in
            if let error = error {
                print("iOS: Error fetching FCM registration token: \(error)")
            } else if let token = token {
                print("iOS: FCM registration token: \(token)")
                IOSKoinAppKt.handleIOSCurrentToken(token: token)
            }
        }

        // Настройка уведомлений
        UNUserNotificationCenter.current().delegate = self

        // Регистрация для удаленных уведомлений
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
        print("iOS: Received notification in foreground: \(notification.request.content.userInfo)")
        
        // TODO: Передать уведомление в KMP модуль
        // Пока просто логируем
        
        completionHandler([[.alert, .sound, .badge]])
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                              didReceive response: UNNotificationResponse,
                              withCompletionHandler completionHandler: @escaping () -> Void) {
        print("iOS: User tapped notification: \(response.notification.request.content.userInfo)")
        
        // TODO: Передать уведомление в KMP модуль
        // Пока просто логируем
        
        completionHandler()
    }
    
    // MARK: - Remote Notifications
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        print("iOS: Successfully registered for remote notifications")
        Messaging.messaging().apnsToken = deviceToken
    }
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("iOS: Failed to register for remote notifications: \(error.localizedDescription)")
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