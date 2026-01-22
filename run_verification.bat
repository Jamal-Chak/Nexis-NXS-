@echo off
echo ==========================================
echo      NEXIS BLOCKCHAIN VERIFICATION
echo ==========================================
echo.
echo Waiting for you to finish installing Java and Maven...
echo If you haven't installed them yet, this script will fail.
echo.
pause

echo.
echo [1/3] Running Private Network Test (Phase 7)...
call mvn clean compile exec:java -Dexec.mainClass="com.nexis.app.TestPhase7"
echo.
echo ------------------------------------------
echo.

echo [2/3] Running High Fee "Fee War" Test (Phase 8)...
call mvn clean compile exec:java -Dexec.mainClass="com.nexis.app.TestHighFees"
echo.
echo ------------------------------------------
echo.

echo [3/3] Running Stress Test & Scaling Metrics (Phase 6)...
call mvn clean compile exec:java -Dexec.mainClass="com.nexis.app.TestPhase6"
echo.
echo ------------------------------------------
echo.

echo All Verification Tests Complete!
echo You can now check the output above for [PASS] messages.
pause
