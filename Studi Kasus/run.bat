@echo off
title Futsal & Mini Soccer Arena Malang
echo Mengompilasi program Java...
javac *.java
if %ERRORLEVEL% EQU 0 (
    echo Menjalankan aplikasi GUI...
    java Main
) else (
    echo [ERROR] Kompilasi gagal!
    pause
)
