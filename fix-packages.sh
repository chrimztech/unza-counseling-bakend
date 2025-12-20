#!/bin/bash

# Fix all package declarations by removing "main.java." prefix
find src/main/java -name "*.java" -type f -exec sed -i 's/^package main\.java\./package /g' {} \;

echo "Fixed all package declarations!"