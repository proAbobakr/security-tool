"""
Report generation module
"""

import json
from datetime import datetime
from typing import Dict, Any, List
from pathlib import Path


class Reporter:
    """Generate security analysis reports in various formats"""

    def __init__(self, results: Dict[str, Any]):
        self.results = results

    def generate_json(self, output_path: str):
        """Generate JSON report"""
        report = {
            'generated_at': datetime.now().isoformat(),
            'analyzer_version': '1.0.0',
            'results': self.results
        }

        with open(output_path, 'w') as f:
            json.dump(report, f, indent=2)

    def generate_html(self, output_path: str):
        """Generate HTML report"""
        html_template = """
<!DOCTYPE html>
<html>
<head>
    <title>Android Security Analysis Report</title>
    <style>
        body {{
            font-family: Arial, sans-serif;
            margin: 20px;
            background-color: #f5f5f5;
        }}
        .container {{
            max-width: 1200px;
            margin: 0 auto;
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }}
        h1 {{
            color: #333;
            border-bottom: 3px solid #007bff;
            padding-bottom: 10px;
        }}
        h2 {{
            color: #555;
            margin-top: 30px;
        }}
        .summary {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin: 20px 0;
        }}
        .summary-card {{
            padding: 15px;
            border-radius: 5px;
            text-align: center;
        }}
        .summary-card h3 {{
            margin: 0;
            font-size: 2em;
        }}
        .summary-card p {{
            margin: 5px 0 0 0;
            color: #666;
        }}
        .critical {{ background-color: #dc3545; color: white; }}
        .high {{ background-color: #fd7e14; color: white; }}
        .medium {{ background-color: #ffc107; color: #333; }}
        .low {{ background-color: #28a745; color: white; }}
        .info {{ background-color: #17a2b8; color: white; }}
        .finding {{
            margin: 15px 0;
            padding: 15px;
            border-left: 4px solid;
            background-color: #f8f9fa;
            border-radius: 4px;
        }}
        .finding.CRITICAL {{ border-left-color: #dc3545; }}
        .finding.HIGH {{ border-left-color: #fd7e14; }}
        .finding.MEDIUM {{ border-left-color: #ffc107; }}
        .finding.LOW {{ border-left-color: #28a745; }}
        .finding.INFO {{ border-left-color: #17a2b8; }}
        .finding h4 {{
            margin: 0 0 10px 0;
            color: #333;
        }}
        .finding .severity {{
            display: inline-block;
            padding: 3px 10px;
            border-radius: 3px;
            font-size: 0.85em;
            font-weight: bold;
            margin-right: 10px;
        }}
        .severity.CRITICAL {{ background-color: #dc3545; color: white; }}
        .severity.HIGH {{ background-color: #fd7e14; color: white; }}
        .severity.MEDIUM {{ background-color: #ffc107; color: #333; }}
        .severity.LOW {{ background-color: #28a745; color: white; }}
        .severity.INFO {{ background-color: #17a2b8; color: white; }}
        .metadata {{
            background-color: #e9ecef;
            padding: 15px;
            border-radius: 5px;
            margin: 20px 0;
        }}
        .metadata-item {{
            margin: 5px 0;
        }}
        .metadata-label {{
            font-weight: bold;
            color: #495057;
        }}
        table {{
            width: 100%;
            border-collapse: collapse;
            margin: 10px 0;
        }}
        th, td {{
            padding: 10px;
            text-align: left;
            border-bottom: 1px solid #dee2e6;
        }}
        th {{
            background-color: #007bff;
            color: white;
        }}
        .footer {{
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #dee2e6;
            color: #6c757d;
            text-align: center;
        }}
    </style>
</head>
<body>
    <div class="container">
        <h1>Android Security Analysis Report</h1>

        <div class="metadata">
            <div class="metadata-item">
                <span class="metadata-label">File:</span> {file_path}
            </div>
            <div class="metadata-item">
                <span class="metadata-label">Type:</span> {file_type}
            </div>
            <div class="metadata-item">
                <span class="metadata-label">Size:</span> {file_size} bytes
            </div>
            <div class="metadata-item">
                <span class="metadata-label">SHA-256:</span> <code>{sha256}</code>
            </div>
            <div class="metadata-item">
                <span class="metadata-label">Generated:</span> {generated_at}
            </div>
        </div>

        <h2>Summary</h2>
        <div class="summary">
            {summary_cards}
        </div>

        {metadata_section}

        <h2>Security Findings</h2>
        {findings_section}

        <div class="footer">
            <p>Generated by Android Security Analyzer v1.0.0</p>
            <p>Based on OWASP Mobile Top 10 and MASVS</p>
        </div>
    </div>
</body>
</html>
"""

        # Count findings by severity
        severity_counts = {'CRITICAL': 0, 'HIGH': 0, 'MEDIUM': 0, 'LOW': 0, 'INFO': 0}
        for finding in self.results.get('findings', []):
            severity = finding.get('severity', 'INFO')
            severity_counts[severity] = severity_counts.get(severity, 0) + 1

        # Generate summary cards
        summary_cards = ''
        for severity, count in severity_counts.items():
            summary_cards += f'''
            <div class="summary-card {severity.lower()}">
                <h3>{count}</h3>
                <p>{severity}</p>
            </div>
            '''

        # Generate metadata section
        metadata_section = ''
        if self.results.get('metadata'):
            metadata_section = '<h2>Application Metadata</h2><div class="metadata">'
            metadata = self.results['metadata']

            if 'package_name' in metadata:
                metadata_section += f'<div class="metadata-item"><span class="metadata-label">Package:</span> {metadata["package_name"]}</div>'
            if 'version_name' in metadata:
                metadata_section += f'<div class="metadata-item"><span class="metadata-label">Version:</span> {metadata["version_name"]} ({metadata.get("version_code", "?")})</div>'
            if 'min_sdk' in metadata:
                metadata_section += f'<div class="metadata-item"><span class="metadata-label">Min SDK:</span> {metadata["min_sdk"]}</div>'
            if 'target_sdk' in metadata:
                metadata_section += f'<div class="metadata-item"><span class="metadata-label">Target SDK:</span> {metadata["target_sdk"]}</div>'

            # Permissions
            if 'permissions' in metadata and metadata['permissions']:
                metadata_section += '<div class="metadata-item"><span class="metadata-label">Permissions:</span><ul>'
                for perm in metadata['permissions'][:20]:  # Limit display
                    metadata_section += f'<li><code>{perm}</code></li>'
                if len(metadata['permissions']) > 20:
                    metadata_section += f'<li>... and {len(metadata["permissions"]) - 20} more</li>'
                metadata_section += '</ul></div>'

            metadata_section += '</div>'

        # Generate findings
        findings_section = ''
        if self.results.get('findings'):
            for finding in self.results['findings']:
                severity = finding.get('severity', 'INFO')
                title = finding.get('title', 'Unknown')
                description = finding.get('description', '')
                owasp = finding.get('owasp_category', '')
                cwe = finding.get('cwe', '')
                recommendation = finding.get('recommendation', '')

                findings_section += f'''
                <div class="finding {severity}">
                    <h4>
                        <span class="severity {severity}">{severity}</span>
                        {title}
                    </h4>
                    <p><strong>Description:</strong> {description}</p>
                    {f'<p><strong>OWASP Category:</strong> {owasp}</p>' if owasp else ''}
                    {f'<p><strong>CWE:</strong> {cwe}</p>' if cwe else ''}
                    {f'<p><strong>Recommendation:</strong> {recommendation}</p>' if recommendation else ''}
                </div>
                '''
        else:
            findings_section = '<p>No security findings detected.</p>'

        # Fill template
        html = html_template.format(
            file_path=self.results.get('file_path', ''),
            file_type=self.results.get('file_type', ''),
            file_size=self.results.get('file_size', 0),
            sha256=self.results.get('file_hash', {}).get('sha256', ''),
            generated_at=datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
            summary_cards=summary_cards,
            metadata_section=metadata_section,
            findings_section=findings_section
        )

        with open(output_path, 'w') as f:
            f.write(html)

    def generate_markdown(self, output_path: str):
        """Generate Markdown report"""
        md = f"""# Android Security Analysis Report

## File Information
- **Path**: {self.results.get('file_path', '')}
- **Type**: {self.results.get('file_type', '')}
- **Size**: {self.results.get('file_size', 0)} bytes
- **SHA-256**: {self.results.get('file_hash', {}).get('sha256', '')}
- **Generated**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}

## Summary
"""

        # Count findings
        severity_counts = {}
        for finding in self.results.get('findings', []):
            severity = finding.get('severity', 'INFO')
            severity_counts[severity] = severity_counts.get(severity, 0) + 1

        for severity in ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO']:
            count = severity_counts.get(severity, 0)
            md += f"- **{severity}**: {count}\n"

        md += "\n## Security Findings\n\n"

        if self.results.get('findings'):
            for i, finding in enumerate(self.results['findings'], 1):
                md += f"### {i}. [{finding.get('severity', 'INFO')}] {finding.get('title', 'Unknown')}\n\n"
                md += f"**Description**: {finding.get('description', '')}\n\n"

                if finding.get('owasp_category'):
                    md += f"**OWASP Category**: {finding['owasp_category']}\n\n"

                if finding.get('cwe'):
                    md += f"**CWE**: {finding['cwe']}\n\n"

                if finding.get('recommendation'):
                    md += f"**Recommendation**: {finding['recommendation']}\n\n"

                md += "---\n\n"
        else:
            md += "No security findings detected.\n"

        with open(output_path, 'w') as f:
            f.write(md)

    def print_console(self):
        """Print report to console"""
        try:
            from colorama import Fore, Style, init
            init()
            has_color = True
        except ImportError:
            has_color = False

        def colorize(text, severity):
            if not has_color:
                return text

            colors = {
                'CRITICAL': Fore.RED,
                'HIGH': Fore.LIGHTRED_EX,
                'MEDIUM': Fore.YELLOW,
                'LOW': Fore.GREEN,
                'INFO': Fore.CYAN,
            }
            return f"{colors.get(severity, '')}{text}{Style.RESET_ALL}"

        print("\n" + "="*80)
        print("ANDROID SECURITY ANALYSIS REPORT")
        print("="*80)

        print(f"\nFile: {self.results.get('file_path', '')}")
        print(f"Type: {self.results.get('file_type', '')}")
        print(f"Size: {self.results.get('file_size', 0)} bytes")

        # Summary
        print("\nSUMMARY:")
        severity_counts = {}
        for finding in self.results.get('findings', []):
            severity = finding.get('severity', 'INFO')
            severity_counts[severity] = severity_counts.get(severity, 0) + 1

        for severity in ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO']:
            count = severity_counts.get(severity, 0)
            print(f"  {colorize(severity, severity)}: {count}")

        # Findings
        print(f"\nFINDINGS ({len(self.results.get('findings', []))}):")
        print("-"*80)

        for i, finding in enumerate(self.results.get('findings', []), 1):
            severity = finding.get('severity', 'INFO')
            print(f"\n{i}. {colorize(f'[{severity}]', severity)} {finding.get('title', 'Unknown')}")
            print(f"   {finding.get('description', '')}")

            if finding.get('owasp_category'):
                print(f"   OWASP: {finding['owasp_category']}")

            if finding.get('recommendation'):
                print(f"   Fix: {finding['recommendation']}")

        print("\n" + "="*80 + "\n")
