@echo off
mkdir C:\tmp\test_gerar2
copy .env C:\tmp\test_gerar2\.env

rem Add TEF_PROVIDER manually to test the "already exists" scenario
echo TEF_PROVIDER=STONE >> "C:\tmp\test_gerar2\.env"

rem Check if TEF_PROVIDER is already in .env
findstr /i "TEF_PROVIDER=" C:\tmp\test_gerar2\.env >nul
if errorlevel 1 (
    echo TEF variables added (was missing)
    echo TEF_PROVIDER=STONE >> "C:\tmp\test_gerar2\.env"
    echo TEF_CODIGO_CREDENCIADORA=123456 >> "C:\tmp\test_gerar2\.env"
    echo TEF_TERMINAL_ID=001 >> "C:\tmp\test_gerar2\.env"
) else (
    echo TEF variables already present - NOT adding duplicates
)

echo.
echo === Final .env content ===
findstr /i "TEF_PROVIDER\|TEF_CODIGO\|TEF_TERMINAL" C:\tmp\test_gerar2\.env