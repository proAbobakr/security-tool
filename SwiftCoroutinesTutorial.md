# Swift Coroutines Tutorial: Complete Guide to Async/Await Concurrency

Swift's async/await concurrency model (introduced in Swift 5.5) is the equivalent of coroutines in other languages. This tutorial covers all types of Swift concurrency primitives.

## Table of Contents
1. [Basic Async/Await](#1-basic-asyncawait)
2. [Tasks](#2-tasks)
3. [Async Let](#3-async-let)
4. [Task Groups](#4-task-groups)
5. [Async Sequences](#5-async-sequences)
6. [Actors](#6-actors)
7. [Continuations](#7-continuations)
8. [Main Actor](#8-main-actor)
9. [Task Cancellation](#9-task-cancellation)
10. [Task Priority](#10-task-priority)

---

## 1. Basic Async/Await

The foundation of Swift concurrency. Async functions can suspend execution and resume later.

### Defining Async Functions

```swift
// Basic async function
func fetchUserData(id: Int) async -> User {
    // Simulates network delay
    try? await Task.sleep(nanoseconds: 1_000_000_000)
    return User(id: id, name: "User \(id)")
}

// Async function that can throw errors
func fetchUserDataOrFail(id: Int) async throws -> User {
    guard id > 0 else {
        throw UserError.invalidID
    }
    try await Task.sleep(nanoseconds: 1_000_000_000)
    return User(id: id, name: "User \(id)")
}
```

### Calling Async Functions

```swift
func loadUser() async {
    // Use 'await' to call async functions
    let user = await fetchUserData(id: 1)
    print("Loaded: \(user.name)")

    // Handle throwing async functions
    do {
        let user2 = try await fetchUserDataOrFail(id: 2)
        print("Loaded: \(user2.name)")
    } catch {
        print("Error: \(error)")
    }
}
```

### Sequential vs Concurrent Execution

```swift
// Sequential - operations happen one after another
func loadUsersSequentially() async {
    let user1 = await fetchUserData(id: 1)  // Wait for this
    let user2 = await fetchUserData(id: 2)  // Then wait for this
    print("Users: \(user1.name), \(user2.name)")
}

// See 'Async Let' section for concurrent execution
```

---

## 2. Tasks

Tasks are the fundamental unit of concurrency in Swift. They represent independent work that can run concurrently.

### 2.1 Unstructured Tasks

Unstructured tasks run independently and outlive their creation scope.

```swift
// Task.init - Creates a new unstructured task
func startBackgroundWork() {
    Task {
        let result = await performLongOperation()
        print("Result: \(result)")
    }
    // Function returns immediately, task continues in background
}

// Task with priority
Task(priority: .high) {
    await processUrgentData()
}

// Detached Task - inherits nothing from parent context
Task.detached {
    // Completely independent execution
    await performIndependentWork()
}
```

### 2.2 Structured Tasks

Structured tasks create a parent-child relationship ensuring proper cleanup.

```swift
func processData() async {
    await withTaskGroup(of: Int.self) { group in
        // Child tasks are automatically cancelled when parent completes
        group.addTask { await process(1) }
        group.addTask { await process(2) }

        for await result in group {
            print("Got result: \(result)")
        }
    }
    // All child tasks are guaranteed to complete before this point
}
```

### 2.3 Task Handle

```swift
func manageTasks() {
    // Store task handle for control
    let task = Task<String, Never> {
        await performOperation()
        return "Done"
    }

    // Get result later
    Task {
        let result = await task.value
        print(result)
    }

    // Cancel if needed
    task.cancel()
}
```

---

## 3. Async Let

Async let enables concurrent execution of multiple async operations with automatic waiting.

### Basic Async Let

```swift
func loadMultipleResources() async {
    // Start multiple operations concurrently
    async let user = fetchUserData(id: 1)
    async let posts = fetchUserPosts(userID: 1)
    async let comments = fetchUserComments(userID: 1)

    // Operations run in parallel until we await them
    let userData = await user
    let postData = await posts
    let commentData = await comments

    print("Loaded \(postData.count) posts and \(commentData.count) comments")
}
```

### Error Handling with Async Let

```swift
func loadWithErrorHandling() async {
    do {
        async let user = try fetchUserDataOrFail(id: 1)
        async let profile = try fetchUserProfile(id: 1)

        // Both must succeed or throws error
        let (userData, profileData) = try await (user, profile)
        print("User: \(userData), Profile: \(profileData)")
    } catch {
        print("Failed to load data: \(error)")
    }
}
```

### Combining Results

```swift
func combineData() async -> UserDashboard {
    async let user = fetchUserData(id: 1)
    async let statistics = fetchStatistics(id: 1)
    async let notifications = fetchNotifications(id: 1)

    // Wait for all and combine
    return await UserDashboard(
        user: user,
        stats: statistics,
        notifications: notifications
    )
}
```

---

## 4. Task Groups

Task groups allow dynamic creation of child tasks and collecting their results.

### 4.1 Basic Task Group

```swift
func processMultipleItems() async -> [Result] {
    await withTaskGroup(of: Result.self) { group in
        var results: [Result] = []

        // Add tasks dynamically
        for id in 1...10 {
            group.addTask {
                await processItem(id: id)
            }
        }

        // Collect results as they complete
        for await result in group {
            results.append(result)
        }

        return results
    }
}
```

### 4.2 Throwing Task Group

```swift
func processWithErrors() async throws -> [Result] {
    try await withThrowingTaskGroup(of: Result.self) { group in
        var results: [Result] = []

        for id in 1...5 {
            group.addTask {
                try await processItemOrFail(id: id)
            }
        }

        // If any task throws, entire group throws
        for try await result in group {
            results.append(result)
        }

        return results
    }
}
```

### 4.3 Limiting Concurrent Tasks

```swift
func processBatch(items: [Item], maxConcurrent: Int) async -> [Result] {
    await withTaskGroup(of: Result.self) { group in
        var results: [Result] = []
        var itemIterator = items.makeIterator()
        var activeTasks = 0

        // Start initial batch
        while activeTasks < maxConcurrent, let item = itemIterator.next() {
            group.addTask { await process(item) }
            activeTasks += 1
        }

        // Process remaining items as tasks complete
        for await result in group {
            results.append(result)

            // Start next task if available
            if let nextItem = itemIterator.next() {
                group.addTask { await process(nextItem) }
            }
        }

        return results
    }
}
```

### 4.4 Early Exit from Task Groups

```swift
func findFirstMatch() async -> Item? {
    await withTaskGroup(of: Item?.self) { group in
        for id in 1...100 {
            group.addTask {
                await searchForItem(id: id)
            }
        }

        // Return first match found
        for await item in group {
            if let item = item {
                group.cancelAll()  // Cancel remaining tasks
                return item
            }
        }

        return nil
    }
}
```

---

## 5. Async Sequences

Async sequences allow iteration over values that arrive asynchronously over time.

### 5.1 Basic Async Sequence

```swift
// Consuming an async sequence
func consumeAsyncSequence() async {
    let stream = NetworkStream()

    for await message in stream {
        print("Received: \(message)")
    }
}

// Breaking from async sequence
func consumeUntilCondition() async {
    for await message in networkStream {
        if message.isComplete {
            break
        }
        process(message)
    }
}
```

### 5.2 Creating Custom Async Sequence

```swift
struct CountdownSequence: AsyncSequence {
    typealias Element = Int
    let start: Int

    struct AsyncIterator: AsyncIteratorProtocol {
        var current: Int

        mutating func next() async -> Int? {
            guard current > 0 else { return nil }

            try? await Task.sleep(nanoseconds: 1_000_000_000)
            defer { current -= 1 }
            return current
        }
    }

    func makeAsyncIterator() -> AsyncIterator {
        AsyncIterator(current: start)
    }
}

// Usage
func countdown() async {
    for await number in CountdownSequence(start: 5) {
        print(number)
    }
}
```

### 5.3 AsyncStream

AsyncStream provides an easy way to create async sequences from callbacks or events.

```swift
// Creating AsyncStream
func temperatureStream() -> AsyncStream<Double> {
    AsyncStream { continuation in
        let sensor = TemperatureSensor()

        sensor.onUpdate = { temperature in
            continuation.yield(temperature)
        }

        sensor.onComplete = {
            continuation.finish()
        }

        continuation.onTermination = { @Sendable _ in
            sensor.stop()
        }

        sensor.start()
    }
}

// Using AsyncStream
func monitorTemperature() async {
    for await temp in temperatureStream() {
        print("Temperature: \(temp)°C")

        if temp > 100 {
            print("Warning: Too hot!")
            break
        }
    }
}
```

### 5.4 AsyncThrowingStream

```swift
func networkEventStream() -> AsyncThrowingStream<Event, Error> {
    AsyncThrowingStream { continuation in
        let connection = NetworkConnection()

        connection.onEvent = { event in
            continuation.yield(event)
        }

        connection.onError = { error in
            continuation.finish(throwing: error)
        }

        connection.connect()
    }
}

// Usage with error handling
func handleNetworkEvents() async {
    do {
        for try await event in networkEventStream() {
            process(event)
        }
    } catch {
        print("Network error: \(error)")
    }
}
```

### 5.5 Async Sequence Operators

```swift
func transformAsyncSequence() async {
    let numbers = AsyncStream<Int> { continuation in
        for i in 1...10 {
            continuation.yield(i)
        }
        continuation.finish()
    }

    // Map
    for await doubled in numbers.map({ $0 * 2 }) {
        print(doubled)
    }

    // Filter
    for await even in numbers.filter({ $0 % 2 == 0 }) {
        print(even)
    }

    // CompactMap
    for await result in numbers.compactMap({ $0 > 5 ? $0 : nil }) {
        print(result)
    }
}
```

---

## 6. Actors

Actors provide thread-safe mutable state by ensuring only one task can access their mutable state at a time.

### 6.1 Basic Actor

```swift
actor BankAccount {
    private var balance: Double = 0

    func deposit(amount: Double) {
        balance += amount
    }

    func withdraw(amount: Double) -> Bool {
        guard balance >= amount else {
            return false
        }
        balance -= amount
        return true
    }

    func getBalance() -> Double {
        return balance
    }
}

// Usage
func performTransaction() async {
    let account = BankAccount()

    // All actor calls require await
    await account.deposit(amount: 100)
    let success = await account.withdraw(amount: 50)
    let balance = await account.getBalance()

    print("Balance: \(balance)")
}
```

### 6.2 Actor Isolation

```swift
actor DataCache {
    private var cache: [String: Data] = [:]

    // Isolated to actor - safe to access mutable state
    func store(_ data: Data, forKey key: String) {
        cache[key] = data
    }

    // Isolated to actor
    func retrieve(forKey key: String) -> Data? {
        return cache[key]
    }

    // Non-isolated - doesn't access mutable state
    nonisolated func generateCacheKey(for url: URL) -> String {
        return url.absoluteString.hash.description
    }
}

// Usage
func cacheData() async {
    let cache = DataCache()

    // Non-isolated call - no await needed
    let key = cache.generateCacheKey(for: someURL)

    // Isolated calls - await required
    await cache.store(data, forKey: key)
    let retrieved = await cache.retrieve(forKey: key)
}
```

### 6.3 Actor with Async Methods

```swift
actor ImageProcessor {
    private var processedImages: [String: UIImage] = [:]

    func processImage(at url: URL) async throws -> UIImage {
        // Check cache first
        let cacheKey = url.absoluteString
        if let cached = processedImages[cacheKey] {
            return cached
        }

        // Load and process (async operations)
        let data = try await URLSession.shared.data(from: url).0
        guard let image = UIImage(data: data) else {
            throw ProcessingError.invalidImage
        }

        let processed = await applyFilters(to: image)

        // Cache result
        processedImages[cacheKey] = processed
        return processed
    }

    private func applyFilters(to image: UIImage) async -> UIImage {
        // Expensive processing
        try? await Task.sleep(nanoseconds: 1_000_000_000)
        return image
    }
}
```

### 6.4 Actor Reentrancy

```swift
actor Counter {
    private var value = 0

    func increment() async {
        // Potential suspension point
        await Task.yield()

        // Value might have changed during suspension!
        value += 1
    }

    func safeIncrement() async {
        // Capture current value before suspension
        let current = value
        await Task.yield()

        // Use captured value
        value = current + 1
    }
}
```

### 6.5 Global Actors

```swift
@globalActor
actor DatabaseActor {
    static let shared = DatabaseActor()
}

// Mark types or functions with global actor
@DatabaseActor
class DatabaseManager {
    var connection: DatabaseConnection?

    func executeQuery(_ query: String) -> Result {
        // All access is isolated to DatabaseActor
        return connection?.execute(query) ?? .empty
    }
}

// Usage
func queryDatabase() async {
    let manager = DatabaseManager()

    // Calls to DatabaseManager are isolated to DatabaseActor
    let result = await manager.executeQuery("SELECT * FROM users")
}
```

---

## 7. Continuations

Continuations bridge callback-based APIs with async/await.

### 7.1 Checked Continuation

```swift
// Convert callback-based API to async
func fetchUserCallback(id: Int, completion: @escaping (User?, Error?) -> Void) {
    // Legacy callback API
    NetworkManager.shared.getUser(id: id, completion: completion)
}

func fetchUserAsync(id: Int) async throws -> User {
    try await withCheckedThrowingContinuation { continuation in
        fetchUserCallback(id: id) { user, error in
            if let error = error {
                continuation.resume(throwing: error)
            } else if let user = user {
                continuation.resume(returning: user)
            } else {
                continuation.resume(throwing: NetworkError.unknown)
            }
        }
    }
}
```

### 7.2 Unsafe Continuation

```swift
// For performance-critical code where you guarantee correct usage
func fetchUserUnsafe(id: Int) async throws -> User {
    try await withUnsafeThrowingContinuation { continuation in
        NetworkManager.shared.getUser(id: id) { user, error in
            if let error = error {
                continuation.resume(throwing: error)
            } else if let user = user {
                continuation.resume(returning: user)
            }
        }
    }
}
```

### 7.3 Non-Throwing Continuation

```swift
func waitForNotification(named name: Notification.Name) async -> Notification {
    await withCheckedContinuation { continuation in
        var observer: NSObjectProtocol?

        observer = NotificationCenter.default.addObserver(
            forName: name,
            object: nil,
            queue: nil
        ) { notification in
            continuation.resume(returning: notification)
            if let observer = observer {
                NotificationCenter.default.removeObserver(observer)
            }
        }
    }
}

// Usage
func handleNotification() async {
    let notification = await waitForNotification(named: .userDidLogin)
    print("User logged in: \(notification)")
}
```

### 7.4 Continuation Best Practices

```swift
// ❌ WRONG - Resume must be called exactly once
func incorrectContinuation() async -> String {
    await withCheckedContinuation { continuation in
        if someCondition {
            continuation.resume(returning: "A")
            continuation.resume(returning: "B")  // CRASH! Double resume
        }
        // Missing resume in else branch!
    }
}

// ✅ CORRECT - Resume exactly once on all paths
func correctContinuation() async -> String {
    await withCheckedContinuation { continuation in
        if someCondition {
            continuation.resume(returning: "A")
        } else {
            continuation.resume(returning: "B")
        }
    }
}
```

---

## 8. Main Actor

The MainActor ensures code runs on the main thread, essential for UI updates.

### 8.1 Basic Main Actor Usage

```swift
// Mark entire class for main actor
@MainActor
class ViewController: UIViewController {
    var label: UILabel!

    // All methods run on main thread
    func updateUI() {
        label.text = "Updated"
    }

    // Can still call async functions
    func loadData() async {
        let data = await fetchData()  // Runs on background
        updateLabel(with: data)        // Runs on main thread
    }

    func updateLabel(with data: Data) {
        // Safe to update UI directly
        label.text = String(data: data, encoding: .utf8)
    }
}
```

### 8.2 Isolated Methods

```swift
class DataProcessor {
    // This specific method runs on main actor
    @MainActor
    func updateProgress(_ progress: Double) {
        progressBar.progress = progress
    }

    // Other methods run on background
    func processData() async {
        for i in 0...100 {
            await heavyComputation(i)

            // UI update on main actor
            await updateProgress(Double(i) / 100.0)
        }
    }
}
```

### 8.3 Calling Main Actor Code

```swift
func backgroundWork() async {
    // This runs on background
    let result = await performComputation()

    // Explicitly run on main actor
    await MainActor.run {
        updateUILabel(with: result)
    }
}
```

### 8.4 Main Actor Isolation

```swift
@MainActor
class UIViewModel {
    var data: [Item] = []

    // Isolated to main actor
    func updateData(_ newData: [Item]) {
        data = newData
    }

    // Non-isolated - can be called from any context
    nonisolated func processInBackground() async {
        let processed = await heavyProcessing()

        // Must use await to call isolated method
        await updateData(processed)
    }
}
```

### 8.5 SwiftUI with Main Actor

```swift
@MainActor
class AppViewModel: ObservableObject {
    @Published var items: [Item] = []
    @Published var isLoading = false

    func loadItems() async {
        isLoading = true

        // Network call on background
        let fetchedItems = await APIClient.shared.fetchItems()

        // UI updates automatically on main thread
        items = fetchedItems
        isLoading = false
    }
}
```

---

## 9. Task Cancellation

Proper cancellation handling ensures resources are cleaned up correctly.

### 9.1 Checking for Cancellation

```swift
func processLargeDataset() async throws {
    for item in largeDataset {
        // Check if cancelled
        try Task.checkCancellation()

        await process(item)
    }
}

// Alternative: Manual check
func processWithManualCheck() async {
    for item in dataset {
        if Task.isCancelled {
            print("Task cancelled, cleaning up...")
            cleanup()
            return
        }

        await process(item)
    }
}
```

### 9.2 Cancelling Tasks

```swift
func startCancellableWork() -> Task<String, Never> {
    Task {
        do {
            for i in 1...100 {
                try Task.checkCancellation()
                await processItem(i)
            }
            return "Completed"
        } catch {
            return "Cancelled"
        }
    }
}

// Usage
func manageWork() async {
    let task = startCancellableWork()

    // Cancel after 2 seconds
    Task {
        try? await Task.sleep(nanoseconds: 2_000_000_000)
        task.cancel()
    }

    let result = await task.value
    print(result)  // Might print "Cancelled"
}
```

### 9.3 Task Group Cancellation

```swift
func processWithCancellation() async {
    await withTaskGroup(of: Void.self) { group in
        for i in 1...10 {
            group.addTask {
                do {
                    try await processItem(i)
                } catch {
                    // Cancel all tasks on error
                    group.cancelAll()
                }
            }
        }
    }
}
```

### 9.4 Cooperative Cancellation

```swift
actor FileProcessor {
    func processFiles(_ files: [URL]) async throws {
        for file in files {
            // Cooperative cancellation check
            try Task.checkCancellation()

            let data = try await loadFile(file)
            try await processData(data)
        }
    }

    func loadFile(_ url: URL) async throws -> Data {
        // URLSession respects task cancellation automatically
        let (data, _) = try await URLSession.shared.data(from: url)
        return data
    }
}
```

### 9.5 Cleanup on Cancellation

```swift
func downloadWithCleanup(url: URL) async throws -> Data {
    let task = URLSession.shared.dataTask(with: url)

    return try await withTaskCancellationHandler {
        try await URLSession.shared.data(from: url).0
    } onCancel: {
        task.cancel()
        print("Download cancelled, cleaning up...")
    }
}
```

---

## 10. Task Priority

Task priority helps the system schedule work appropriately.

### 10.1 Priority Levels

```swift
// Available priorities (highest to lowest):
// - .high
// - .medium (default)
// - .low
// - .background

func processByPriority() {
    // High priority - user interaction
    Task(priority: .high) {
        await updateUI()
    }

    // Medium priority - default
    Task(priority: .medium) {
        await loadData()
    }

    // Low priority - deferrable work
    Task(priority: .low) {
        await prefetchContent()
    }

    // Background priority - maintenance
    Task(priority: .background) {
        await cleanupCache()
    }
}
```

### 10.2 Priority Inheritance

```swift
func demonstratePriorityInheritance() async {
    // Task inherits priority from context
    await withTaskGroup(of: Void.self) { group in
        group.addTask(priority: .high) {
            // This child task has high priority
            await criticalWork()
        }

        group.addTask {
            // Inherits parent's priority
            await regularWork()
        }
    }
}
```

### 10.3 Checking Task Priority

```swift
func adaptToPriority() async {
    let currentPriority = Task.currentPriority

    switch currentPriority {
    case .high:
        await performOptimizedProcessing()
    case .low, .background:
        await performBatchProcessing()
    default:
        await performNormalProcessing()
    }
}
```

### 10.4 Priority in Task Groups

```swift
func processWithPriorities() async {
    await withTaskGroup(of: Result.self) { group in
        // Urgent items with high priority
        for urgentItem in urgentItems {
            group.addTask(priority: .high) {
                await process(urgentItem)
            }
        }

        // Regular items with normal priority
        for item in regularItems {
            group.addTask(priority: .medium) {
                await process(item)
            }
        }

        // Low priority background work
        for backgroundItem in backgroundItems {
            group.addTask(priority: .background) {
                await process(backgroundItem)
            }
        }

        for await result in group {
            handle(result)
        }
    }
}
```

---

## Complete Example: Building a Concurrent Image Downloader

Here's a comprehensive example combining multiple concepts:

```swift
actor ImageDownloader {
    private var cache: [URL: UIImage] = [:]
    private var pendingDownloads: [URL: Task<UIImage, Error>] = [:]

    @MainActor
    func downloadImages(from urls: [URL], maxConcurrent: Int = 3) async throws -> [UIImage] {
        try await withThrowingTaskGroup(of: (Int, UIImage).self) { group in
            var results: [Int: UIImage] = [:]
            var iterator = urls.enumerated().makeIterator()
            var activeCount = 0

            // Start initial batch
            while activeCount < maxConcurrent, let (index, url) = iterator.next() {
                group.addTask {
                    let image = try await self.downloadImage(from: url)
                    return (index, image)
                }
                activeCount += 1
            }

            // Process results and start new downloads
            for try await (index, image) in group {
                results[index] = image

                // Check for cancellation
                try Task.checkCancellation()

                // Start next download if available
                if let (nextIndex, nextURL) = iterator.next() {
                    group.addTask {
                        let image = try await self.downloadImage(from: nextURL)
                        return (nextIndex, image)
                    }
                }
            }

            // Return images in original order
            return urls.indices.compactMap { results[$0] }
        }
    }

    private func downloadImage(from url: URL) async throws -> UIImage {
        // Check cache
        if let cached = cache[url] {
            return cached
        }

        // Check if download already in progress
        if let pending = pendingDownloads[url] {
            return try await pending.value
        }

        // Start new download
        let task = Task<UIImage, Error> {
            try await performDownload(from: url)
        }

        pendingDownloads[url] = task

        do {
            let image = try await task.value
            cache[url] = image
            pendingDownloads.removeValue(forKey: url)
            return image
        } catch {
            pendingDownloads.removeValue(forKey: url)
            throw error
        }
    }

    private func performDownload(from url: URL) async throws -> UIImage {
        let (data, response) = try await URLSession.shared.data(from: url)

        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw DownloadError.invalidResponse
        }

        guard let image = UIImage(data: data) else {
            throw DownloadError.invalidImage
        }

        return image
    }

    func clearCache() {
        cache.removeAll()
    }
}

// Usage
@MainActor
class ImageGalleryViewModel: ObservableObject {
    @Published var images: [UIImage] = []
    @Published var isLoading = false

    private let downloader = ImageDownloader()

    func loadGallery(urls: [URL]) async {
        isLoading = true
        defer { isLoading = false }

        do {
            images = try await downloader.downloadImages(from: urls, maxConcurrent: 5)
        } catch {
            print("Failed to load images: \(error)")
        }
    }
}
```

---

## Best Practices Summary

1. **Use structured concurrency when possible** - Prefer `async let` and `withTaskGroup` over unstructured `Task`
2. **Mark UI code with @MainActor** - Ensure UI updates happen on the main thread
3. **Use actors for shared mutable state** - Instead of locks and semaphores
4. **Handle cancellation cooperatively** - Check `Task.isCancelled` or use `Task.checkCancellation()`
5. **Set appropriate priorities** - Help the system schedule work efficiently
6. **Use continuations to bridge legacy code** - But ensure exactly one resume
7. **Leverage async sequences for streams** - Clean async iteration over time-based data
8. **Test async code thoroughly** - Race conditions can be subtle

---

## Common Patterns

### Pattern 1: Retry with Exponential Backoff

```swift
func fetchWithRetry<T>(
    maxAttempts: Int = 3,
    operation: @escaping () async throws -> T
) async throws -> T {
    var attempt = 0

    while true {
        do {
            return try await operation()
        } catch {
            attempt += 1

            if attempt >= maxAttempts {
                throw error
            }

            let delay = UInt64(pow(2.0, Double(attempt)) * 1_000_000_000)
            try await Task.sleep(nanoseconds: delay)
        }
    }
}
```

### Pattern 2: Timeout

```swift
func withTimeout<T>(
    seconds: TimeInterval,
    operation: @escaping () async throws -> T
) async throws -> T {
    try await withThrowingTaskGroup(of: T.self) { group in
        group.addTask {
            try await operation()
        }

        group.addTask {
            try await Task.sleep(nanoseconds: UInt64(seconds * 1_000_000_000))
            throw TimeoutError()
        }

        let result = try await group.next()!
        group.cancelAll()
        return result
    }
}
```

### Pattern 3: Debounce

```swift
actor Debouncer<T> {
    private var task: Task<T, Error>?

    func debounce(
        delay: Duration,
        operation: @escaping () async throws -> T
    ) async throws -> T {
        task?.cancel()

        let newTask = Task<T, Error> {
            try await Task.sleep(for: delay)
            return try await operation()
        }

        task = newTask
        return try await newTask.value
    }
}
```

---

This tutorial covers all major types of Swift's coroutine equivalents (async/await concurrency). Each section includes practical examples you can adapt to your needs.
