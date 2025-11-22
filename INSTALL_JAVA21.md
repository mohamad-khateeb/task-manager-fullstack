# Install Java 21 - Quick Guide

## Problem
Your system has Java 25, but this project requires Java 21. Lombok doesn't fully support Java 25 yet.

## Solution: Install Java 21

### Option 1: Using Homebrew (Recommended)

```bash
# Install Java 21
brew install openjdk@21

# Link it
sudo ln -sfn /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk

# Set JAVA_HOME for this session
export JAVA_HOME=/opt/homebrew/opt/openjdk@21

# Verify
java -version
```

You should see: `openjdk version "21.x.x"`

### Option 2: Download from Adoptium

1. Go to: https://adoptium.net/temurin/releases/?version=21
2. Download macOS ARM64 (if you have Apple Silicon) or x64
3. Install the .pkg file
4. Verify: `java -version`

### Option 3: Using SDKMAN (Alternative)

```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 21
sdk install java 21-tem

# Use it
sdk use java 21-tem
```

## After Installation

1. Verify Java version:
   ```bash
   java -version
   ```
   Should show: `openjdk version "21"`

2. Set JAVA_HOME (add to ~/.zshrc or ~/.bash_profile):
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)
   ```

3. Try building again:
   ```bash
   cd backend
   mvn clean compile
   ```

## Quick Fix for Current Session

If you just installed Java 21, set it for this terminal session:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH=$JAVA_HOME/bin:$PATH
java -version  # Should show 21
cd backend
mvn clean compile
```

