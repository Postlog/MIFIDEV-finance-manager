#!/bin/bash

# Personal Finance Manager - Test Script
# This script runs tests and generates coverage reports

set -e  # Exit on error

echo "╔════════════════════════════════════════════════════════╗"
echo "║   Personal Finance Manager - Test Suite               ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    echo "   Visit: https://maven.apache.org/install.html"
    exit 1
fi

# Parse command line arguments
SHOW_COVERAGE=false
RUN_CHECKSTYLE=false
RUN_SPOTLESS=false
SKIP_TESTS=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -c|--coverage)
            SHOW_COVERAGE=true
            shift
            ;;
        -s|--style)
            RUN_CHECKSTYLE=true
            shift
            ;;
        -f|--format)
            RUN_SPOTLESS=true
            shift
            ;;
        -a|--all)
            SHOW_COVERAGE=true
            RUN_CHECKSTYLE=true
            RUN_SPOTLESS=true
            shift
            ;;
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        -h|--help)
            echo "Usage: ./test.sh [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  -c, --coverage     Generate and open coverage report"
            echo "  -s, --style        Run Checkstyle code quality checks"
            echo "  -f, --format       Run Spotless code formatting checks"
            echo "  -a, --all          Run all checks (tests, coverage, style)"
            echo "  --skip-tests       Skip tests (only run quality checks)"
            echo "  -h, --help         Show this help message"
            echo ""
            echo "Examples:"
            echo "  ./test.sh              # Run tests only"
            echo "  ./test.sh -c           # Run tests and show coverage"
            echo "  ./test.sh -a           # Run all checks"
            echo "  ./test.sh -s -f        # Run style and format checks"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use -h or --help for usage information"
            exit 1
            ;;
    esac
done

# Run tests
if [ "$SKIP_TESTS" = false ]; then
    echo "🧪 Running tests..."
    echo "════════════════════════════════════════════════════════"
    mvn clean test
    
    if [ $? -ne 0 ]; then
        echo ""
        echo "❌ Tests failed!"
        exit 1
    fi
    
    echo ""
    echo "✓ All tests passed!"
    echo ""
    
    # Count tests
    TEST_COUNT=$(find target/test-classes -name "*Test.class" | wc -l | tr -d ' ')
    echo "📊 Test Statistics:"
    echo "   Total test classes: $TEST_COUNT"
    echo ""
fi

# Generate coverage report
if [ "$SHOW_COVERAGE" = true ]; then
    echo "📊 Generating coverage report..."
    mvn jacoco:report -q
    
    if [ -f "target/site/jacoco/index.html" ]; then
        echo "✓ Coverage report generated!"
        echo ""
        
        # Extract coverage percentage from report
        if command -v grep &> /dev/null && [ -f "target/site/jacoco/index.html" ]; then
            COVERAGE=$(grep -oP 'Total[^%]+\K[0-9]+(?=%)' target/site/jacoco/index.html | head -n 1 || echo "N/A")
            if [ "$COVERAGE" != "N/A" ]; then
                echo "📈 Code Coverage: ${COVERAGE}%"
            fi
        fi
        
        echo ""
        echo "Opening coverage report in browser..."
        
        # Open browser based on OS
        if [[ "$OSTYPE" == "darwin"* ]]; then
            open target/site/jacoco/index.html
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            xdg-open target/site/jacoco/index.html 2>/dev/null || echo "Please open: target/site/jacoco/index.html"
        else
            echo "Please open: target/site/jacoco/index.html"
        fi
    else
        echo "⚠️  Coverage report not found"
    fi
    echo ""
fi

# Run Checkstyle
if [ "$RUN_CHECKSTYLE" = true ]; then
    echo "🔍 Running Checkstyle..."
    mvn checkstyle:check
    
    if [ $? -eq 0 ]; then
        echo "✓ Checkstyle passed!"
    else
        echo "⚠️  Checkstyle found issues"
    fi
    echo ""
fi

# Run Spotless
if [ "$RUN_SPOTLESS" = true ]; then
    echo "✨ Checking code formatting..."
    mvn spotless:check
    
    if [ $? -eq 0 ]; then
        echo "✓ Code formatting is correct!"
    else
        echo "⚠️  Code formatting issues found"
        echo "   Run 'mvn spotless:apply' to fix automatically"
    fi
    echo ""
fi

echo "════════════════════════════════════════════════════════"
echo "✓ Test suite completed!"
echo ""
echo "💡 Tip: Use './test.sh -h' to see all available options"

