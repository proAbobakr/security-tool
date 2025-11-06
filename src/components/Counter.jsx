import { useState, useEffect } from 'react'

/**
 * Counter Component
 * Demonstrates:
 * - useState hook: managing component state
 * - Multiple state variables
 * - State updates and re-rendering
 * - useEffect for side effects
 * - Event handlers
 */
function Counter() {
  // useState: returns [currentValue, setterFunction]
  // Each setState call causes a re-render
  const [count, setCount] = useState(0)
  const [step, setStep] = useState(1)
  const [isAutoIncrementing, setIsAutoIncrementing] = useState(false)

  // useEffect with cleanup for auto-increment
  useEffect(() => {
    let intervalId

    if (isAutoIncrementing) {
      intervalId = setInterval(() => {
        // Functional update: use previous state
        setCount(prevCount => prevCount + step)
      }, 1000)
    }

    // Cleanup: clear interval when effect re-runs or component unmounts
    return () => {
      if (intervalId) {
        clearInterval(intervalId)
      }
    }
  }, [isAutoIncrementing, step]) // Re-run when these dependencies change

  // Event handlers
  const increment = () => {
    setCount(count + step)
  }

  const decrement = () => {
    setCount(count - step)
  }

  const reset = () => {
    setCount(0)
  }

  const handleStepChange = (e) => {
    const value = parseInt(e.target.value) || 1
    setStep(value)
  }

  const toggleAutoIncrement = () => {
    setIsAutoIncrementing(!isAutoIncrementing)
  }

  return (
    <div className="counter-widget">
      <h3>🔢 Counter Demo</h3>

      <div className="counter-display">
        <span className="count-value">{count}</span>
      </div>

      <div className="counter-controls">
        <button onClick={decrement} disabled={isAutoIncrementing}>
          - Decrease
        </button>
        <button onClick={increment} disabled={isAutoIncrementing}>
          + Increase
        </button>
        <button onClick={reset}>
          Reset
        </button>
      </div>

      <div className="step-control">
        <label>
          Step:
          <input
            type="number"
            value={step}
            onChange={handleStepChange}
            min="1"
            max="10"
            disabled={isAutoIncrementing}
          />
        </label>
      </div>

      <div className="auto-control">
        <button
          onClick={toggleAutoIncrement}
          className={isAutoIncrementing ? 'active' : ''}
        >
          {isAutoIncrementing ? '⏸ Stop Auto' : '▶ Start Auto'}
        </button>
      </div>

      <div className="counter-info">
        <small>Count: {count} | Step: {step} | Auto: {isAutoIncrementing ? 'ON' : 'OFF'}</small>
      </div>
    </div>
  )
}

export default Counter
