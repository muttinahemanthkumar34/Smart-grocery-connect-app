# GroceryConnect E2E Test Automation Framework

This directory contains the Appium, Selenium, and API Load testing frameworks, along with configuration managers, custom runners, and report compilers.

---

## 📂 Directory Structure

```
automation/
├── config/             # Appium and device capabilities configuration files
│   └── AppiumConfig.java
├── pages/              # Page Object Model (POM) page element selectors
│   ├── BasePage.java
│   ├── LoginPage.java
│   └── WebLoginPage.java
├── tests/              # Android app and web client TestNG E2E test cases
│   ├── BaseTest.java
│   ├── LoginTest.java
│   └── WebLoginTest.java
├── performance/        # k6 load testing and Artillery stress scripts
│   ├── k6-load-test.js
│   ├── artillery-load-test.yml
│   └── jmeter-test-plan.jmx
├── runners/            # Automated test runner and multi-report compiler
│   └── generate_reports.py
└── README.md           # This execution, security, and setup guide
```

---

## 💻 Local Execution Guide

To run the E2E suites and generate the Excel/HTML reports locally:

### 1. Prerequisites
- **Java JDK**: 11+
- **Python**: 3.8+ (with `openpyxl` and `pandas` installed: `pip install openpyxl pandas`)
- **k6**: Download from [k6.io](https://k6.io/) to run load tests.

### 2. Running the E2E Suite & Report Generator
Navigate to the root directory and run the compilation runner:
```bash
python automation/runners/generate_reports.py
```
This script runs the automation processes, creates folder outputs, and compiles all E2E results into:
- **Excel Report**: `Test Results/Excel/Automation_Test_Report.xlsx` (multi-sheet: executed, passed, failed, metrics)
- **HTML Dashboard**: `Test Results/HTML/execution-report.html` (interactive charts)

---

## 🚀 CI/CD Execution Guide

This framework runs automatically on every push and pull request via GitHub Actions:
- File: `.github/workflows/e2e.yml`
- It runs 6 parallel jobs representing the full 1800 test case matrix.
- Once finished, the **Compile Master Report & Deploy** job joins the reports, uploads the artifact bundle (valid for 30 days), and automatically deploys the interactive report to **GitHub Pages**.

---

## 🛠️ Troubleshooting Guide

### 1. Stuck Git Index Lock
**Error**: `fatal: Unable to create '.../.git/index.lock': File exists.`
**Solution**: Terminate background stuck git processes and clear the lock file:
```bash
taskkill /F /IM git.exe
rm .git/index.lock
```

### 2. Missing openpyxl in Python
**Error**: `ModuleNotFoundError: No module named 'openpyxl'`
**Solution**: Install the required Excel formatter module:
```bash
pip install openpyxl pandas
```

### 3. Connection Refused (Port 8001)
**Error**: `dial tcp 127.0.0.1:8001: connectex: No connection could be made...`
**Solution**: Start the PHP server by launching `START_BACKEND.bat` in the API root directory.
