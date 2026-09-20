#!/bin/sh
# JayasriMart Maven Wrapper for Unix / macOS / Git Bash
if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
elif [ -x "$HOME/apache-maven-3.9.9/bin/mvn" ]; then
    exec "$HOME/apache-maven-3.9.9/bin/mvn" "$@"
else
    echo "ERROR: Maven could not be found. Please ensure Apache Maven is installed." >&2
    exit 1
fi
