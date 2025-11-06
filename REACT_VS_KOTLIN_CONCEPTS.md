# React vs Kotlin: Comprehensive Concept Comparison

## Table of Contents
1. [Introduction](#introduction)
2. [Fundamental Paradigms](#fundamental-paradigms)
3. [State Management](#state-management)
4. [Component/Class Structure](#componentclass-structure)
5. [Lifecycle Management](#lifecycle-management)
6. [Event Handling](#event-handling)
7. [Data Flow & Props](#data-flow--props)
8. [Side Effects](#side-effects)
9. [Conditional Rendering/Logic](#conditional-renderinglogic)
10. [List Rendering/Iteration](#list-renderingiteration)
11. [Type Systems](#type-systems)
12. [Code Examples](#code-examples)

---

## Introduction

This document provides a detailed comparison between **React** (a JavaScript library for building user interfaces) and **Kotlin** (a statically-typed programming language used for Android development, backend, and more). While they serve different purposes, understanding their conceptual differences helps developers transition between web and mobile/backend development.

### Quick Overview

| Aspect | React | Kotlin |
|--------|-------|--------|
| **Type** | JavaScript library | Programming language |
| **Paradigm** | Functional & Declarative | Object-Oriented & Functional |
| **Primary Use** | Web UIs | Android, Backend, Multiplatform |
| **Typing** | Dynamic (with TypeScript option) | Static |
| **Execution** | Browser/Node.js | JVM/Native/JS |

---

## Fundamental Paradigms

### React: Declarative & Component-Based

React follows a **declarative** approach where you describe *what* the UI should look like based on the current state, and React handles *how* to update the DOM.

**Key Concepts:**
- **Components**: Reusable UI pieces
- **Virtual DOM**: Efficient UI updates
- **Unidirectional data flow**: Data flows from parent to child
- **Functional programming**: Emphasis on pure functions and immutability

```javascript
// React Component - Declarative
function UserCard({ name, age }) {
  return (
    <div className="user-card">
      <h2>{name}</h2>
      <p>Age: {age}</p>
    </div>
  )
}
```

### Kotlin: Object-Oriented & Imperative

Kotlin is a **multi-paradigm** language that supports both object-oriented and functional programming. It's primarily imperative but embraces functional concepts.

**Key Concepts:**
- **Classes & Objects**: Blueprint and instances
- **Inheritance & Interfaces**: Code reuse and contracts
- **Null safety**: Built-in null handling
- **Extension functions**: Add functionality to existing classes

```kotlin
// Kotlin Class - Object-Oriented
data class User(val name: String, val age: Int)

class UserCard(private val user: User) {
    fun render(): String {
        return """
            <div class="user-card">
                <h2>${user.name}</h2>
                <p>Age: ${user.age}</p>
            </div>
        """.trimIndent()
    }
}
```

---

## State Management

State is data that changes over time and affects what is displayed.

### React: useState Hook

In React, state is managed using **hooks** (in functional components) or class state (in class components). State updates trigger re-renders.

```javascript
import { useState } from 'react'

function Counter() {
  // useState returns [currentValue, setterFunction]
  const [count, setCount] = useState(0)

  // State updates trigger re-renders
  const increment = () => {
    setCount(count + 1)
  }

  // Can also use functional updates
  const incrementFunctional = () => {
    setCount(prevCount => prevCount + 1)
  }

  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={increment}>Increment</button>
    </div>
  )
}
```

**Key Points:**
- State is immutable - you create new state, not modify existing
- State updates are asynchronous
- State updates trigger component re-renders
- Each component has its own state

### Kotlin: Mutable Properties

In Kotlin, state is typically managed through **mutable properties** or specialized state management libraries (like StateFlow in Kotlin Coroutines).

```kotlin
class Counter {
    // Mutable property
    var count: Int = 0
        private set // Prevent external modification

    // Public method to modify state
    fun increment() {
        count++
    }

    // For reactive programming, use StateFlow
    private val _countFlow = MutableStateFlow(0)
    val countFlow: StateFlow<Int> = _countFlow.asStateFlow()

    fun incrementFlow() {
        _countFlow.value++
    }
}

// Usage
fun main() {
    val counter = Counter()
    println("Count: ${counter.count}") // 0
    counter.increment()
    println("Count: ${counter.count}") // 1
}
```

**Key Points:**
- State can be mutable or immutable
- Direct property modification (not always reactive)
- StateFlow/LiveData for reactive state management
- No automatic UI updates (requires manual or framework handling)

---

## Component/Class Structure

### React: Function Components

Modern React uses **functional components** with hooks for state and side effects.

```javascript
// Functional Component
function SecurityReport({ result }) {
  const [isExpanded, setIsExpanded] = useState(false)

  const toggleExpanded = () => {
    setIsExpanded(!isExpanded)
  }

  return (
    <div className="security-report">
      <h3 onClick={toggleExpanded}>{result.fileName}</h3>
      {isExpanded && (
        <div className="details">
          <p>Type: {result.fileType}</p>
          <p>Threats: {result.threats}</p>
        </div>
      )}
    </div>
  )
}
```

**Component Characteristics:**
- Returns JSX (JavaScript XML)
- Uses hooks for state and effects
- Props passed as function parameters
- Pure functions (ideally) - same props = same output

### Kotlin: Classes and Objects

```kotlin
data class SecurityResult(
    val fileName: String,
    val fileType: String,
    val threats: Int
)

class SecurityReportView(private val result: SecurityResult) {
    private var isExpanded: Boolean = false

    fun toggleExpanded() {
        isExpanded = !isExpanded
    }

    fun render(): String {
        val details = if (isExpanded) {
            """
            <div class="details">
                <p>Type: ${result.fileType}</p>
                <p>Threats: ${result.threats}</p>
            </div>
            """.trimIndent()
        } else ""

        return """
            <div class="security-report">
                <h3>${result.fileName}</h3>
                $details
            </div>
        """.trimIndent()
    }
}
```

**Class Characteristics:**
- Explicit class declaration
- Properties and methods
- Manual rendering/update logic
- Can have constructors, inheritance, interfaces

---

## Lifecycle Management

### React: useEffect Hook

React components have a lifecycle: mount, update, unmount. The `useEffect` hook handles side effects at different lifecycle stages.

```javascript
import { useState, useEffect } from 'react'

function DataFetcher({ userId }) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)

  // Runs after render - like componentDidMount + componentDidUpdate
  useEffect(() => {
    console.log('Effect running')
    setLoading(true)

    // Async operation
    fetchUserData(userId)
      .then(result => {
        setData(result)
        setLoading(false)
      })

    // Cleanup function - runs before next effect and on unmount
    return () => {
      console.log('Cleanup running')
      cancelPendingRequests()
    }
  }, [userId]) // Dependency array - re-run when userId changes

  if (loading) return <div>Loading...</div>
  return <div>{data?.name}</div>
}
```

**useEffect Patterns:**

```javascript
// Run once on mount
useEffect(() => {
  console.log('Component mounted')
}, []) // Empty dependency array

// Run on every render
useEffect(() => {
  console.log('Component rendered')
}) // No dependency array

// Run when specific values change
useEffect(() => {
  console.log('userId changed:', userId)
}, [userId]) // Run when userId changes

// Cleanup on unmount
useEffect(() => {
  const timer = setInterval(() => console.log('tick'), 1000)
  return () => clearInterval(timer) // Cleanup
}, [])
```

### Kotlin: Manual Lifecycle Management

Kotlin doesn't have automatic lifecycle management like React. In Android, you use Activity/Fragment lifecycle methods:

```kotlin
class DataFetcherActivity : AppCompatActivity() {
    private var data: UserData? = null
    private var job: Job? = null

    // onCreate - like componentDidMount
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("Activity created")
        fetchUserData()
    }

    // onStart - component becoming visible
    override fun onStart() {
        super.onStart()
        println("Activity started")
    }

    // onResume - component active
    override fun onResume() {
        super.onResume()
        println("Activity resumed")
    }

    // onPause - component losing focus
    override fun onPause() {
        super.onPause()
        println("Activity paused")
    }

    // onDestroy - like componentWillUnmount
    override fun onDestroy() {
        super.onDestroy()
        job?.cancel() // Cleanup
        println("Activity destroyed")
    }

    private fun fetchUserData() {
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = apiService.getUserData()
                withContext(Dispatchers.Main) {
                    data = result
                    updateUI()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
}
```

**Lifecycle Comparison:**

| React | Android (Kotlin) |
|-------|------------------|
| Component mounts | onCreate() |
| Component updates | onResume() |
| Component unmounts | onDestroy() |
| useEffect cleanup | onPause() / onDestroy() |

---

## Event Handling

### React: Synthetic Events

React uses **synthetic events** - a cross-browser wrapper around native events.

```javascript
function EventDemo() {
  const [value, setValue] = useState('')

  // Click event
  const handleClick = (event) => {
    console.log('Button clicked')
    console.log('Event type:', event.type)
  }

  // Input change event
  const handleChange = (event) => {
    setValue(event.target.value)
  }

  // Form submit event
  const handleSubmit = (event) => {
    event.preventDefault() // Prevent default form submission
    console.log('Form submitted with:', value)
  }

  // Event with parameters
  const handleDelete = (id) => {
    console.log('Delete item:', id)
  }

  return (
    <div>
      <button onClick={handleClick}>Click Me</button>

      <input
        type="text"
        value={value}
        onChange={handleChange}
      />

      <form onSubmit={handleSubmit}>
        <button type="submit">Submit</button>
      </form>

      <button onClick={() => handleDelete(123)}>
        Delete Item
      </button>
    </div>
  )
}
```

**Event Features:**
- CamelCase naming (onClick, onChange)
- Pass function reference, not string
- Automatic event object passed
- Synthetic events pooled for performance

### Kotlin: Event Listeners

```kotlin
// Android View example
class EventDemoActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        button = findViewById(R.id.button)
        editText = findViewById(R.id.editText)

        // Click listener - verbose syntax
        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                println("Button clicked")
            }
        })

        // Click listener - lambda syntax (concise)
        button.setOnClickListener { view ->
            println("Button clicked")
        }

        // Even more concise
        button.setOnClickListener {
            println("Button clicked")
        }

        // Text change listener
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                println("Text changed: $s")
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Custom event with parameter
        fun handleDelete(id: Int) {
            println("Delete item: $id")
        }

        button.setOnClickListener {
            handleDelete(123)
        }
    }
}
```

---

## Data Flow & Props

### React: Props (Properties)

Props are how data flows from parent to child components. They are **read-only** in the child component.

```javascript
// Parent Component
function App() {
  const user = {
    name: 'John Doe',
    age: 30,
    email: 'john@example.com'
  }

  const handleUpdate = (newName) => {
    console.log('Update to:', newName)
  }

  return (
    <div>
      {/* Passing props to child */}
      <UserProfile
        user={user}
        isAdmin={true}
        count={42}
        onUpdate={handleUpdate}
      />
    </div>
  )
}

// Child Component - receiving props
function UserProfile({ user, isAdmin, count, onUpdate }) {
  // Props are read-only - cannot do: user = {...}

  const handleClick = () => {
    // Call parent's function via props
    onUpdate('Jane Doe')
  }

  return (
    <div>
      <h2>{user.name}</h2>
      <p>Age: {user.age}</p>
      <p>Email: {user.email}</p>
      {isAdmin && <span>Admin Badge</span>}
      <p>Count: {count}</p>
      <button onClick={handleClick}>Update Name</button>
    </div>
  )
}

// Props destructuring with defaults
function Button({ label = 'Click', color = 'blue', onClick }) {
  return (
    <button style={{ color }} onClick={onClick}>
      {label}
    </button>
  )
}

// Children prop - special prop for nested content
function Card({ title, children }) {
  return (
    <div className="card">
      <h3>{title}</h3>
      <div className="card-body">
        {children}
      </div>
    </div>
  )
}

// Usage
<Card title="User Info">
  <p>This is nested content</p>
  <UserProfile user={user} />
</Card>
```

### Kotlin: Constructor Parameters & Properties

```kotlin
// Data class - similar to props
data class User(
    val name: String,
    val age: Int,
    val email: String
)

// Class receiving "props" via constructor
class UserProfileView(
    private val user: User,
    private val isAdmin: Boolean = false, // Default value
    private val count: Int = 0,
    private val onUpdate: (String) -> Unit // Function parameter
) {
    fun render(): String {
        val adminBadge = if (isAdmin) "<span>Admin Badge</span>" else ""

        return """
            <div>
                <h2>${user.name}</h2>
                <p>Age: ${user.age}</p>
                <p>Email: ${user.email}</p>
                $adminBadge
                <p>Count: $count</p>
                <button onclick="handleClick()">Update Name</button>
            </div>
        """.trimIndent()
    }

    fun handleClick() {
        onUpdate("Jane Doe")
    }
}

// Usage
fun main() {
    val user = User("John Doe", 30, "john@example.com")

    val profile = UserProfileView(
        user = user,
        isAdmin = true,
        count = 42,
        onUpdate = { newName ->
            println("Update to: $newName")
        }
    )

    println(profile.render())
}

// Higher-order function (similar to children prop)
class Card(
    private val title: String,
    private val content: () -> String
) {
    fun render(): String {
        return """
            <div class="card">
                <h3>$title</h3>
                <div class="card-body">
                    ${content()}
                </div>
            </div>
        """.trimIndent()
    }
}
```

---

## Side Effects

### React: useEffect for Side Effects

Side effects are operations that affect things outside the component (API calls, subscriptions, timers, etc.)

```javascript
import { useState, useEffect } from 'react'

function UserDashboard() {
  const [user, setUser] = useState(null)
  const [notifications, setNotifications] = useState([])

  // API call on mount
  useEffect(() => {
    async function fetchUser() {
      const response = await fetch('/api/user')
      const data = await response.json()
      setUser(data)
    }

    fetchUser()
  }, [])

  // WebSocket subscription with cleanup
  useEffect(() => {
    const ws = new WebSocket('ws://localhost:8080')

    ws.onmessage = (event) => {
      const notification = JSON.parse(event.data)
      setNotifications(prev => [...prev, notification])
    }

    // Cleanup: close WebSocket on unmount
    return () => {
      ws.close()
    }
  }, [])

  // Timer with cleanup
  useEffect(() => {
    const intervalId = setInterval(() => {
      console.log('Periodic update')
    }, 5000)

    return () => clearInterval(intervalId)
  }, [])

  // Document title update
  useEffect(() => {
    document.title = user ? `${user.name}'s Dashboard` : 'Dashboard'
  }, [user])

  return <div>Dashboard content...</div>
}
```

### Kotlin: Manual Side Effect Management

```kotlin
class UserDashboard : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        Dispatchers.Main + Job()

    private var user: User? = null
    private val notifications = mutableListOf<Notification>()
    private var webSocket: WebSocket? = null
    private var timer: Timer? = null

    // Called when view is created
    fun onCreate() {
        fetchUser()
        setupWebSocket()
        startPeriodicUpdate()
    }

    // API call
    private fun fetchUser() {
        launch {
            try {
                val response = apiService.getUser()
                user = response
                updateTitle()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    // WebSocket subscription
    private fun setupWebSocket() {
        webSocket = WebSocket("ws://localhost:8080").apply {
            onMessage = { message ->
                val notification = Json.decodeFromString<Notification>(message)
                notifications.add(notification)
            }
        }
    }

    // Timer
    private fun startPeriodicUpdate() {
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    println("Periodic update")
                }
            }, 0, 5000)
        }
    }

    // Update document title
    private fun updateTitle() {
        val title = user?.let { "${it.name}'s Dashboard" } ?: "Dashboard"
        // Update title in UI framework
    }

    // Cleanup - called when view is destroyed
    fun onDestroy() {
        webSocket?.close()
        timer?.cancel()
        coroutineContext.cancel()
    }
}
```

---

## Conditional Rendering/Logic

### React: JSX Conditional Rendering

React uses JavaScript expressions for conditional rendering.

```javascript
function StatusDisplay({ isLoggedIn, user, loading, error }) {
  // 1. If-else using ternary operator
  return (
    <div>
      {isLoggedIn ? (
        <p>Welcome, {user.name}!</p>
      ) : (
        <p>Please log in</p>
      )}
    </div>
  )

  // 2. Logical && operator (short-circuit evaluation)
  return (
    <div>
      {error && <div className="error">{error}</div>}
      {loading && <div className="spinner">Loading...</div>}
      {user && <UserProfile user={user} />}
    </div>
  )

  // 3. Early return
  if (loading) {
    return <div>Loading...</div>
  }

  if (error) {
    return <div>Error: {error}</div>
  }

  if (!user) {
    return <div>No user data</div>
  }

  return <UserProfile user={user} />

  // 4. Nullish coalescing
  return (
    <div>
      <p>{user?.name ?? 'Guest'}</p>
    </div>
  )

  // 5. Switch-case pattern (using object)
  const statusMessages = {
    loading: 'Loading...',
    success: 'Success!',
    error: 'Error occurred',
    idle: 'Ready'
  }

  return <div>{statusMessages[status]}</div>
}
```

### Kotlin: Traditional Conditionals

```kotlin
class StatusDisplay(
    private val isLoggedIn: Boolean,
    private val user: User?,
    private val loading: Boolean,
    private val error: String?
) {
    fun render(): String {
        // 1. If-else expression
        val loginMessage = if (isLoggedIn) {
            "Welcome, ${user?.name}!"
        } else {
            "Please log in"
        }

        // 2. Multiple conditions
        val statusView = buildString {
            error?.let { append("<div class='error'>$it</div>") }
            if (loading) append("<div class='spinner'>Loading...</div>")
            user?.let { append(renderUserProfile(it)) }
        }

        // 3. When expression (like switch)
        val message = when {
            loading -> "Loading..."
            error != null -> "Error: $error"
            user == null -> "No user data"
            else -> renderUserProfile(user)
        }

        // 4. Elvis operator (like nullish coalescing)
        val userName = user?.name ?: "Guest"

        // 5. When with enum/sealed class
        enum class Status { LOADING, SUCCESS, ERROR, IDLE }

        val statusMessage = when (status) {
            Status.LOADING -> "Loading..."
            Status.SUCCESS -> "Success!"
            Status.ERROR -> "Error occurred"
            Status.IDLE -> "Ready"
        }

        return "<div>$loginMessage</div>"
    }

    private fun renderUserProfile(user: User): String {
        return "<div>${user.name}</div>"
    }
}
```

---

## List Rendering/Iteration

### React: Mapping Arrays to JSX

```javascript
function FileList({ files }) {
  // 1. Basic map
  return (
    <ul>
      {files.map(file => (
        <li key={file.id}>{file.name}</li>
      ))}
    </ul>
  )

  // 2. Map with index
  return (
    <ul>
      {files.map((file, index) => (
        <li key={file.id}>
          {index + 1}. {file.name}
        </li>
      ))}
    </ul>
  )

  // 3. Map with component
  return (
    <div>
      {files.map(file => (
        <FileCard key={file.id} file={file} />
      ))}
    </div>
  )

  // 4. Filter then map
  return (
    <ul>
      {files
        .filter(file => file.size > 1000)
        .map(file => (
          <li key={file.id}>{file.name}</li>
        ))}
    </ul>
  )

  // 5. Empty state
  return (
    <div>
      {files.length === 0 ? (
        <p>No files available</p>
      ) : (
        <ul>
          {files.map(file => (
            <li key={file.id}>{file.name}</li>
          ))}
        </ul>
      )}
    </div>
  )

  // 6. Nested lists
  return (
    <div>
      {folders.map(folder => (
        <div key={folder.id}>
          <h3>{folder.name}</h3>
          <ul>
            {folder.files.map(file => (
              <li key={file.id}>{file.name}</li>
            ))}
          </ul>
        </div>
      ))}
    </div>
  )
}
```

**Key Points:**
- Always provide unique `key` prop for list items
- Keys help React identify which items changed
- Use stable identifiers (IDs), not array indices
- Map returns new array of JSX elements

### Kotlin: Traditional Loops & Collection Operations

```kotlin
data class File(val id: Int, val name: String, val size: Long)
data class Folder(val id: Int, val name: String, val files: List<File>)

class FileListView(private val files: List<File>) {
    // 1. For loop
    fun renderWithLoop(): String {
        val html = StringBuilder()
        html.append("<ul>")
        for (file in files) {
            html.append("<li>${file.name}</li>")
        }
        html.append("</ul>")
        return html.toString()
    }

    // 2. forEach
    fun renderWithForEach(): String = buildString {
        append("<ul>")
        files.forEach { file ->
            append("<li>${file.name}</li>")
        }
        append("</ul>")
    }

    // 3. map and joinToString
    fun renderWithMap(): String {
        val items = files.map { file ->
            "<li>${file.name}</li>"
        }.joinToString("")

        return "<ul>$items</ul>"
    }

    // 4. forEachIndexed
    fun renderWithIndex(): String = buildString {
        append("<ul>")
        files.forEachIndexed { index, file ->
            append("<li>${index + 1}. ${file.name}</li>")
        }
        append("</ul>")
    }

    // 5. Filter then map
    fun renderLargeFiles(): String {
        val items = files
            .filter { it.size > 1000 }
            .map { "<li>${it.name}</li>" }
            .joinToString("")

        return "<ul>$items</ul>"
    }

    // 6. Empty state handling
    fun renderWithEmptyState(): String {
        return if (files.isEmpty()) {
            "<p>No files available</p>"
        } else {
            val items = files.map { "<li>${it.name}</li>" }.joinToString("")
            "<ul>$items</ul>"
        }
    }

    // 7. Nested lists
    fun renderFolders(folders: List<Folder>): String = buildString {
        folders.forEach { folder ->
            append("<div>")
            append("<h3>${folder.name}</h3>")
            append("<ul>")
            folder.files.forEach { file ->
                append("<li>${file.name}</li>")
            }
            append("</ul>")
            append("</div>")
        }
    }
}
```

---

## Type Systems

### React: Dynamic Typing (JavaScript) or Static (TypeScript)

JavaScript is **dynamically typed**, but you can use TypeScript for static typing.

```javascript
// JavaScript - no type checking
function UserCard(props) {
  return <div>{props.name}</div>
}

// Can pass anything - no compile-time checks
<UserCard name="John" />
<UserCard name={123} /> // Works but might cause issues
<UserCard /> // props.name is undefined
```

**With TypeScript:**

```typescript
// TypeScript - static type checking
interface User {
  id: number
  name: string
  email: string
  age?: number // Optional property
}

interface UserCardProps {
  user: User
  isAdmin: boolean
  onUpdate: (user: User) => void
}

function UserCard({ user, isAdmin, onUpdate }: UserCardProps) {
  return (
    <div>
      <h2>{user.name}</h2>
      <p>{user.email}</p>
      {isAdmin && <span>Admin</span>}
    </div>
  )
}

// Type checking at compile time
const user: User = {
  id: 1,
  name: "John",
  email: "john@example.com"
}

<UserCard user={user} isAdmin={true} onUpdate={handleUpdate} />
// <UserCard user="wrong" /> // Compile error!
```

### Kotlin: Static Typing

Kotlin is **statically typed** with type inference.

```kotlin
// Data class with types
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val age: Int? = null // Nullable type
)

class UserCard(
    private val user: User,
    private val isAdmin: Boolean,
    private val onUpdate: (User) -> Unit
) {
    fun render(): String {
        val adminBadge = if (isAdmin) "<span>Admin</span>" else ""
        return """
            <div>
                <h2>${user.name}</h2>
                <p>${user.email}</p>
                $adminBadge
            </div>
        """.trimIndent()
    }
}

// Type safety enforced at compile time
val user = User(
    id = 1,
    name = "John",
    email = "john@example.com"
)

val card = UserCard(
    user = user,
    isAdmin = true,
    onUpdate = { updatedUser -> println(updatedUser) }
)

// val card = UserCard(user = "wrong", ...) // Compile error!

// Null safety
fun printUserAge(user: User) {
    // Safe call operator
    println(user.age?.toString() ?: "Unknown")

    // Also works
    user.age?.let { age ->
        println("Age: $age")
    }
}
```

---

## Code Examples

### Complete Example: Todo App

#### React Implementation

```javascript
import { useState } from 'react'

// Todo type (if using TypeScript)
interface Todo {
  id: number
  text: string
  completed: boolean
}

function TodoApp() {
  const [todos, setTodos] = useState<Todo[]>([])
  const [inputValue, setInputValue] = useState('')

  // Add todo
  const addTodo = () => {
    if (inputValue.trim()) {
      const newTodo: Todo = {
        id: Date.now(),
        text: inputValue,
        completed: false
      }
      setTodos([...todos, newTodo])
      setInputValue('')
    }
  }

  // Toggle todo
  const toggleTodo = (id: number) => {
    setTodos(todos.map(todo =>
      todo.id === id
        ? { ...todo, completed: !todo.completed }
        : todo
    ))
  }

  // Delete todo
  const deleteTodo = (id: number) => {
    setTodos(todos.filter(todo => todo.id !== id))
  }

  // Filter todos
  const activeTodos = todos.filter(t => !t.completed)
  const completedTodos = todos.filter(t => t.completed)

  return (
    <div className="todo-app">
      <h1>📝 Todo List</h1>

      {/* Input section */}
      <div className="input-section">
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && addTodo()}
          placeholder="What needs to be done?"
        />
        <button onClick={addTodo}>Add</button>
      </div>

      {/* Stats */}
      <div className="stats">
        <span>Total: {todos.length}</span>
        <span>Active: {activeTodos.length}</span>
        <span>Completed: {completedTodos.length}</span>
      </div>

      {/* Todo list */}
      <ul className="todo-list">
        {todos.map(todo => (
          <li key={todo.id} className={todo.completed ? 'completed' : ''}>
            <input
              type="checkbox"
              checked={todo.completed}
              onChange={() => toggleTodo(todo.id)}
            />
            <span>{todo.text}</span>
            <button onClick={() => deleteTodo(todo.id)}>Delete</button>
          </li>
        ))}
      </ul>

      {/* Empty state */}
      {todos.length === 0 && (
        <p className="empty-state">No todos yet. Add one above!</p>
      )}
    </div>
  )
}

export default TodoApp
```

#### Kotlin Implementation

```kotlin
data class Todo(
    val id: Long,
    val text: String,
    val completed: Boolean = false
)

class TodoApp {
    private val todos = mutableListOf<Todo>()
    private var inputValue = ""

    // Add todo
    fun addTodo() {
        if (inputValue.trim().isNotEmpty()) {
            val newTodo = Todo(
                id = System.currentTimeMillis(),
                text = inputValue,
                completed = false
            )
            todos.add(newTodo)
            inputValue = ""
            render()
        }
    }

    // Toggle todo
    fun toggleTodo(id: Long) {
        val index = todos.indexOfFirst { it.id == id }
        if (index != -1) {
            todos[index] = todos[index].copy(completed = !todos[index].completed)
            render()
        }
    }

    // Delete todo
    fun deleteTodo(id: Long) {
        todos.removeIf { it.id == id }
        render()
    }

    // Filter todos
    fun getActiveTodos() = todos.filter { !it.completed }
    fun getCompletedTodos() = todos.filter { it.completed }

    // Handle input change
    fun onInputChange(value: String) {
        inputValue = value
    }

    // Render
    fun render(): String = buildString {
        append("""
            <div class="todo-app">
                <h1>📝 Todo List</h1>

                <!-- Input section -->
                <div class="input-section">
                    <input type="text" value="$inputValue" placeholder="What needs to be done?" />
                    <button onclick="addTodo()">Add</button>
                </div>

                <!-- Stats -->
                <div class="stats">
                    <span>Total: ${todos.size}</span>
                    <span>Active: ${getActiveTodos().size}</span>
                    <span>Completed: ${getCompletedTodos().size}</span>
                </div>

                <!-- Todo list -->
                <ul class="todo-list">
        """.trimIndent())

        todos.forEach { todo ->
            val completedClass = if (todo.completed) "class='completed'" else ""
            val checked = if (todo.completed) "checked" else ""
            append("""
                <li $completedClass>
                    <input type="checkbox" $checked onchange="toggleTodo(${todo.id})" />
                    <span>${todo.text}</span>
                    <button onclick="deleteTodo(${todo.id})">Delete</button>
                </li>
            """.trimIndent())
        }

        append("</ul>")

        // Empty state
        if (todos.isEmpty()) {
            append("<p class='empty-state'>No todos yet. Add one above!</p>")
        }

        append("</div>")
    }
}

// Usage
fun main() {
    val app = TodoApp()
    app.onInputChange("Buy groceries")
    app.addTodo()
    app.onInputChange("Write code")
    app.addTodo()
    println(app.render())
}
```

---

## Key Differences Summary

| Concept | React | Kotlin |
|---------|-------|--------|
| **UI Updates** | Automatic (reactive) | Manual refresh needed |
| **State Changes** | Triggers re-render | Requires explicit UI update |
| **Immutability** | Encouraged (setState) | Optional (mutable/immutable) |
| **Lifecycle** | useEffect with dependencies | Manual lifecycle methods |
| **Data Flow** | Unidirectional (props down) | Flexible (any direction) |
| **Event Handling** | Synthetic events, camelCase | Native events, listeners |
| **Type System** | Dynamic (or TypeScript) | Static with inference |
| **Null Handling** | Optional chaining (?.) | Built-in null safety (?.) |
| **Composition** | JSX components | Classes/functions |
| **Async Handling** | Promises/async-await | Coroutines/suspend functions |

---

## Conclusion

While React and Kotlin serve different domains, understanding their conceptual parallels helps developers:

1. **Transition between platforms** - Web to mobile or vice versa
2. **Apply design patterns** - Similar patterns across languages
3. **Understand paradigms** - Declarative vs imperative approaches
4. **Choose the right tool** - Based on project requirements

### When to Use React:
- Building web applications
- Need reactive, component-based UI
- Fast prototyping with rich ecosystem
- Cross-platform web apps

### When to Use Kotlin:
- Android mobile development
- Backend services (with Ktor, Spring)
- Multiplatform projects (Kotlin Multiplatform)
- Type-safe, null-safe requirements

Both technologies emphasize modern development practices: immutability, functional programming, and clean code architecture.
