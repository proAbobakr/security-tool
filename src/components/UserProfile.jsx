import { useState, useEffect } from 'react'

/**
 * UserProfile Component
 * Demonstrates:
 * - Props with default values
 * - Object state
 * - Complex state updates
 * - Conditional rendering
 * - Form handling
 */
function UserProfile({ name = 'Guest', role = 'User' }) {
  // Object state: managing complex data structures
  const [profile, setProfile] = useState({
    name: name,
    role: role,
    status: 'online',
    lastActive: new Date().toISOString(),
    preferences: {
      theme: 'light',
      notifications: true
    }
  })

  const [isEditing, setIsEditing] = useState(false)
  const [editForm, setEditForm] = useState({ name: '', role: '' })

  // Update profile when props change
  useEffect(() => {
    setProfile(prev => ({
      ...prev,
      name: name,
      role: role
    }))
  }, [name, role])

  // Update last active time every minute
  useEffect(() => {
    const intervalId = setInterval(() => {
      setProfile(prev => ({
        ...prev,
        lastActive: new Date().toISOString()
      }))
    }, 60000) // 60 seconds

    return () => clearInterval(intervalId)
  }, [])

  // Toggle status
  const toggleStatus = () => {
    setProfile(prev => ({
      ...prev,
      status: prev.status === 'online' ? 'offline' : 'online'
    }))
  }

  // Toggle theme
  const toggleTheme = () => {
    setProfile(prev => ({
      ...prev,
      preferences: {
        ...prev.preferences,
        theme: prev.preferences.theme === 'light' ? 'dark' : 'light'
      }
    }))
  }

  // Toggle notifications
  const toggleNotifications = () => {
    setProfile(prev => ({
      ...prev,
      preferences: {
        ...prev.preferences,
        notifications: !prev.preferences.notifications
      }
    }))
  }

  // Handle edit mode
  const startEditing = () => {
    setEditForm({ name: profile.name, role: profile.role })
    setIsEditing(true)
  }

  const cancelEditing = () => {
    setIsEditing(false)
  }

  const saveProfile = () => {
    setProfile(prev => ({
      ...prev,
      name: editForm.name,
      role: editForm.role
    }))
    setIsEditing(false)
  }

  // Handle form input changes
  const handleInputChange = (e) => {
    const { name, value } = e.target
    setEditForm(prev => ({
      ...prev,
      [name]: value
    }))
  }

  return (
    <div className="user-profile-widget">
      <h3>👤 User Profile</h3>

      {isEditing ? (
        // Edit mode
        <div className="profile-edit">
          <div className="form-group">
            <label>Name:</label>
            <input
              type="text"
              name="name"
              value={editForm.name}
              onChange={handleInputChange}
            />
          </div>
          <div className="form-group">
            <label>Role:</label>
            <input
              type="text"
              name="role"
              value={editForm.role}
              onChange={handleInputChange}
            />
          </div>
          <div className="edit-actions">
            <button onClick={saveProfile} className="save-btn">Save</button>
            <button onClick={cancelEditing} className="cancel-btn">Cancel</button>
          </div>
        </div>
      ) : (
        // View mode
        <div className="profile-view">
          <div className="profile-info">
            <div className="info-row">
              <span className="label">Name:</span>
              <span className="value">{profile.name}</span>
            </div>
            <div className="info-row">
              <span className="label">Role:</span>
              <span className="value">{profile.role}</span>
            </div>
            <div className="info-row">
              <span className="label">Status:</span>
              <span className={`status-indicator ${profile.status}`}>
                {profile.status}
              </span>
            </div>
          </div>

          <div className="profile-preferences">
            <h4>Preferences</h4>
            <div className="pref-row">
              <span>Theme:</span>
              <button onClick={toggleTheme} className="toggle-btn">
                {profile.preferences.theme === 'light' ? '☀️ Light' : '🌙 Dark'}
              </button>
            </div>
            <div className="pref-row">
              <span>Notifications:</span>
              <button onClick={toggleNotifications} className="toggle-btn">
                {profile.preferences.notifications ? '🔔 On' : '🔕 Off'}
              </button>
            </div>
          </div>

          <div className="profile-actions">
            <button onClick={toggleStatus} className="status-btn">
              Toggle Status
            </button>
            <button onClick={startEditing} className="edit-btn">
              Edit Profile
            </button>
          </div>

          <div className="last-active">
            <small>Last active: {new Date(profile.lastActive).toLocaleTimeString()}</small>
          </div>
        </div>
      )}
    </div>
  )
}

export default UserProfile
