"""
OWASP Mobile Security checks based on OWASP Mobile Top 10 and MASVS
"""

import re
import xml.etree.ElementTree as ET
from typing import List, Dict, Any, Optional

try:
    from androguard.core.bytecodes.apk import APK
    ANDROGUARD_AVAILABLE = True
except ImportError:
    ANDROGUARD_AVAILABLE = False


class OWASPSecurityChecker:
    """
    Implements OWASP Mobile Top 10 security checks:
    M1: Improper Platform Usage
    M2: Insecure Data Storage
    M3: Insecure Communication
    M4: Insecure Authentication
    M5: Insufficient Cryptography
    M6: Insecure Authorization
    M7: Client Code Quality
    M8: Code Tampering
    M9: Reverse Engineering
    M10: Extraneous Functionality
    """

    def __init__(self, analyzer):
        self.analyzer = analyzer
        self.apk = analyzer.apk

    def run_all_checks(self):
        """Run all OWASP security checks"""
        if not self.apk:
            return

        self.check_permissions()
        self.check_debuggable()
        self.check_backup_allowed()
        self.check_network_security()
        self.check_exported_components()
        self.check_insecure_algorithms()
        self.check_hardcoded_secrets()
        self.check_webview_security()
        self.check_certificate_pinning()
        self.check_root_detection()
        self.check_obfuscation()
        self.check_insecure_storage()
        self.check_logging()
        self.check_sql_injection()
        self.check_deeplinks()

    def check_permissions(self):
        """M1: Check for dangerous permissions"""
        dangerous_permissions = {
            'android.permission.READ_SMS': 'Can read SMS messages',
            'android.permission.SEND_SMS': 'Can send SMS messages',
            'android.permission.READ_CONTACTS': 'Can access contacts',
            'android.permission.WRITE_CONTACTS': 'Can modify contacts',
            'android.permission.ACCESS_FINE_LOCATION': 'Can access precise location',
            'android.permission.ACCESS_COARSE_LOCATION': 'Can access approximate location',
            'android.permission.CAMERA': 'Can access camera',
            'android.permission.RECORD_AUDIO': 'Can record audio',
            'android.permission.READ_PHONE_STATE': 'Can read phone state',
            'android.permission.CALL_PHONE': 'Can make phone calls',
            'android.permission.READ_CALL_LOG': 'Can read call logs',
            'android.permission.WRITE_CALL_LOG': 'Can write call logs',
            'android.permission.READ_EXTERNAL_STORAGE': 'Can read external storage',
            'android.permission.WRITE_EXTERNAL_STORAGE': 'Can write to external storage',
            'android.permission.GET_ACCOUNTS': 'Can access account list',
            'android.permission.READ_CALENDAR': 'Can read calendar',
            'android.permission.WRITE_CALENDAR': 'Can modify calendar',
        }

        permissions = self.apk.get_permissions()

        for perm in permissions:
            if perm in dangerous_permissions:
                self.analyzer.add_finding(
                    'MEDIUM',
                    f'Dangerous Permission: {perm}',
                    dangerous_permissions[perm],
                    owasp_category='M1: Improper Platform Usage',
                    recommendation='Ensure this permission is necessary and properly justified to users'
                )

    def check_debuggable(self):
        """M1: Check if app is debuggable"""
        if self.apk.get_attribute_value('application', 'debuggable') == 'true':
            self.analyzer.add_finding(
                'HIGH',
                'Application is Debuggable',
                'The application has android:debuggable="true" which allows debugging in production',
                owasp_category='M7: Client Code Quality',
                cwe='CWE-489',
                recommendation='Set android:debuggable="false" in production builds'
            )

    def check_backup_allowed(self):
        """M2: Check if backup is allowed"""
        if self.apk.get_attribute_value('application', 'allowBackup') == 'true':
            self.analyzer.add_finding(
                'MEDIUM',
                'Backup Allowed',
                'Application allows backup which may expose sensitive data',
                owasp_category='M2: Insecure Data Storage',
                cwe='CWE-200',
                recommendation='Set android:allowBackup="false" or implement backup rules'
            )

    def check_network_security(self):
        """M3: Check network security configuration"""
        # Check for cleartext traffic
        uses_cleartext = self.apk.get_attribute_value('application', 'usesCleartextTraffic')

        if uses_cleartext == 'true':
            self.analyzer.add_finding(
                'HIGH',
                'Cleartext Traffic Allowed',
                'Application allows cleartext (HTTP) traffic',
                owasp_category='M3: Insecure Communication',
                cwe='CWE-319',
                recommendation='Disable cleartext traffic and use HTTPS only'
            )

        # Check for network security config
        network_config = self.apk.get_attribute_value('application', 'networkSecurityConfig')
        if not network_config:
            self.analyzer.add_finding(
                'LOW',
                'No Network Security Configuration',
                'Application does not define a network security configuration',
                owasp_category='M3: Insecure Communication',
                recommendation='Implement network security configuration to enforce HTTPS and certificate pinning'
            )

    def check_exported_components(self):
        """M1/M6: Check for exported components without permissions"""
        # Check activities
        for activity in self.apk.get_activities():
            if self._is_component_exported(activity, 'activity'):
                self.analyzer.add_finding(
                    'MEDIUM',
                    f'Exported Activity: {activity}',
                    'Activity is exported and may be accessible to other apps',
                    owasp_category='M6: Insecure Authorization',
                    cwe='CWE-927',
                    recommendation='Add permission requirements or set android:exported="false"'
                )

        # Check services
        for service in self.apk.get_services():
            if self._is_component_exported(service, 'service'):
                self.analyzer.add_finding(
                    'HIGH',
                    f'Exported Service: {service}',
                    'Service is exported and may be accessible to other apps',
                    owasp_category='M6: Insecure Authorization',
                    cwe='CWE-927',
                    recommendation='Add permission requirements or set android:exported="false"'
                )

        # Check receivers
        for receiver in self.apk.get_receivers():
            if self._is_component_exported(receiver, 'receiver'):
                self.analyzer.add_finding(
                    'MEDIUM',
                    f'Exported Receiver: {receiver}',
                    'Broadcast receiver is exported and may receive intents from other apps',
                    owasp_category='M6: Insecure Authorization',
                    cwe='CWE-927',
                    recommendation='Add permission requirements or set android:exported="false"'
                )

        # Check providers
        for provider in self.apk.get_providers():
            self.analyzer.add_finding(
                'HIGH',
                f'Content Provider: {provider}',
                'Content provider detected - verify it has proper permissions',
                owasp_category='M2: Insecure Data Storage',
                cwe='CWE-926',
                recommendation='Ensure content provider has proper read/write permissions'
            )

    def _is_component_exported(self, component: str, component_type: str) -> bool:
        """Check if a component is exported"""
        # Simplified check - in production would parse AndroidManifest.xml properly
        return True  # Conservative approach - flag all for review

    def check_insecure_algorithms(self):
        """M5: Check for weak cryptographic algorithms"""
        weak_patterns = [
            (r'DES', 'DES encryption is weak'),
            (r'MD5', 'MD5 hashing is cryptographically broken'),
            (r'SHA-?1', 'SHA-1 is considered weak'),
            (r'ECB', 'ECB mode is insecure'),
            (r'Random\(\)', 'Using Random() instead of SecureRandom'),
        ]

        # This would normally scan DEX bytecode
        # Placeholder for demonstration
        pass

    def check_hardcoded_secrets(self):
        """M5: Check for hardcoded secrets in strings"""
        strings = []
        try:
            # Get all strings from APK
            strings = list(self.apk.get_strings())
        except:
            pass

        secret_patterns = [
            (r'api[_-]?key', 'Potential API key'),
            (r'api[_-]?secret', 'Potential API secret'),
            (r'password\s*=\s*["\'][^"\']+["\']', 'Hardcoded password'),
            (r'secret\s*=\s*["\'][^"\']+["\']', 'Hardcoded secret'),
            (r'token\s*=\s*["\'][^"\']+["\']', 'Hardcoded token'),
            (r'aws_access_key', 'AWS access key'),
            (r'aws_secret_key', 'AWS secret key'),
            (r'private[_-]?key', 'Private key'),
        ]

        for string in strings[:1000]:  # Limit to avoid performance issues
            for pattern, description in secret_patterns:
                if re.search(pattern, string, re.IGNORECASE):
                    self.analyzer.add_finding(
                        'HIGH',
                        f'Potential Hardcoded Secret',
                        f'{description}: {string[:100]}...',
                        owasp_category='M5: Insufficient Cryptography',
                        cwe='CWE-798',
                        recommendation='Never hardcode secrets - use secure configuration or key management'
                    )
                    break

    def check_webview_security(self):
        """M1: Check WebView security settings"""
        # This would scan for WebView usage in bytecode
        # Placeholder - would check for:
        # - setJavaScriptEnabled(true)
        # - setAllowFileAccess(true)
        # - setAllowFileAccessFromFileURLs(true)
        # - setAllowUniversalAccessFromFileURLs(true)
        pass

    def check_certificate_pinning(self):
        """M3: Check for certificate pinning implementation"""
        # Check if network security config exists
        network_config = self.apk.get_attribute_value('application', 'networkSecurityConfig')

        if not network_config:
            self.analyzer.add_finding(
                'MEDIUM',
                'No Certificate Pinning Detected',
                'Application does not implement certificate pinning',
                owasp_category='M3: Insecure Communication',
                recommendation='Implement certificate pinning to prevent man-in-the-middle attacks'
            )

    def check_root_detection(self):
        """M8: Check for root detection mechanisms"""
        # This would scan bytecode for root detection
        # Placeholder
        self.analyzer.add_finding(
            'INFO',
            'Root Detection Check',
            'Verify if root detection is implemented',
            owasp_category='M8: Code Tampering',
            recommendation='Implement root detection for sensitive applications'
        )

    def check_obfuscation(self):
        """M9: Check if code is obfuscated"""
        # Simple heuristic: check if class names look obfuscated
        activities = self.apk.get_activities()

        obfuscated_count = 0
        for activity in activities[:10]:  # Sample first 10
            # Check for single letter class names (common in ProGuard/R8)
            if re.search(r'\.[a-z]$', activity):
                obfuscated_count += 1

        if obfuscated_count < len(activities[:10]) * 0.3:
            self.analyzer.add_finding(
                'MEDIUM',
                'Code May Not Be Obfuscated',
                'Application code does not appear to be obfuscated',
                owasp_category='M9: Reverse Engineering',
                recommendation='Use ProGuard or R8 to obfuscate code in release builds'
            )

    def check_insecure_storage(self):
        """M2: Check for insecure data storage indicators"""
        permissions = self.apk.get_permissions()

        if 'android.permission.WRITE_EXTERNAL_STORAGE' in permissions:
            self.analyzer.add_finding(
                'MEDIUM',
                'External Storage Access',
                'Application can write to external storage which is world-readable',
                owasp_category='M2: Insecure Data Storage',
                cwe='CWE-312',
                recommendation='Avoid storing sensitive data on external storage. Use internal storage or encrypt data'
            )

    def check_logging(self):
        """M7: Check for logging (android.util.Log)"""
        # This would scan bytecode for Log.d, Log.v, etc.
        # Placeholder
        self.analyzer.add_finding(
            'INFO',
            'Logging Check',
            'Verify that sensitive information is not logged',
            owasp_category='M7: Client Code Quality',
            cwe='CWE-532',
            recommendation='Remove or disable verbose logging in production builds'
        )

    def check_sql_injection(self):
        """M7: Check for SQL injection vulnerabilities"""
        # This would scan for rawQuery and execSQL usage
        # Placeholder
        pass

    def check_deeplinks(self):
        """M1/M6: Check for insecure deep link handling"""
        # Check for intent filters with data schemes
        # Placeholder
        self.analyzer.add_finding(
            'INFO',
            'Deep Link Check',
            'Verify that deep links are properly validated',
            owasp_category='M1: Improper Platform Usage',
            recommendation='Validate and sanitize all deep link inputs'
        )
