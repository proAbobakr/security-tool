from setuptools import setup, find_packages

with open("README.md", "r", encoding="utf-8") as fh:
    long_description = fh.read()

setup(
    name="android-security-analyzer",
    version="1.0.0",
    author="Security Tool Team",
    description="OWASP-based static analysis tool for Android APK, AAR, and JAR files",
    long_description=long_description,
    long_description_content_type="text/markdown",
    packages=find_packages(),
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ],
    python_requires=">=3.8",
    install_requires=[
        "androguard>=3.4.0a1",
        "lxml>=4.9.3",
        "requests>=2.31.0",
        "pyyaml>=6.0.1",
        "jinja2>=3.1.2",
        "colorama>=0.4.6",
        "click>=8.1.7",
        "python-magic>=0.4.27",
    ],
    entry_points={
        "console_scripts": [
            "android-security-analyzer=analyzer.cli:main",
        ],
    },
)
