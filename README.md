# 🛡️ Secure Software Design – Assignment 03  
### *Secure Coding – Vulnerability Fix Implementation*  
**Author:** i221756-i221745-i221660
**Course:** Secure Software Design (Fall 2025)

---

## 📘 Project Overview
This repository demonstrates the implementation of **secure coding practices** by identifying and fixing common vulnerabilities in a web application.  

The project includes:
- An **original vulnerable version** (`vulnerable` branch)
- A **secure fixed version** (`fixed` branch)
- A **Pull Request (PR)** for code review between the two branches

This structure provides a clear demonstration of vulnerability mitigation, secure coding principles, and Git-based version control.

---

## 🧩 Implemented Security Fixes

| # | Vulnerability | Fix Description |
|---|----------------|----------------|
| **1** | **Password Security** | Replaced plaintext password storage and comparison with BCrypt hashing. Updated signup and authentication flows accordingly. |
| **2** | **Access Control** | Hardened `SecurityFilterChain`. Removed global `permitAll` and enforced authentication + role-based access for sensitive endpoints. |
| **3** | **Resource Ownership Enforcement** | Added ownership validation in controllers to ensure users can only access their own data. |
| **4** | **Data Exposure Control** | Implemented DTOs to prevent sensitive information (passwords, roles, admin flags) from being exposed in API responses. |
| **5** | **Rate Limiting** | Added `Bucket4j`-based rate limiting filter to protect sensitive endpoints from abuse or brute-force attacks. |
| **6** | **Mass Assignment Prevention** | Restricted sensitive fields in DTOs and validated incoming data server-side. |
| **7** | **JWT Hardening** | Secured JWT handling by using environment-based secret keys, adding short token TTL, and enforcing issuer/audience validation. |
| **8** | **Error Handling & Logging** | Implemented a global exception handler with safe client error messages and detailed server-side logging. |
| **9** | **Input Validation** | Enforced numeric range and format validation for transfer inputs and rejected invalid or malicious data. |
| **10** | **Testing & Verification** | Performed integration testing for all fixes to confirm that vulnerabilities are eliminated and security mechanisms function correctly. |

---

## 🌿 Git Branch Structure

| Branch | Purpose |
|---------|----------|
| `vulnerable` | Contains the **original insecure version** of the application. |
| `fixed` | Contains the **securely fixed version** implementing all mitigations. |

---

## 🔀 Pull Request

A Pull Request (PR) was created to review and compare the **secure version (`fixed`)** with the **vulnerable version (`vulnerable`)**.

📎 **Pull Request Link:**  
[View PR on GitHub](https://github.com/i221756-cyber/SecureSoftwareAssignment03/pull/1)

---

## 🧠 How to View the Code

1. Clone the repository:
   ```bash
   git clone https://github.com/i221756-cyber/SecureSoftwareAssignment03.git
   cd SecureSoftwareAssignment03
   ```
2. Switch to the desired branch:
   ```bash
   git checkout vulnerable   # View original insecure code
   git checkout fixed        # View secure fixed code
   ```
3. Review the Pull Request to see side-by-side security improvements.

---

## 🧾 Assignment Requirements Fulfilled

- ✅ Vulnerable Code Branch (`vulnerable`)
- ✅ Fixed Code Branch (`fixed`)
- ✅ Pull Request for Code Review
- ✅ Separate descriptive commits for each fix
- ✅ Inline code comments explaining security changes
- ✅ Documentation and testing verification

---

## 🧑‍💻 Tools & Technologies
- **Java / Spring Boot**
- **JWT Authentication**
- **BCrypt Password Hashing**
- **Bucket4j Rate Limiting**
- **Maven**
- **Git & GitHub Version Control**

---

## 📚 Reference
This work was completed as part of **Assignment 03 – Secure Software Design (Fall 2025)**  
under the course outcome **CLO 2: Demonstrate secure coding practices and integrate standards/frameworks to minimize vulnerabilities in software development.**

---

**© 2025 cyber | National University Secure Software Design Lab**
