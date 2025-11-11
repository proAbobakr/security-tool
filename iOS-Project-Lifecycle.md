# iOS Project Lifecycle with SwiftUI

## Table of Contents
1. [Introduction](#introduction)
2. [App Lifecycle](#app-lifecycle)
3. [View Lifecycle](#view-lifecycle)
4. [Scene Lifecycle](#scene-lifecycle)
5. [Development Lifecycle](#development-lifecycle)
6. [Build and Deployment Lifecycle](#build-and-deployment-lifecycle)

---

## Introduction

The iOS project lifecycle encompasses multiple layers: the application lifecycle, view lifecycle, scene lifecycle, and the development/deployment lifecycle. In SwiftUI, Apple introduced a declarative approach that changes how we handle lifecycle events compared to UIKit.

---

## App Lifecycle

### SwiftUI App Structure

Starting with iOS 14+, SwiftUI introduced the `@main` App protocol, which replaces the traditional AppDelegate pattern:

```swift
@main
struct MyApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

### App Lifecycle States

iOS apps transition through several states:

1. **Not Running**: App hasn't been launched or was terminated
2. **Inactive**: App is in foreground but not receiving events (e.g., during interruptions)
3. **Active**: App is in foreground and receiving events
4. **Background**: App is executing code but not visible
5. **Suspended**: App is in memory but not executing code

### Monitoring App Lifecycle in SwiftUI

#### Using Scene Phase

```swift
@main
struct MyApp: App {
    @Environment(\.scenePhase) private var scenePhase

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .onChange(of: scenePhase) { oldPhase, newPhase in
            switch newPhase {
            case .active:
                print("App became active")
            case .inactive:
                print("App became inactive")
            case .background:
                print("App moved to background")
            @unknown default:
                print("Unknown scene phase")
            }
        }
    }
}
```

#### Using AppDelegate (When Needed)

For legacy support or specific requirements:

```swift
class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        print("App launched")
        return true
    }

    func applicationWillTerminate(_ application: UIApplication) {
        print("App will terminate")
    }
}

@main
struct MyApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

---

## View Lifecycle

### SwiftUI View Lifecycle Events

Unlike UIKit's `viewDidLoad`, `viewWillAppear`, etc., SwiftUI uses modifiers:

#### 1. **onAppear**
Called when a view appears on screen:

```swift
struct ContentView: View {
    var body: some View {
        Text("Hello, World!")
            .onAppear {
                print("View appeared")
                // Fetch data, start animations, etc.
            }
    }
}
```

#### 2. **onDisappear**
Called when a view is removed from the view hierarchy:

```swift
struct ContentView: View {
    var body: some View {
        Text("Hello, World!")
            .onDisappear {
                print("View disappeared")
                // Clean up resources, stop timers, etc.
            }
    }
}
```

#### 3. **task**
Modern async/await lifecycle management (iOS 15+):

```swift
struct ContentView: View {
    @State private var data: [String] = []

    var body: some View {
        List(data, id: \.self) { item in
            Text(item)
        }
        .task {
            // Automatically cancelled when view disappears
            await fetchData()
        }
    }

    func fetchData() async {
        // Async work here
    }
}
```

### View Body Evaluation

SwiftUI's `body` is evaluated when:
- State variables change (`@State`, `@Binding`)
- Observable objects publish changes (`@ObservedObject`, `@StateObject`, `@EnvironmentObject`)
- Environment values change
- Parent view is re-evaluated

```swift
struct ContentView: View {
    @State private var counter = 0

    var body: some View {
        // This body is re-evaluated every time counter changes
        VStack {
            Text("Counter: \(counter)")
            Button("Increment") {
                counter += 1  // Triggers body re-evaluation
            }
        }
    }
}
```

### State Management Lifecycle

#### @State
Local view state, owned by the view:

```swift
struct CounterView: View {
    @State private var count = 0  // Initialized once, persists across view updates

    var body: some View {
        Text("\(count)")
    }
}
```

#### @StateObject
Reference type state owned by the view (iOS 14+):

```swift
class ViewModel: ObservableObject {
    @Published var data: String = ""
}

struct ContentView: View {
    @StateObject private var viewModel = ViewModel()  // Created once, survives view updates

    var body: some View {
        Text(viewModel.data)
    }
}
```

#### @ObservedObject
Reference to external observable object:

```swift
struct DetailView: View {
    @ObservedObject var viewModel: ViewModel  // Passed from parent, may be recreated

    var body: some View {
        Text(viewModel.data)
    }
}
```

---

## Scene Lifecycle

### Understanding Scenes

Scenes represent instances of your app's UI. In iPadOS and macOS, users can create multiple windows (scenes) of the same app.

```swift
@main
struct MyApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }

        #if os(macOS)
        Settings {
            SettingsView()
        }
        #endif
    }
}
```

### Scene Phase Monitoring

```swift
struct ContentView: View {
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        Text("Hello")
            .onChange(of: scenePhase) { oldPhase, newPhase in
                if newPhase == .active {
                    print("Scene is now active")
                } else if newPhase == .background {
                    print("Scene moved to background - save data")
                }
            }
    }
}
```

---

## Development Lifecycle

### 1. Project Setup

```
Create Project → Configure Bundle ID → Set Deployment Target → Add Capabilities
```

**Key Configuration Files:**
- `Info.plist`: App metadata, permissions, configurations
- `Entitlements`: App capabilities (Push Notifications, iCloud, etc.)
- `Config.xcconfig`: Build configuration settings

### 2. Development Phases

#### Phase 1: Initial Setup
```swift
// 1. Define your app entry point
@main
struct MyApp: App {
    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}

// 2. Set up your data models
struct User: Codable, Identifiable {
    let id: UUID
    var name: String
}

// 3. Create view models
class UserViewModel: ObservableObject {
    @Published var users: [User] = []
}
```

#### Phase 2: Building Features
- Implement views using SwiftUI components
- Create view models for business logic
- Set up navigation (NavigationStack, NavigationSplitView)
- Add data persistence (UserDefaults, CoreData, SwiftData)

#### Phase 3: Testing
```swift
import XCTest
@testable import MyApp

class MyAppTests: XCTestCase {
    func testUserCreation() {
        let user = User(id: UUID(), name: "Test")
        XCTAssertEqual(user.name, "Test")
    }
}
```

#### Phase 4: Optimization
- Profile with Instruments
- Optimize view updates
- Reduce body evaluations
- Implement lazy loading

### 3. Preview Development

SwiftUI previews accelerate development:

```swift
struct ContentView: View {
    var body: some View {
        Text("Hello, World!")
    }
}

#Preview {
    ContentView()
}

#Preview("Dark Mode") {
    ContentView()
        .preferredColorScheme(.dark)
}

#Preview("Different Data") {
    ContentView()
        .environment(\.locale, Locale(identifier: "es"))
}
```

---

## Build and Deployment Lifecycle

### 1. Build Process

```
Source Code → Compilation → Linking → Code Signing → Archive
```

#### Build Configurations
- **Debug**: Includes debugging symbols, no optimization
- **Release**: Optimized, stripped symbols, ready for distribution

#### Schemes and Targets
```
Scheme: Defines build configuration
Target: Defines the product (App, Extension, Framework)
```

### 2. Code Signing

Every iOS app must be signed:

```
Developer Certificate + Provisioning Profile → Signed App
```

**Provisioning Profile Types:**
- **Development**: For testing on registered devices
- **Ad Hoc**: For distributing to specific devices
- **App Store**: For App Store distribution
- **Enterprise**: For internal distribution (Enterprise accounts)

### 3. Testing Phases

#### Unit Testing
```swift
func testViewModel() async {
    let viewModel = ViewModel()
    await viewModel.fetchData()
    XCTAssertFalse(viewModel.data.isEmpty)
}
```

#### UI Testing
```swift
func testLoginFlow() {
    let app = XCUIApplication()
    app.launch()

    app.textFields["username"].tap()
    app.textFields["username"].typeText("testuser")
    app.buttons["Login"].tap()

    XCTAssertTrue(app.staticTexts["Welcome"].exists)
}
```

#### TestFlight
- Internal testing (up to 100 testers)
- External testing (up to 10,000 testers)
- Collect crash reports and feedback

### 4. App Store Submission

```
Archive → Upload to App Store Connect → Add Metadata → Submit for Review → Release
```

**Review Process Steps:**
1. **Waiting for Review**: In queue
2. **In Review**: Apple is reviewing
3. **Pending Developer Release**: Approved, waiting for release
4. **Ready for Sale**: Live on App Store
5. **Rejected**: Needs fixes

### 5. Post-Release

#### Monitoring
- Crash reports (Xcode Organizer, Crashlytics)
- Analytics (App Store Connect, third-party services)
- User reviews and ratings

#### Updates
```
Fix Issues → Version Bump → Test → Submit Update → Release
```

**Versioning:**
- **Version Number**: 1.0.0 (major.minor.patch)
- **Build Number**: Incremental (1, 2, 3, ...)

---

## Best Practices

### 1. Lifecycle Management

**DO:**
- Use `onAppear` for data fetching
- Use `onDisappear` for cleanup
- Monitor `scenePhase` for app state changes
- Use `task` for async operations (automatically cancelled)

**DON'T:**
- Perform heavy operations in view initializers
- Forget to cancel timers and observers
- Ignore memory warnings
- Block the main thread

### 2. State Management

```swift
// Good: StateObject for owned objects
@StateObject private var viewModel = ViewModel()

// Good: ObservedObject for passed objects
@ObservedObject var sharedViewModel: SharedViewModel

// Good: State for simple values
@State private var isPresented = false

// Bad: Creating ObservableObject in body
var body: some View {
    let viewModel = ViewModel()  // ❌ Created every render!
    return Text(viewModel.data)
}
```

### 3. Performance

```swift
// Use lazy loading for lists
ScrollView {
    LazyVStack {
        ForEach(items) { item in
            ItemView(item: item)
        }
    }
}

// Avoid unnecessary view updates
struct OptimizedView: View {
    let staticData: String  // Won't cause re-renders

    var body: some View {
        Text(staticData)
            .id(staticData)  // Cache based on data
    }
}
```

---

## Common Patterns

### 1. Dependency Injection

```swift
@main
struct MyApp: App {
    @StateObject private var dataManager = DataManager()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(dataManager)
        }
    }
}

struct ContentView: View {
    @EnvironmentObject var dataManager: DataManager

    var body: some View {
        Text("Data: \(dataManager.info)")
    }
}
```

### 2. Navigation Lifecycle

```swift
struct AppRoot: View {
    @State private var navigationPath = NavigationPath()

    var body: some View {
        NavigationStack(path: $navigationPath) {
            HomeView()
                .navigationDestination(for: User.self) { user in
                    UserDetailView(user: user)
                }
        }
        .onAppear {
            // Set up deep linking
        }
    }
}
```

### 3. Data Persistence

```swift
class DataManager: ObservableObject {
    @Published var users: [User] = []

    init() {
        loadData()
    }

    func loadData() {
        // Load from persistence layer
        if let data = UserDefaults.standard.data(forKey: "users") {
            users = (try? JSONDecoder().decode([User].self, from: data)) ?? []
        }
    }

    func saveData() {
        if let data = try? JSONEncoder().encode(users) {
            UserDefaults.standard.set(data, forKey: "users")
        }
    }
}
```

---

## Conclusion

Understanding the iOS project lifecycle in SwiftUI requires knowledge of:

1. **App Lifecycle**: How the app transitions between states
2. **View Lifecycle**: When views appear, disappear, and update
3. **Scene Lifecycle**: How multiple windows/scenes are managed
4. **Development Lifecycle**: From project setup to App Store submission

SwiftUI's declarative nature simplifies many lifecycle aspects, but understanding the underlying mechanisms is crucial for building robust, performant applications.

---

## Additional Resources

- [Apple's SwiftUI Documentation](https://developer.apple.com/documentation/swiftui)
- [App Distribution Guide](https://developer.apple.com/distribute/)
- [Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines)
- [WWDC Sessions on SwiftUI](https://developer.apple.com/wwdc/)
