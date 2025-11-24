@echo off
echo ================================================
echo CT-Tibet-WMS Test Runner
echo ================================================
echo.

:menu
echo Please select an option:
echo [1] Run all tests
echo [2] Run unit tests only
echo [3] Run integration tests only
echo [4] Run tests with coverage report
echo [5] Run specific test class
echo [6] Exit
echo.
set /p choice="Enter your choice (1-6): "

if "%choice%"=="1" goto run_all
if "%choice%"=="2" goto run_unit
if "%choice%"=="3" goto run_integration
if "%choice%"=="4" goto run_coverage
if "%choice%"=="5" goto run_specific
if "%choice%"=="6" goto end

echo Invalid choice. Please try again.
echo.
goto menu

:run_all
echo.
echo Running all tests...
call mvn test
goto result

:run_unit
echo.
echo Running unit tests only...
call mvn test -Dtest=*ServiceImplTest
goto result

:run_integration
echo.
echo Running integration tests only...
call mvn test -Dtest=*FlowTest
goto result

:run_coverage
echo.
echo Running tests with coverage report...
call mvn clean test jacoco:report
echo.
echo Coverage report generated at: target\site\jacoco\index.html
start target\site\jacoco\index.html
goto result

:run_specific
echo.
set /p testclass="Enter test class name (e.g., StatisticsServiceImplTest): "
echo Running %testclass%...
call mvn test -Dtest=%testclass%
goto result

:result
echo.
echo ================================================
echo Test execution completed!
echo ================================================
echo.
pause
goto menu

:end
echo.
echo Goodbye!
exit
