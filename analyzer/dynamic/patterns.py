"""
Shared detection patterns for dynamic analysis.

These regexes identify potentially sensitive data observed at runtime
(in logs, network traffic, or storage) so the analyzer can flag insecure
handling. Matching is deliberately conservative to limit false positives.
"""

import re
from typing import Dict, List, Pattern, Tuple

# (name, compiled pattern, severity) for values that should never appear
# in cleartext channels such as logcat or HTTP bodies.
SENSITIVE_VALUE_PATTERNS: List[Tuple[str, Pattern, str]] = [
    ("AWS Access Key ID", re.compile(r"\bAKIA[0-9A-Z]{16}\b"), "HIGH"),
    ("Google API Key", re.compile(r"\bAIza[0-9A-Za-z_\-]{35}\b"), "HIGH"),
    ("JWT", re.compile(r"\beyJ[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+"), "MEDIUM"),
    ("Bearer Token", re.compile(r"[Bb]earer\s+[A-Za-z0-9_\-\.=]{20,}"), "MEDIUM"),
    ("Private Key Block", re.compile(r"-----BEGIN (?:RSA |EC )?PRIVATE KEY-----"), "HIGH"),
    ("Basic Auth Header", re.compile(r"[Bb]asic\s+[A-Za-z0-9+/]{16,}={0,2}"), "MEDIUM"),
    ("Credit Card (PAN)", re.compile(r"\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13})\b"), "HIGH"),
    ("Email Address", re.compile(r"\b[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}\b"), "LOW"),
    ("US SSN", re.compile(r"\b[0-9]{3}-[0-9]{2}-[0-9]{4}\b"), "MEDIUM"),
]

# Keys whose presence next to a value suggests a secret, e.g. `password=...`.
SENSITIVE_KEY_PATTERNS: List[Tuple[str, Pattern, str]] = [
    ("Password", re.compile(r"(?i)\b(pass(?:word|wd|phrase)?)\b\s*[:=]\s*\S+"), "HIGH"),
    ("Secret", re.compile(r"(?i)\b(secret|client[_\-]?secret)\b\s*[:=]\s*\S+"), "HIGH"),
    ("API Key", re.compile(r"(?i)\b(api[_\-]?key|apikey)\b\s*[:=]\s*\S+"), "HIGH"),
    ("Token", re.compile(r"(?i)\b(access[_\-]?token|auth[_\-]?token|session[_\-]?token)\b\s*[:=]\s*\S+"), "MEDIUM"),
    ("Session ID", re.compile(r"(?i)\b(session[_\-]?id|sessionid|jsessionid)\b\s*[:=]\s*\S+"), "MEDIUM"),
]

# Cryptographic transforms considered weak when seen in use at runtime.
# Maps a normalized token -> (human description, severity).
WEAK_CRYPTO_TRANSFORMS: Dict[str, Tuple[str, str]] = {
    "DES": ("DES is obsolete and brute-forceable", "HIGH"),
    "DESEDE": ("Triple DES (3DES) is deprecated", "MEDIUM"),
    "RC4": ("RC4 stream cipher is broken", "HIGH"),
    "RC2": ("RC2 cipher is weak", "HIGH"),
    "BLOWFISH": ("Blowfish has a 64-bit block and is discouraged", "MEDIUM"),
    "AES/ECB": ("ECB mode leaks plaintext structure", "HIGH"),
    "AES": ("AES without an authenticated mode may be misused; prefer GCM", "LOW"),
    "RSA/ECB/NOPADDING": ("RSA with NoPadding is insecure", "HIGH"),
    "RSA/ECB/PKCS1PADDING": ("RSA PKCS#1 v1.5 padding is vulnerable to padding oracles", "MEDIUM"),
}

# Message-digest algorithms considered weak at runtime.
WEAK_DIGESTS: Dict[str, Tuple[str, str]] = {
    "MD2": ("MD2 is broken", "HIGH"),
    "MD5": ("MD5 is collision-prone", "HIGH"),
    "SHA-1": ("SHA-1 is deprecated for security use", "MEDIUM"),
    "SHA1": ("SHA-1 is deprecated for security use", "MEDIUM"),
}


def normalize_transform(transform: str) -> str:
    """Normalize a Cipher transform string for lookup (upper, trimmed)."""
    return transform.strip().upper()


def scan_text(text: str) -> List[Dict[str, str]]:
    """
    Scan a block of text for sensitive values and key/value secrets.

    Returns a list of dicts: {category, severity, match_type, excerpt}.
    The excerpt is truncated and never includes the full matched secret
    to avoid the report itself becoming a leak.
    """
    results: List[Dict[str, str]] = []
    if not text:
        return results

    for name, pattern, severity in SENSITIVE_VALUE_PATTERNS:
        for m in pattern.finditer(text):
            results.append({
                "category": name,
                "severity": severity,
                "match_type": "value",
                "excerpt": _redact(m.group(0)),
            })

    for name, pattern, severity in SENSITIVE_KEY_PATTERNS:
        for m in pattern.finditer(text):
            results.append({
                "category": name,
                "severity": severity,
                "match_type": "key-value",
                "excerpt": _redact(m.group(0)),
            })

    return results


def _redact(value: str, keep: int = 4) -> str:
    """Show only a prefix of a matched secret, masking the remainder."""
    value = value.replace("\n", " ").strip()
    if len(value) <= keep:
        return "*" * len(value)
    return value[:keep] + "*" * min(len(value) - keep, 12)
