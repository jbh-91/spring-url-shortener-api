@echo off
setlocal
cls

set "API_URL=http://localhost:8080/"
set "TEST_URL=Banane"

echo --------------------------------------------------
echo [1] Sende POST Request fuer: %TEST_URL%
echo --------------------------------------------------

:: curl sendet die URL. Wir zeigen das Ergebnis direkt an.
:: Windows CMD ist schlecht im Speichern von Variablen aus Befehlen, 
:: daher machen wir es hier interaktiv.

curl.exe -X POST -H "Content-Type: text/plain" -d "%TEST_URL%" %API_URL%
echo.
echo.

echo --------------------------------------------------
echo BITTE PRUEFEN: Oben sollte jetzt z.B. http://localhost:8080/1 stehen.
echo Merke dir den Code am Ende (z.B. 1).
echo --------------------------------------------------
set /p id="Gib den Code ein (z.B. 1): "

echo.
echo --------------------------------------------------
echo [2] Teste Redirect fuer ID: %id%
echo --------------------------------------------------

:: -v zeigt die Header an, wir suchen nach "Location"
curl.exe -v http://localhost:8080/%id% 2>&1 | findstr "Location"

echo.
echo --------------------------------------------------
echo Wenn oben "Location: https://..." steht, hat es geklappt!
pause