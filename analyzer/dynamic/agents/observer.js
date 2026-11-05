/*
 * Runtime security observer agent (Frida + frida-java-bridge).
 *
 * Instruments common Android security-relevant APIs to REPORT insecure usage
 * for an authorized security assessment. Hooks wrap the original methods and
 * always delegate to them unchanged -- this agent observes behavior, it does
 * not alter, bypass, or defeat any security control. Findings are emitted via
 * send() and correlated on the Python side.
 */
import Java from "frida-java-bridge";

function finding(check, severity, owasp, title, detail) {
  send({ kind: "finding", check, severity, owasp, title, detail });
}

function observation(channel, data) {
  send({ kind: "observation", channel, data });
}

function safe(label, fn) {
  try {
    fn();
  } catch (e) {
    send({ kind: "hook-error", label, error: String(e) });
  }
}

const WEAK_CIPHER = [
  ["DES", "HIGH"], ["DESEDE", "MEDIUM"], ["RC4", "HIGH"], ["RC2", "HIGH"],
  ["BLOWFISH", "MEDIUM"],
];
const WEAK_DIGEST = [["MD5", "HIGH"], ["MD2", "HIGH"], ["SHA-1", "MEDIUM"], ["SHA1", "MEDIUM"]];

function classifyTransform(t) {
  const up = (t || "").toUpperCase();
  if (up.includes("/ECB")) return ["HIGH", "ECB mode leaks plaintext structure: " + t];
  for (const [name, sev] of WEAK_CIPHER) {
    if (up.startsWith(name)) return [sev, "Weak cipher transform in use: " + t];
  }
  if (up.startsWith("RSA") && up.includes("NOPADDING"))
    return ["HIGH", "RSA/NoPadding in use: " + t];
  return null;
}

function hookCrypto() {
  safe("Cipher.getInstance", () => {
    const Cipher = Java.use("javax.crypto.Cipher");
    Cipher.getInstance.overload("java.lang.String").implementation = function (t) {
      const c = classifyTransform(t);
      if (c) finding("weak_cipher", c[0], "M5: Insufficient Cryptography",
        "Weak cipher observed at runtime", c[1]);
      return this.getInstance(t);
    };
  });

  safe("MessageDigest.getInstance", () => {
    const MD = Java.use("java.security.MessageDigest");
    MD.getInstance.overload("java.lang.String").implementation = function (algo) {
      const up = (algo || "").toUpperCase();
      for (const [name, sev] of WEAK_DIGEST) {
        if (up === name) {
          finding("weak_digest", sev, "M5: Insufficient Cryptography",
            "Weak message digest observed", "MessageDigest." + algo + " used at runtime");
          break;
        }
      }
      return this.getInstance(algo);
    };
  });

  safe("Random-for-crypto", () => {
    const Rnd = Java.use("java.util.Random");
    Rnd.$init.overload().implementation = function () {
      observation("insecure-random", { note: "java.util.Random constructed" });
      return this.$init();
    };
  });

  safe("SecretKeySpec", () => {
    const SKS = Java.use("javax.crypto.spec.SecretKeySpec");
    SKS.$init.overload("[B", "java.lang.String").implementation = function (key, algo) {
      // Report only the length and algorithm, never the key bytes.
      observation("key-material", { algorithm: algo, keyLengthBytes: key.length });
      return this.$init(key, algo);
    };
  });
}

function hookTls() {
  // Detect app-defined TrustManagers/HostnameVerifiers for manual review.
  // We wrap and delegate to the original logic; we never weaken it.
  safe("SSLContext.init", () => {
    const SSLContext = Java.use("javax.net.ssl.SSLContext");
    SSLContext.init.overload(
      "[Ljavax.net.ssl.KeyManager;",
      "[Ljavax.net.ssl.TrustManager;",
      "java.security.SecureRandom"
    ).implementation = function (km, tm, sr) {
      if (tm) {
        for (let i = 0; i < tm.length; i++) {
          const cn = tm[i].$className || "";
          if (!cn.startsWith("com.android.") && !cn.startsWith("sun.") &&
              !cn.startsWith("java.") && !cn.startsWith("javax.")) {
            finding("custom_trust_manager", "MEDIUM", "M3: Insecure Communication",
              "Custom TrustManager installed",
              "App installs a custom TrustManager (" + cn + "). Verify it rejects " +
              "invalid or self-signed certificate chains.");
          }
        }
      }
      return this.init(km, tm, sr);
    };
  });

  safe("HostnameVerifier", () => {
    const HUC = Java.use("javax.net.ssl.HttpsURLConnection");
    HUC.setDefaultHostnameVerifier.implementation = function (v) {
      const cn = v ? v.$className : "null";
      finding("custom_hostname_verifier", "MEDIUM", "M3: Insecure Communication",
        "Default HostnameVerifier overridden",
        "App sets a custom default HostnameVerifier (" + cn + "). Verify it does " +
        "not unconditionally accept mismatched hostnames.");
      return this.setDefaultHostnameVerifier(v);
    };
  });
}

