import { useState, useRef } from 'react'

/**
 * FileAnalyzer Component
 * Demonstrates:
 * - Props: receiving data from parent component
 * - useRef hook: accessing DOM elements
 * - Event handling: onClick, onChange
 * - Controlled vs uncontrolled components
 * - Conditional rendering based on props
 */
function FileAnalyzer({ onFileSelect, isAnalyzing, selectedFile }) {
  // useRef: creates a mutable reference that persists across re-renders
  // Unlike state, changing ref doesn't cause re-render
  const fileInputRef = useRef(null)

  // Local state for drag and drop
  const [isDragging, setIsDragging] = useState(false)

  // Event handler for file input
  const handleFileChange = (event) => {
    const file = event.target.files[0]
    if (file) {
      onFileSelect(file)
    }
  }

  // Event handler for button click
  const handleButtonClick = () => {
    // Using ref to programmatically click the hidden file input
    fileInputRef.current?.click()
  }

  // Drag and drop event handlers
  const handleDragOver = (e) => {
    e.preventDefault()
    setIsDragging(true)
  }

  const handleDragLeave = (e) => {
    e.preventDefault()
    setIsDragging(false)
  }

  const handleDrop = (e) => {
    e.preventDefault()
    setIsDragging(false)

    const file = e.dataTransfer.files[0]
    if (file) {
      onFileSelect(file)
    }
  }

  return (
    <div className="file-analyzer">
      <h2>📁 File Upload & Analysis</h2>

      {/* Conditional class name based on state */}
      <div
        className={`drop-zone ${isDragging ? 'dragging' : ''}`}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
      >
        {isAnalyzing ? (
          // Conditional rendering: Show different UI during analysis
          <div className="analyzing">
            <div className="spinner"></div>
            <p>Analyzing {selectedFile?.name}...</p>
          </div>
        ) : (
          <>
            <p className="drop-text">
              Drag and drop a file here, or click to select
            </p>
            <p className="file-types">
              Supported: APK, JAR, AAR files
            </p>
            <button onClick={handleButtonClick} className="select-btn">
              Select File
            </button>
          </>
        )}
      </div>

      {/* Hidden file input - controlled by ref */}
      <input
        ref={fileInputRef}
        type="file"
        accept=".apk,.jar,.aar"
        onChange={handleFileChange}
        style={{ display: 'none' }}
      />

      {/* Display selected file info */}
      {selectedFile && !isAnalyzing && (
        <div className="file-info">
          <h3>Selected File:</h3>
          <p><strong>Name:</strong> {selectedFile.name}</p>
          <p><strong>Size:</strong> {(selectedFile.size / 1024).toFixed(2)} KB</p>
        </div>
      )}
    </div>
  )
}

export default FileAnalyzer
