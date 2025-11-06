import { useState } from 'react'
import FileAnalyzer from './components/FileAnalyzer'
import SecurityReport from './components/SecurityReport'
import Counter from './components/Counter'
import UserProfile from './components/UserProfile'
import './App.css'

/**
 * Main App Component
 * Demonstrates:
 * - Component composition (using multiple child components)
 * - State management with useState hook
 * - Props passing to child components
 * - Conditional rendering
 */
function App() {
  // State: data that changes over time and causes re-renders
  const [analysisResults, setAnalysisResults] = useState([])
  const [isAnalyzing, setIsAnalyzing] = useState(false)
  const [selectedFile, setSelectedFile] = useState(null)

  // Event handler: function that responds to user actions
  const handleFileAnalysis = (file) => {
    setIsAnalyzing(true)
    setSelectedFile(file)

    // Simulate async analysis
    setTimeout(() => {
      const newResult = {
        id: Date.now(),
        fileName: file.name,
        fileType: file.type || 'unknown',
        size: file.size,
        timestamp: new Date().toISOString(),
        threats: Math.floor(Math.random() * 5),
        status: Math.random() > 0.5 ? 'safe' : 'warning'
      }

      setAnalysisResults(prev => [...prev, newResult])
      setIsAnalyzing(false)
    }, 2000)
  }

  const clearResults = () => {
    setAnalysisResults([])
    setSelectedFile(null)
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>🔒 Security Tool - Web Interface</h1>
        <p>Analyze APK, JAR, and AAR files for security vulnerabilities</p>
      </header>

      <main className="app-main">
        {/* Component composition: Using custom components */}
        <section className="analyzer-section">
          <FileAnalyzer
            onFileSelect={handleFileAnalysis}
            isAnalyzing={isAnalyzing}
            selectedFile={selectedFile}
          />
        </section>

        {/* Conditional rendering: Show content based on state */}
        {analysisResults.length > 0 && (
          <section className="results-section">
            <div className="results-header">
              <h2>Analysis Results ({analysisResults.length})</h2>
              <button onClick={clearResults} className="clear-btn">
                Clear All
              </button>
            </div>

            {/* List rendering: Map over array to render multiple components */}
            {analysisResults.map(result => (
              <SecurityReport
                key={result.id}
                result={result}
              />
            ))}
          </section>
        )}

        {/* Demo components showing different React concepts */}
        <section className="demo-section">
          <h2>React Concepts Demo</h2>
          <div className="demo-grid">
            <Counter />
            <UserProfile name="Security Analyst" role="Admin" />
          </div>
        </section>
      </main>

      <footer className="app-footer">
        <p>React Web Application for Security Analysis</p>
      </footer>
    </div>
  )
}

export default App
