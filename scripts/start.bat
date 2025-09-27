@echo off
setlocal ENABLEDELAYEDEXPANSION
chcp 65001 >nul

REM =============== 路径配置(按需修改) ===============
set "KAFKA_HOME=E:\kafka_2.13-3.6.1"
set "MONGO_HOME=E:\homework\output\mongodb-windows-x86_64-8.0.8"
set "REDIS_HOME=E:\homework\output\Redis-3.0.504"

set "KAFKA_SCRIPT=%KAFKA_HOME%\bin\windows\kafka-server-start.bat"
set "KAFKA_CONF=%KAFKA_HOME%\config\kraft\server.properties"
set "KAFKA_START=%KAFKA_SCRIPT% %KAFKA_CONF%"
set "MONGO_START=%MONGO_HOME%\start.bat"
set "REDIS_EXE=%REDIS_HOME%\redis-server.exe"

REM =============== 存在性检查 ===============
call :checkExist "Kafka 启动脚本" "%KAFKA_SCRIPT%" || goto :end
call :checkExist "MongoDB 启动脚本" "%MONGO_START%" || goto :end
call :checkExist "Redis 可执行文件" "%REDIS_EXE%" || goto :end

echo.
echo 正在分别在三个新窗口启动: Kafka / MongoDB / Redis ...

start "Kafka" cmd /k call "%KAFKA_SCRIPT%" "%KAFKA_CONF%"
start "MongoDB" cmd /k call "%MONGO_START%"
start "Redis"   cmd /k "%REDIS_EXE%"

echo 所有启动命令已发出，请查看各自窗口日志。

goto :end

:checkExist
REM %1 描述  %2 路径
if exist %2 (
  echo [OK] %1: %2
  exit /b 0
) else (
  echo [ERR] 未找到 %1: %2
  exit /b 1
)

:end
endlocal

