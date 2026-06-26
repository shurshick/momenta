#!/usr/bin/env python3
"""Verify APK for installability."""
import os
import sys
from androguard.misc import AnalyzeAPK

apk_path = sys.argv[1] if len(sys.argv) > 1 else r'C:\Users\odmin4eg\Downloads\momenta-apk\app-dev-debug.apk'

a, d, dx = AnalyzeAPK(apk_path)

errors = []

print("=== Package Info ===")
print(f"Package: {a.get_package()}")
print(f"Application: {a.get_app_name()}")
print(f"Version Code: {a.get_androidversion_code()}")
print(f"Version Name: {a.get_androidversion_name()}")
print(f"Min SDK: {a.get_min_sdk_version()}")
print(f"Target SDK: {a.get_target_sdk_version()}")
print(f"Max SDK: {a.get_max_sdk_version()}")

if a.get_package() != "com.bghitech.momenta.dev":
    errors.append(f"WRONG PACKAGE: {a.get_package()}")

if a.get_min_sdk_version() != "24":
    errors.append(f"WRONG MIN_SDK: {a.get_min_sdk_version()}")

if a.get_target_sdk_version() != "34":
    errors.append(f"WRONG TARGET_SDK: {a.get_target_sdk_version()}")

print()
print("=== Manifest Attributes ===")
attrs = [
    ("android:debuggable", a.get_attribute_value("application", "android:debuggable")),
    ("android:testOnly", a.get_attribute_value("application", "android:testOnly")),
    ("android:extractNativeLibs", a.get_attribute_value("application", "android:extractNativeLibs")),
]
for name, val in attrs:
    print(f"{name}: {val}")
    if name == "android:testOnly" and val == "true":
        errors.append("FAIL: testOnly=true in APK manifest")

print()
print("=== Permissions ===")
perms = a.get_permissions()
print(f"Total: {len(perms)}")
for p in sorted(perms)[:20]:
    print(f"  {p}")

print()
print("=== Signing ===")
certs = a.get_certificates_v2()
print(f"Signing Scheme v2: {certs is not None and len(certs) > 0}")
certs_v1 = a.get_certificates()
print(f"Signing Scheme v1: {len(certs_v1) > 0}")
for i, cert in enumerate(certs_v1):
    print(f"  Cert {i}: subject={cert.subject.human_friendly}")
    print(f"    not_after={cert.not_valid_after}")

if not certs_v1:
    errors.append("FAIL: APK has no v1 signature (required for Package Installer on older devices)")

print()
print("=== APK Size ===")
size = os.path.getsize(apk_path)
print(f"{size:,} bytes ({size / 1024 / 1024:.1f} MB)")

if size == 0:
    errors.append("FAIL: APK is empty")

print()
print("=== Summary ===")
if errors:
    for e in errors:
        print(f"ERROR: {e}")
    sys.exit(1)
else:
    print("ALL CHECKS PASSED")
    sys.exit(0)
