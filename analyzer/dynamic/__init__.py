"""
Dynamic analysis package.

Runtime security observation for Android apps on a connected device or
emulator, complementing the static analyzer. Designed for authorized
assessment of applications you own or are permitted to test, following the
OWASP Mobile Application Security Testing Guide (MASTG).

External dependencies (adb, frida, mitmproxy) are optional and detected at
runtime; each component degrades to a clear, non-fatal error when a tool is
unavailable so the rest of the pipeline keeps working.
"""

from .runner import DynamicAnalyzer, DynamicConfig

__all__ = ["DynamicAnalyzer", "DynamicConfig"]
