"""
Command-line interface for Android Security Analyzer
"""

import sys
import os
from pathlib import Path
import click
from .core import AndroidAnalyzer
from .owasp_checks import OWASPSecurityChecker
from .reporter import Reporter


@click.group()
@click.version_option(version='1.0.0')
def main():
    """Android Security Analyzer - OWASP-based static analysis for APK/AAR/JAR files"""
    pass


@main.command()
@click.argument('file_path', type=click.Path(exists=True))
@click.option('--output', '-o', default='report.html', help='Output file path')
@click.option('--format', '-f', type=click.Choice(['html', 'json', 'markdown', 'console']),
              default='html', help='Report format')
@click.option('--severity', '-s', type=click.Choice(['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO']),
              default='INFO', help='Minimum severity level to report')
def analyze(file_path, output, format, severity):
    """Analyze an APK, AAR, or JAR file for security issues"""

    click.echo(f"Analyzing {file_path}...")

    try:
        # Initialize analyzer
        analyzer = AndroidAnalyzer(file_path)

        # Run basic analysis
        results = analyzer.analyze()

        # Run OWASP security checks (for APK files)
        if analyzer.file_type == 'APK' and analyzer.apk:
            click.echo("Running OWASP security checks...")
            checker = OWASPSecurityChecker(analyzer)
            checker.run_all_checks()
            results['findings'] = analyzer.findings

        # Filter by severity
        severity_order = {'INFO': 0, 'LOW': 1, 'MEDIUM': 2, 'HIGH': 3, 'CRITICAL': 4}
        min_severity = severity_order[severity]

        results['findings'] = [
            f for f in results.get('findings', [])
            if severity_order.get(f.get('severity', 'INFO'), 0) >= min_severity
        ]

        # Generate report
        reporter = Reporter(results)

        if format == 'console':
            reporter.print_console()
        else:
            if format == 'html':
                reporter.generate_html(output)
            elif format == 'json':
                reporter.generate_json(output)
            elif format == 'markdown':
                reporter.generate_markdown(output)

            click.echo(f"\nReport saved to: {output}")

        # Summary
        total_findings = len(results.get('findings', []))
        click.echo(f"\nTotal findings: {total_findings}")

        # Exit with error code if high/critical findings
        critical_count = sum(1 for f in results.get('findings', []) if f.get('severity') == 'CRITICAL')
        high_count = sum(1 for f in results.get('findings', []) if f.get('severity') == 'HIGH')

        if critical_count > 0:
            click.echo(f"⚠️  {critical_count} CRITICAL issues found!")
            sys.exit(2)
        elif high_count > 0:
            click.echo(f"⚠️  {high_count} HIGH severity issues found!")
            sys.exit(1)

    except Exception as e:
        click.echo(f"Error: {str(e)}", err=True)
        sys.exit(1)


@main.command()
@click.argument('directory', type=click.Path(exists=True))
@click.option('--pattern', '-p', default='*.apk', help='File pattern to match')
@click.option('--output-dir', '-o', default='reports', help='Output directory for reports')
@click.option('--format', '-f', type=click.Choice(['html', 'json', 'markdown']),
              default='html', help='Report format')
def batch(directory, pattern, output_dir, format):
    """Batch analyze multiple files in a directory"""

    import glob

    files = glob.glob(os.path.join(directory, pattern))

    if not files:
        click.echo(f"No files found matching pattern: {pattern}")
        return

    os.makedirs(output_dir, exist_ok=True)

    click.echo(f"Found {len(files)} file(s) to analyze")

    for i, file_path in enumerate(files, 1):
        click.echo(f"\n[{i}/{len(files)}] Analyzing {os.path.basename(file_path)}...")

        try:
            analyzer = AndroidAnalyzer(file_path)
            results = analyzer.analyze()

            if analyzer.file_type == 'APK' and analyzer.apk:
                checker = OWASPSecurityChecker(analyzer)
                checker.run_all_checks()
                results['findings'] = analyzer.findings

            # Generate report
            reporter = Reporter(results)
            base_name = os.path.splitext(os.path.basename(file_path))[0]
            output_file = os.path.join(output_dir, f"{base_name}.{format}")

            if format == 'html':
                reporter.generate_html(output_file)
            elif format == 'json':
                reporter.generate_json(output_file)
            elif format == 'markdown':
                reporter.generate_markdown(output_file)

            click.echo(f"  ✓ Report saved: {output_file}")

        except Exception as e:
            click.echo(f"  ✗ Error: {str(e)}", err=True)

    click.echo(f"\n✓ Batch analysis complete. Reports saved to: {output_dir}")


@main.command()
def info():
    """Display information about the analyzer"""

    click.echo("""
Android Security Analyzer v1.0.0

Based on OWASP Mobile Top 10 and MASVS (Mobile Application Security Verification Standard)

Supported file types:
  - APK (Android Application Package)
  - AAR (Android Archive)
  - JAR (Java Archive)

Security checks performed:
  - M1: Improper Platform Usage
  - M2: Insecure Data Storage
  - M3: Insecure Communication
  - M4: Insecure Authentication
  - M5: Insufficient Cryptography
  - M6: Insecure Authorization
  - M7: Client Code Quality
  - M8: Code Tampering
  - M9: Reverse Engineering
  - M10: Extraneous Functionality

For more information: https://owasp.org/www-project-mobile-top-10/
    """)


if __name__ == '__main__':
    main()
