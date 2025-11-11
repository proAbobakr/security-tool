# App Store Publishing Guide: Android vs iOS

This guide provides a comprehensive comparison of publishing processes for Google Play Store (Android) and Apple App Store (iOS).

## Table of Contents
- [Overview](#overview)
- [Account Requirements](#account-requirements)
- [Development Prerequisites](#development-prerequisites)
- [App Preparation](#app-preparation)
- [Store Listing Requirements](#store-listing-requirements)
- [Review Process](#review-process)
- [Publishing Steps](#publishing-steps)
- [Post-Publication](#post-publication)
- [Key Differences Summary](#key-differences-summary)

---

## Overview

Publishing apps to Google Play Store and Apple App Store involves different processes, requirements, and timelines. Understanding these differences is crucial for a successful launch.

---

## Account Requirements

### Android (Google Play Store)

**Developer Account Setup:**
- **Cost:** $25 one-time registration fee
- **Payment:** Credit/debit card
- **Verification:** Google account required
- **Processing Time:** Typically 24-48 hours
- **Organization Account:** Requires D-U-N-S number for organization accounts

**Requirements:**
- Valid Google account
- Accepted payment method
- Developer distribution agreement acceptance
- US export laws compliance (if applicable)

### iOS (Apple App Store)

**Developer Account Setup:**
- **Cost:** $99/year subscription (renews annually)
- **Payment:** Credit card required
- **Verification:** Two-factor authentication mandatory
- **Processing Time:** 24-48 hours (can take longer for organization accounts)
- **Organization Account:** Requires D-U-N-S number and legal entity verification

**Requirements:**
- Apple ID with two-factor authentication
- Valid payment method
- Apple Developer Program enrollment
- Legal entity documentation (for organization accounts)
- Physical address verification

**Key Difference:** Android has a one-time fee, while iOS requires annual renewal.

---

## Development Prerequisites

### Android

**Technical Requirements:**
- Android Studio or compatible IDE
- Java/Kotlin development environment
- Android SDK
- Gradle build system
- Keystore file for app signing

**App Signing:**
- Create keystore file using `keytool`
- Sign APK/AAB with your private key
- Google manages signing keys with Play App Signing (recommended)
- Keep keystore backup (losing it means you cannot update your app)

**Build Format:**
- APK (Android Package) - legacy format
- **AAB (Android App Bundle)** - required for new apps since August 2021

### iOS

**Technical Requirements:**
- Xcode (macOS required)
- Swift or Objective-C
- Mac computer (physical or cloud-based)
- Provisioning profiles
- Code signing certificates

**App Signing:**
- iOS Distribution Certificate from Apple
- Provisioning profiles (Development, Ad Hoc, App Store)
- Automatic signing via Xcode (recommended)
- Manual signing option available

**Build Format:**
- IPA (iOS App Store Package)
- Built through Xcode or Xcode Cloud

**Key Difference:** iOS development requires macOS and Xcode; Android can be developed on any platform.

---

## App Preparation

### Android

**Version Management:**
```gradle
android {
    defaultConfig {
        versionCode 1          // Integer, incremental
        versionName "1.0.0"    // String, user-visible
    }
}
```

**Required Files:**
- Signed APK/AAB
- App icon (various densities: mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
- Feature graphic (1024 x 500 px)
- Screenshots (minimum 2, up to 8 per device type)
- Privacy policy URL (if app handles sensitive data)

**Permissions:**
- Declare all permissions in AndroidManifest.xml
- Justify sensitive permissions in Play Console
- Request runtime permissions for dangerous permissions

**App Bundle:**
```bash
# Build signed bundle
./gradlew bundleRelease

# Generate AAB
# Located at: app/build/outputs/bundle/release/app-release.aab
```

### iOS

**Version Management:**
```swift
// In Info.plist or project settings
CFBundleShortVersionString: "1.0.0"  // User-visible version
CFBundleVersion: "1"                  // Build number
```

**Required Files:**
- Signed IPA file
- App icons (multiple sizes for different devices)
- Screenshots (required for all supported device sizes)
- Privacy policy URL (mandatory for all apps)
- App preview videos (optional)

**Permissions:**
- Declare usage descriptions in Info.plist
- All permission requests must have clear justifications
- Examples:
  ```xml
  <key>NSCameraUsageDescription</key>
  <string>Camera access is needed to scan documents</string>
  ```

**Archive and Export:**
```bash
# Via Xcode
# Product > Archive > Distribute App > App Store Connect
```

**Key Difference:** iOS has stricter requirements for permission justifications and requires device-specific screenshots.

---

## Store Listing Requirements

### Android (Google Play Console)

**App Details:**
- App name (max 50 characters)
- Short description (max 80 characters)
- Full description (max 4000 characters)
- Categorization (app type and category)
- Content rating questionnaire
- Target age group

**Graphics:**
- App icon: 512 x 512 px (PNG, 32-bit)
- Feature graphic: 1024 x 500 px (JPG or PNG)
- Screenshots: Minimum 2 (up to 8)
  - Phone: 320-3840 px on longest side
  - 7-inch tablet: 320-3840 px
  - 10-inch tablet: 320-3840 px
- Promo video: YouTube URL (optional)

**Store Listing Languages:**
- Support for 100+ languages
- Default language required
- Localized listings recommended

**Privacy & Security:**
- Privacy policy URL (required if app accesses sensitive data)
- Data safety section (detailed data collection disclosure)
- Target API level requirements (must target recent Android version)

### iOS (App Store Connect)

**App Information:**
- App name (max 30 characters)
- Subtitle (max 30 characters)
- Promotional text (max 170 characters, updatable without review)
- Description (max 4000 characters)
- Keywords (max 100 characters, comma-separated)
- Category (primary and optional secondary)
- Age rating (automatic based on questionnaire)

**Graphics:**
- App icon: 1024 x 1024 px (PNG, no transparency)
- Screenshots: Required for all device sizes you support
  - iPhone 6.7": 1290 x 2796 px or 1284 x 2778 px
  - iPhone 6.5": 1242 x 2688 px or 1284 x 2778 px
  - iPhone 5.5": 1242 x 2208 px
  - iPad Pro (12.9"): 2048 x 2732 px
  - iPad Pro (11"): 1668 x 2388 px
- App previews: Up to 3 videos per device size (optional)

**Store Listing Languages:**
- Support for 40+ languages
- Base language required
- Localized metadata recommended

**Privacy & Security:**
- Privacy policy URL (mandatory for all apps)
- Privacy nutrition labels (detailed questionnaire)
- App privacy questions (data collection, tracking, etc.)

**Key Difference:** iOS has character limits on app names (30 vs 50) and requires screenshots for each supported device size.

---

## Review Process

### Android (Google Play Store)

**Review Timeline:**
- Initial review: Few hours to 7 days (typically 1-3 days)
- Updates: Similar timeline
- Expedited review: Not available

**Review Focus:**
- Policy compliance (content, permissions, functionality)
- Malware and security scanning
- Metadata accuracy
- Privacy policy compliance
- Target audience appropriateness

**Automated Systems:**
- Pre-launch reports (automated testing on real devices)
- Security scanning
- Policy violation detection

**App States:**
- Draft
- Pending publication
- Published
- Rejected/Suspended

**Appeal Process:**
- Policy Center in Play Console
- Appeal within 7 days of rejection
- Response typically within 2-3 business days

### iOS (Apple App Store)

**Review Timeline:**
- Initial review: 24-48 hours (can extend to 5-7 days)
- Updates: 24-48 hours
- Expedited review: Available (limited use, requires justification)

**Review Focus:**
- Human review of all submissions
- Functionality testing on actual devices
- Design guidelines compliance
- Business model compliance
- Legal and privacy compliance
- Performance and stability

**Review Guidelines:**
- Strict adherence to App Store Review Guidelines
- Common rejection reasons:
  - Crashes or bugs
  - Incomplete information
  - Misleading metadata
  - Guideline violations
  - Privacy issues

**App States:**
- Prepare for Submission
- Waiting for Review
- In Review
- Pending Developer Release
- Ready for Sale
- Rejected

**Appeal Process:**
- App Review Board appeal
- Provide detailed explanation
- Response within 1-2 business days
- Can request phone call for complex issues

**Key Difference:** iOS has human reviewers testing every submission; Android relies more on automated systems.

---

## Publishing Steps

### Android - Step by Step

**1. Prepare Your App**
```bash
# Update version in build.gradle
android {
    defaultConfig {
        versionCode 2
        versionName "1.0.1"
    }
}

# Build signed bundle
./gradlew bundleRelease

# Sign with your keystore
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore my-release-key.keystore app-release.aab alias_name
```

**2. Create App in Play Console**
- Go to Google Play Console
- Click "Create app"
- Fill in app details (name, language, type, category)
- Accept declarations

**3. Set Up Store Listing**
- App details: descriptions, graphics
- Categorization
- Contact details
- Privacy policy

**4. Content Rating**
- Complete questionnaire
- Receive rating certificate
- Apply to all regions

**5. Set Up Pricing & Distribution**
- Select countries
- Set pricing (free or paid)
- Choose distribution options
- Opt in/out of programs (e.g., Google Play for Education)

**6. App Content**
- Target audience
- News apps declaration
- COVID-19 contact tracing
- Data safety section

**7. Create Release**
- Select release track (Internal, Closed, Open, Production)
- Upload AAB/APK
- Add release notes
- Review and roll out

**8. Submit for Review**
- Complete all required sections
- Click "Submit for review"
- Monitor status in dashboard

**9. Track Review Status**
- Check email notifications
- Monitor Play Console dashboard
- Address any issues if rejected

**10. Publish**
- Approve release if using "Managed Publishing"
- App goes live (can take few hours to propagate)

### iOS - Step by Step

**1. Prepare Your App**
```bash
# Update version in Xcode
# Project > General > Version: 1.0.1, Build: 2

# Or in Info.plist
CFBundleShortVersionString: "1.0.1"
CFBundleVersion: "2"
```

**2. Archive Your App**
- Select "Any iOS Device (arm64)" as destination
- Product > Archive
- Wait for archive to complete

**3. Create App in App Store Connect**
- Log in to App Store Connect
- Click "My Apps" > "+" > "New App"
- Fill in app information:
  - Platform (iOS)
  - App name
  - Primary language
  - Bundle ID
  - SKU

**4. App Information**
- Privacy policy URL
- Category
- License agreement (optional)
- Age rating questionnaire

**5. Pricing and Availability**
- Select price tier or custom price
- Choose availability date
- Select countries/regions

**6. Prepare for Submission**
- Screenshots for all device sizes
- App description and keywords
- Support URL
- Marketing URL (optional)
- Promotional text
- App icon (1024x1024)

**7. Build Upload**
- Open Xcode Organizer
- Select archive
- Click "Distribute App"
- Choose "App Store Connect"
- Upload to App Store Connect
- Wait for processing (10-30 minutes)

**8. Select Build**
- In App Store Connect, select uploaded build
- Fill in "What's New in This Version"
- Provide export compliance information
- Add app review information:
  - Contact information
  - Demo account (if required)
  - Notes for reviewer

**9. Submit for Review**
- Complete all required fields
- Click "Submit for Review"
- Confirm submission

**10. Track Review Status**
- Monitor email notifications
- Check App Store Connect status
- Respond to any reviewer questions

**11. Release**
- Automatic release upon approval (default)
- Or manual release if selected
- App appears on App Store within 24 hours

---

## Post-Publication

### Android

**Analytics:**
- Google Play Console statistics
- User acquisition reports
- Crash reports and ANR (Application Not Responding)
- Pre-launch reports

**Updates:**
- Upload new version
- Can use staged rollouts (5%, 10%, 20%, 50%, 100%)
- Can halt rollout if issues detected
- Can create multiple release tracks

**Release Tracks:**
- Internal testing (up to 100 testers)
- Closed testing (opt-in testers)
- Open testing (public beta)
- Production

**User Engagement:**
- Respond to reviews
- Monitor ratings
- Send update notifications

**Monetization:**
- In-app purchases
- Subscriptions
- Ads
- Paid app pricing

### iOS

**Analytics:**
- App Store Connect analytics
- Sales and trends
- Crashes (via Xcode Organizer or third-party)
- TestFlight analytics

**Updates:**
- Submit new version
- Phased release option (7-day automatic rollout)
- Can pause and resume phased releases
- TestFlight for beta testing

**Testing:**
- TestFlight internal testing (up to 100 testers)
- TestFlight external testing (up to 10,000 testers)
- External testing requires beta review

**User Engagement:**
- Respond to reviews (once per review)
- Monitor ratings by country
- Promotional offers and codes

**Monetization:**
- In-app purchases
- Subscriptions (with free trials)
- Paid app pricing
- Apple commission: 30% (15% for small businesses <$1M)

**Key Difference:** Android offers more granular staged rollouts; iOS offers phased release with simpler controls.

---

## Key Differences Summary

| Aspect | Android (Google Play) | iOS (App Store) |
|--------|----------------------|-----------------|
| **Account Fee** | $25 (one-time) | $99/year |
| **Development Platform** | Any OS | macOS required |
| **App Format** | AAB (Android App Bundle) | IPA |
| **Review Time** | 1-3 days (automated + some manual) | 1-2 days (human review) |
| **Review Process** | Primarily automated | Human reviewers |
| **Rejection Rate** | Lower (~10-15%) | Higher (~30-40%) first submission |
| **Update Rollout** | Staged rollouts (5-100%) | Phased release (7 days) |
| **App Name Length** | 50 characters | 30 characters |
| **Screenshot Requirements** | Minimum 2 per type | All supported device sizes |
| **Privacy Policy** | Required for sensitive data | Required for all apps |
| **Beta Testing** | Internal/Closed/Open tracks | TestFlight |
| **Update Frequency** | No restrictions | No restrictions (but must pass review) |
| **Rejection Appeals** | Policy Center | App Review Board |
| **Commission** | 15-30% | 15-30% |
| **Content Restrictions** | Moderate | Strict |
| **Approval Predictability** | More predictable | Less predictable |

---

## Best Practices

### For Both Platforms

1. **Test Thoroughly**
   - Test on multiple devices
   - Test all features and edge cases
   - Fix crashes and bugs before submission

2. **Prepare Complete Metadata**
   - Have all assets ready before starting
   - Write clear, compelling descriptions
   - Use keywords strategically

3. **Follow Guidelines**
   - Read platform guidelines thoroughly
   - Stay updated on policy changes
   - Avoid common rejection reasons

4. **Plan for Rejections**
   - Budget extra time for potential rejections
   - Have a response plan
   - Learn from feedback

5. **Maintain Privacy Compliance**
   - Be transparent about data collection
   - Implement proper consent mechanisms
   - Keep privacy policy updated

### Android-Specific

1. **Use App Bundles**
   - AAB reduces download size
   - Google manages signing
   - Better optimization

2. **Target Latest API Level**
   - Keep targetSdkVersion current
   - Google enforces recent API levels

3. **Leverage Testing Tracks**
   - Use internal testing first
   - Closed alpha/beta for broader testing
   - Staged rollouts for production

### iOS-Specific

1. **Prepare for Strict Review**
   - Follow Human Interface Guidelines
   - Ensure app is fully functional
   - Provide demo account if needed

2. **Use TestFlight**
   - Beta test before production
   - Gather feedback early
   - Find bugs before review

3. **Respond Quickly to Reviewers**
   - Check Resolution Center daily
   - Respond to questions promptly
   - Provide requested information

---

## Common Rejection Reasons

### Android

1. Privacy policy missing or incomplete
2. Misleading app description
3. Inappropriate content
4. Malware or security issues
5. Intellectual property violations
6. Broken functionality
7. Target API level too old
8. Missing data safety declarations

### iOS

1. App crashes or has bugs
2. Incomplete app information
3. Misleading screenshots or descriptions
4. Guideline violations (content, business model)
5. Missing privacy policy
6. Inaccurate privacy nutrition labels
7. Lack of parental consent for kids' apps
8. In-app purchase implementation issues
9. Links to external payment systems
10. Apps that are not "app-like" (basic websites)

---

## Conclusion

Both Google Play Store and Apple App Store have distinct processes, requirements, and timelines. Success on both platforms requires:

- **Preparation:** Have all assets and information ready
- **Quality:** Ensure your app is stable and polished
- **Compliance:** Follow platform guidelines strictly
- **Patience:** Budget time for review and potential revisions
- **Communication:** Respond promptly to reviewer feedback

By understanding these differences and preparing accordingly, you can successfully navigate both publishing processes and reach users on Android and iOS platforms.

---

## Additional Resources

### Android
- [Google Play Console](https://play.google.com/console)
- [Developer Policy Center](https://support.google.com/googleplay/android-developer/answer/9858738)
- [Launch Checklist](https://developer.android.com/distribute/best-practices/launch/launch-checklist)

### iOS
- [App Store Connect](https://appstoreconnect.apple.com/)
- [App Store Review Guidelines](https://developer.apple.com/app-store/review/guidelines/)
- [Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)

---

*Last Updated: November 2025*