function looksSensitiveKey(k) {
  return /pass|secret|token|key|auth|session|pin|ssn|card/i.test(k || "");
}

function hookStorage() {
  safe("SharedPreferences.putString", () => {
    const Editor = Java.use("android.content.SharedPreferences$Editor");
    Editor.putString.implementation = function (key, value) {
      if (looksSensitiveKey(key)) {
        finding("plaintext_prefs", "MEDIUM", "M2: Insecure Data Storage",
          "Sensitive-looking key written to SharedPreferences",
          "Key '" + key + "' stored via SharedPreferences. Confirm value is " +
          "encrypted (e.g. EncryptedSharedPreferences) rather than cleartext.");
      }
      return this.putString(key, value);
    };
  });

  safe("Context.openFileOutput", () => {
    const Ctx = Java.use("android.content.ContextWrapper");
    Ctx.openFileOutput.implementation = function (name, mode) {
      // MODE_WORLD_READABLE = 1, MODE_WORLD_WRITEABLE = 2 (deprecated/insecure)
      if ((mode & 1) !== 0 || (mode & 2) !== 0) {
        finding("world_accessible_file", "HIGH", "M2: Insecure Data Storage",
          "File opened world-accessible",
          "openFileOutput('" + name + "', mode=" + mode + ") uses a world " +
          "readable/writable mode.");
      }
      return this.openFileOutput(name, mode);
    };
  });

  safe("SQLiteDatabase.execSQL", () => {
    const DB = Java.use("android.database.sqlite.SQLiteDatabase");
    DB.execSQL.overload("java.lang.String").implementation = function (sql) {
      observation("raw-sql", { op: "execSQL", sql: String(sql).slice(0, 120) });
      return this.execSQL(sql);
    };
  });
}

function hookWebView() {
  safe("WebSettings.setJavaScriptEnabled", () => {
    const WS = Java.use("android.webkit.WebSettings");
    WS.setJavaScriptEnabled.implementation = function (enabled) {
      if (enabled) {
        observation("webview", { setting: "javaScriptEnabled", value: true });
      }
      return this.setJavaScriptEnabled(enabled);
    };
    WS.setAllowFileAccess.implementation = function (enabled) {
      if (enabled) {
        finding("webview_file_access", "MEDIUM", "M1: Improper Platform Usage",
          "WebView file access enabled",
          "WebSettings.setAllowFileAccess(true) can expose local files to web content.");
      }
      return this.setAllowFileAccess(enabled);
    };
  });

  safe("WebView.addJavascriptInterface", () => {
    const WV = Java.use("android.webkit.WebView");
    WV.addJavascriptInterface.implementation = function (obj, name) {
      finding("js_interface", "HIGH", "M7: Client Code Quality",
        "addJavascriptInterface used",
        "A Java object is exposed to JavaScript as '" + name + "'. On exploitable " +
        "content this can lead to RCE; verify the bridge is strictly scoped.");
      return this.addJavascriptInterface(obj, name);
    };
  });
}

function hookLogging() {
  safe("Log", () => {
    const Log = Java.use("android.util.Log");
    ["d", "v", "i"].forEach((level) => {
      Log[level].overload("java.lang.String", "java.lang.String").implementation =
        function (tag, msg) {
          observation("log", { level, tag: String(tag), message: String(msg) });
          return this[level](tag, msg);
        };
    });
  });
}

rpc.exports = {
  // Called by the Python runner after attach to activate instrumentation.
  start: function () {
    Java.perform(function () {
      hookCrypto();
      hookTls();
      hookStorage();
      hookWebView();
      hookLogging();
      send({ kind: "ready" });
    });
  },
};
