@echo off
chcp 65001 >nul
title AgroLink 一键启动

echo ========================================
echo    AgroLink 一键启动
echo ========================================
echo.

:: 检查 Git Bash 路径
where bash >nul 2>&1
if %errorlevel% neq 0 (
    echo [失败] 未找到 Git Bash，请安装 Git for Windows
    pause
    exit /b 1
)

:: 执行启动脚本
bash "%~dp0start.sh"

pause
