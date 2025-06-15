# Q-Cloak-qloak-
Qloak - Java Security Toolkit | File integrity checks, metadata removal, password management, keystroke monitoring, and password strength analysis.
# 🔒 Qloak - Java Security Toolkit

Qloak is a comprehensive security suite implemented in Java, featuring five essential security tools for file protection, password management, and system monitoring.

## 🧰 Features

1. **File Integrity Checker**  
   - Generate/verify SHA-256 checksums
   - Detect file tampering and corruption

2. **Metadata Remover**  
   - Clean sensitive metadata from:
     - 📄 PDF documents
     - 🖼️ Images (JPG, PNG, GIF, BMP, TIFF)
     - 📝 DOCX files

3. **Password Manager**  
   - Store credentials with AES/DES encryption
   - Master password protection
   - Secure credential retrieval

4. **Password Strength Checker**  
   - Analyze password robustness
   - Provide improvement suggestions
   - Strength rating system (Weak/Moderate/Strong)

5. **Keystroke Monitor**  
   - Capture keyboard input (educational use only)
   - Auto-save logs every 30 seconds
   - Ethical usage warning system

## ⚙️ Installation & Usage

### Prerequisites
- Java JDK 11+
- Maven

### Build & Run
```bash
git clone https://github.com/your-username/Qloak.git
cd Qloak
mvn package
