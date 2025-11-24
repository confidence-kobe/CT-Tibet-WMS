#!/bin/bash

# CT-Tibet-WMS Test Runner

echo "================================================"
echo "CT-Tibet-WMS Test Runner"
echo "================================================"
echo ""

show_menu() {
    echo "Please select an option:"
    echo "[1] Run all tests"
    echo "[2] Run unit tests only"
    echo "[3] Run integration tests only"
    echo "[4] Run tests with coverage report"
    echo "[5] Run specific test class"
    echo "[6] Exit"
    echo ""
}

run_all_tests() {
    echo ""
    echo "Running all tests..."
    mvn test
}

run_unit_tests() {
    echo ""
    echo "Running unit tests only..."
    mvn test -Dtest='*ServiceImplTest'
}

run_integration_tests() {
    echo ""
    echo "Running integration tests only..."
    mvn test -Dtest='*FlowTest'
}

run_coverage() {
    echo ""
    echo "Running tests with coverage report..."
    mvn clean test jacoco:report
    echo ""
    echo "Coverage report generated at: target/site/jacoco/index.html"

    # Try to open the report in the default browser
    if command -v xdg-open > /dev/null; then
        xdg-open target/site/jacoco/index.html
    elif command -v open > /dev/null; then
        open target/site/jacoco/index.html
    else
        echo "Please open target/site/jacoco/index.html manually to view the coverage report"
    fi
}

run_specific_test() {
    echo ""
    read -p "Enter test class name (e.g., StatisticsServiceImplTest): " testclass
    echo "Running $testclass..."
    mvn test -Dtest="$testclass"
}

show_result() {
    echo ""
    echo "================================================"
    echo "Test execution completed!"
    echo "================================================"
    echo ""
}

# Main loop
while true; do
    show_menu
    read -p "Enter your choice (1-6): " choice

    case $choice in
        1)
            run_all_tests
            show_result
            ;;
        2)
            run_unit_tests
            show_result
            ;;
        3)
            run_integration_tests
            show_result
            ;;
        4)
            run_coverage
            show_result
            ;;
        5)
            run_specific_test
            show_result
            ;;
        6)
            echo ""
            echo "Goodbye!"
            exit 0
            ;;
        *)
            echo "Invalid choice. Please try again."
            echo ""
            ;;
    esac
done
