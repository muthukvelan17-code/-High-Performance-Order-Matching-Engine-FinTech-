@echo off
setlocal
echo =======================================================
echo   Starting Custom gRPC Trading Bot (Market Maker)
echo =======================================================
echo.

set MAVEN_CMD=mvn
if exist ".maven\bin\mvn.cmd" (
    set MAVEN_CMD=.maven\bin\mvn.cmd
)

:: echo Compiling the bot...
:: call "%MAVEN_CMD%" compile -pl trading-engine-app -am

echo.
echo Running the bot...
call "%MAVEN_CMD%" exec:java -Dexec.mainClass="com.trading.engine.app.LiquidityProviderBot" -pl trading-engine-app -Dexec.classpathScope=runtime
pause
