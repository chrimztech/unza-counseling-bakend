#!/bin/bash

# UNZA Counseling System - Development Startup Script
# This script starts both frontend and backend services

echo "🚀 Starting UNZA Counseling Management System..."
echo "================================================"

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js first."
    exit 1
fi

# Check if Java/Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Create logs directory
mkdir -p logs

echo "📦 Installing frontend dependencies..."
cd src
npm install

echo "🏗️  Building frontend..."
npm run build

# Start frontend in background
echo "🌐 Starting frontend server on http://localhost:3000..."
npm start &
FRONTEND_PID=$!
echo "Frontend PID: $FRONTEND_PID"

# Go back to root directory
cd ..

echo "☕ Starting backend server on http://localhost:8080..."
mvn spring-boot:run -Dspring-boot.run.profiles=development &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"

echo ""
echo "✅ Services Started Successfully!"
echo "================================"
echo "🌐 Frontend: http://localhost:3000"
echo "☕ Backend:  http://localhost:8080/api"
echo "📖 API Docs: http://localhost:8080/api-docs"
echo ""
echo "Press Ctrl+C to stop all services"

# Function to cleanup processes on exit
cleanup() {
    echo ""
    echo "🛑 Stopping services..."
    kill $FRONTEND_PID 2>/dev/null
    kill $BACKEND_PID 2>/dev/null
    echo "✅ All services stopped."
    exit 0
}

# Trap Ctrl+C
trap cleanup SIGINT

# Wait for both processes
wait