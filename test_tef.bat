@echo off
mkdir C:\tmp\test_gerar
copy .env C:\tmp\test_gerar\.env

rem Check if TEF_PROVIDER is already in .env
findstr /i "TEF_PROVIDER=" C:\tmp\test_gerar\.env >nul
if errorlevel 1 (
    echo TEF_PROVIDER=STONE >> "C:\tmp\test_gerar\.env"
    echo TEF_CODIGO_CREDENCIADORA=123456 >> "C:\tmp\test_gerar\.env"
    echo TEF_TERMINAL_ID=001 >> "C:\tmp\test_gerar\.env"
    echo TEF variables added successfully
) else (
    echo TEF variables already present
)

echo.
echo === Final .env content ===
type C:\tmp\test_gerar\.env