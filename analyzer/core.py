"""
Core analyzer module for APK, AAR, and JAR files
"""

import os
import zipfile
import xml.etree.ElementTree as ET
from typing import Dict, List, Any, Optional
from pathlib import Path
import hashlib

try:
    from androguard.core.bytecodes.apk import APK
    from androguard.core.bytecodes.dvm import DalvikVMFormat
    from androguard.core.analysis.analysis import Analysis
    ANDROGUARD_AVAILABLE = True
except ImportError:
    ANDROGUARD_AVAILABLE = False


class AndroidAnalyzer:
    """Main analyzer class for Android artifacts"""

    def __init__(self, file_path: str):
        self.file_path = file_path
        self.file_type = self._detect_file_type()
        self.findings: List[Dict[str, Any]] = []
        self.apk = None
        self.analysis = None

    def _detect_file_type(self) -> str:
        """Detect if file is APK, AAR, or JAR"""
        ext = os.path.splitext(self.file_path)[1].lower()
        if ext == '.apk':
            return 'APK'
        elif ext == '.aar':
            return 'AAR'
        elif ext == '.jar':
            return 'JAR'
        else:
            raise ValueError(f"Unsupported file type: {ext}")

    def analyze(self) -> Dict[str, Any]:
        """Main analysis entry point"""
        results = {
            'file_path': self.file_path,
            'file_type': self.file_type,
            'file_hash': self._calculate_hash(),
            'file_size': os.path.getsize(self.file_path),
            'findings': [],
            'metadata': {}
        }

        if self.file_type == 'APK':
            results.update(self._analyze_apk())
        elif self.file_type == 'AAR':
            results.update(self._analyze_aar())
        elif self.file_type == 'JAR':
            results.update(self._analyze_jar())

        results['findings'] = self.findings
        return results

    def _calculate_hash(self) -> Dict[str, str]:
        """Calculate file hashes"""
        hashes = {}
        algorithms = ['md5', 'sha1', 'sha256']

        with open(self.file_path, 'rb') as f:
            data = f.read()
            for algo in algorithms:
                h = hashlib.new(algo)
                h.update(data)
                hashes[algo] = h.hexdigest()

        return hashes

    def _analyze_apk(self) -> Dict[str, Any]:
        """Analyze APK file"""
        if not ANDROGUARD_AVAILABLE:
            self.add_finding(
                "ERROR",
                "Androguard not available",
                "Install androguard to analyze APK files"
            )
            return {}

        try:
            self.apk = APK(self.file_path)

            metadata = {
                'package_name': self.apk.get_package(),
                'app_name': self.apk.get_app_name(),
                'version_name': self.apk.get_androidversion_name(),
                'version_code': self.apk.get_androidversion_code(),
                'min_sdk': self.apk.get_min_sdk_version(),
                'target_sdk': self.apk.get_target_sdk_version(),
                'permissions': self.apk.get_permissions(),
                'activities': self.apk.get_activities(),
                'services': self.apk.get_services(),
                'receivers': self.apk.get_receivers(),
                'providers': self.apk.get_providers(),
            }

            return {'metadata': metadata}

        except Exception as e:
            self.add_finding("ERROR", "APK Analysis Failed", str(e))
            return {}

    def _analyze_aar(self) -> Dict[str, Any]:
        """Analyze AAR (Android Archive) file"""
        metadata = {}

        try:
            with zipfile.ZipFile(self.file_path, 'r') as zip_ref:
                files = zip_ref.namelist()
                metadata['files'] = files

                # Check for AndroidManifest.xml
                if 'AndroidManifest.xml' in files:
                    manifest_data = zip_ref.read('AndroidManifest.xml')
                    # Parse binary XML if needed
                    metadata['has_manifest'] = True

                # Check for classes.jar
                if 'classes.jar' in files:
                    metadata['has_classes'] = True

                # Check for native libraries
                native_libs = [f for f in files if f.startswith('jni/')]
                if native_libs:
                    metadata['native_libraries'] = native_libs

        except Exception as e:
            self.add_finding("ERROR", "AAR Analysis Failed", str(e))

        return {'metadata': metadata}

    def _analyze_jar(self) -> Dict[str, Any]:
        """Analyze JAR file"""
        metadata = {}

        try:
            with zipfile.ZipFile(self.file_path, 'r') as zip_ref:
                files = zip_ref.namelist()

                # Count class files
                class_files = [f for f in files if f.endswith('.class')]
                metadata['class_count'] = len(class_files)
                metadata['total_files'] = len(files)

                # Check for META-INF
                if 'META-INF/MANIFEST.MF' in files:
                    manifest = zip_ref.read('META-INF/MANIFEST.MF').decode('utf-8', errors='ignore')
                    metadata['manifest'] = manifest

        except Exception as e:
            self.add_finding("ERROR", "JAR Analysis Failed", str(e))

        return {'metadata': metadata}

    def add_finding(self, severity: str, title: str, description: str,
                   owasp_category: Optional[str] = None,
                   cwe: Optional[str] = None,
                   recommendation: Optional[str] = None):
        """Add a security finding"""
        finding = {
            'severity': severity,
            'title': title,
            'description': description,
        }

        if owasp_category:
            finding['owasp_category'] = owasp_category
        if cwe:
            finding['cwe'] = cwe
        if recommendation:
            finding['recommendation'] = recommendation

        self.findings.append(finding)
