import { useState, useEffect } from 'react'

/**
 * SecurityReport Component
 * Demonstrates:
 * - Props destructuring
 * - useEffect hook for side effects
 * - Derived state (computing values from props)
 * - Conditional styling
 * - Date formatting
 */
function SecurityReport({ result }) {
  // State for expanded/collapsed view
  const [isExpanded, setIsExpanded] = useState(false)
  const [viewCount, setViewCount] = useState(0)

  // useEffect: runs after component renders
  // Empty dependency array [] means it runs only once after initial render
  useEffect(() => {
    console.log('SecurityReport mounted for:', result.fileName)

    // Cleanup function: runs before component unmounts
    return () => {
      console.log('SecurityReport unmounted for:', result.fileName)
    }
  }, [result.fileName])

  // useEffect with dependencies: runs when dependencies change
  useEffect(() => {
    if (isExpanded) {
      setViewCount(prev => prev + 1)
    }
  }, [isExpanded])

  // Derived values: computed from props/state
  const statusColor = result.status === 'safe' ? 'green' : 'orange'
  const threatLevel = result.threats === 0 ? 'No threats' :
                      result.threats < 3 ? 'Low risk' : 'High risk'

  // Format date
  const formattedDate = new Date(result.timestamp).toLocaleString()

  // Event handler
  const toggleExpanded = () => {
    setIsExpanded(!isExpanded)
  }

  return (
    <div className={`security-report ${result.status}`}>
      <div className="report-header" onClick={toggleExpanded}>
        <div className="report-title">
          <h3>📄 {result.fileName}</h3>
          <span className={`status-badge ${result.status}`}>
            {result.status.toUpperCase()}
          </span>
        </div>
        <button className="expand-btn">
          {isExpanded ? '▼' : '▶'}
        </button>
      </div>

      {/* Conditional rendering: Show details only when expanded */}
      {isExpanded && (
        <div className="report-details">
          <div className="detail-row">
            <span className="label">File Type:</span>
            <span className="value">{result.fileType}</span>
          </div>
          <div className="detail-row">
            <span className="label">File Size:</span>
            <span className="value">{(result.size / 1024).toFixed(2)} KB</span>
          </div>
          <div className="detail-row">
            <span className="label">Threats Found:</span>
            <span className="value" style={{ color: statusColor }}>
              {result.threats}
            </span>
          </div>
          <div className="detail-row">
            <span className="label">Threat Level:</span>
            <span className="value">{threatLevel}</span>
          </div>
          <div className="detail-row">
            <span className="label">Analyzed:</span>
            <span className="value">{formattedDate}</span>
          </div>
          <div className="detail-row">
            <span className="label">Times Viewed:</span>
            <span className="value">{viewCount}</span>
          </div>

          {/* Conditional content based on threats */}
          {result.threats > 0 && (
            <div className="threat-warning">
              ⚠️ This file may contain security vulnerabilities. Review carefully.
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default SecurityReport
