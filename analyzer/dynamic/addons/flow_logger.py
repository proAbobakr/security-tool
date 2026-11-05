"""
mitmproxy addon: normalize each HTTP flow to one JSON line.

Runs inside `mitmdump -s flow_logger.py --set flow_out=<path>`. It imports
mitmproxy only within the mitmdump process, so the analyzer package never
depends on mitmproxy being importable in its own environment.

Bodies are truncated to keep the log bounded; the analyzer scans these
excerpts for sensitive data exposed over the wire.
"""

import json
import os

from mitmproxy import ctx, http

_MAX_BODY = 8192


class FlowLogger:
    def __init__(self):
        self.path = None

    def load(self, loader):
        loader.add_option(
            name="flow_out", typespec=str, default="flows.jsonl",
            help="Path to write normalized flow records (JSONL).",
        )

    def configure(self, updated):
        if "flow_out" in updated:
            self.path = ctx.options.flow_out

    def _body(self, message) -> str:
        try:
            text = message.get_text(strict=False) or ""
        except Exception:
            text = ""
        return text[:_MAX_BODY]

    def response(self, flow: http.HTTPFlow):
        self._write(flow)

    def error(self, flow: http.HTTPFlow):
        self._write(flow)

    def _write(self, flow: http.HTTPFlow):
        path = self.path or ctx.options.flow_out
        req = flow.request
        record = {
            "scheme": req.scheme,
            "method": req.method,
            "host": req.host,
            "port": req.port,
            "path": req.path,
            "url": req.pretty_url,
            "req_headers": dict(req.headers),
            "req_body": self._body(req),
            "status": flow.response.status_code if flow.response else None,
            "resp_headers": dict(flow.response.headers) if flow.response else {},
            "resp_body": self._body(flow.response) if flow.response else "",
        }
        with open(path, "a", encoding="utf-8") as fh:
            fh.write(json.dumps(record) + "\n")


addons = [FlowLogger()]
