@ECHO OFF
SETLOCAL

set BASE_DIR=%~dp0
set WRAPPER_DIR=%BASE_DIR%\.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set WRAPPER_PROPERTIES=%WRAPPER_DIR%\maven-wrapper.properties

if not "%JAVA_HOME%"=="" (
  set JAVACMD=%JAVA_HOME%\bin\java.exe
) else (
  set JAVACMD=java.exe
)

if not exist "%WRAPPER_JAR%" (
  if exist "%SystemRoot%\System32\curl.exe" (
    for /f "tokens=1,* delims==" %%A in (%WRAPPER_PROPERTIES%) do (
      if "%%A"=="wrapperUrl" set WRAPPER_URL=%%B
    )
    "%SystemRoot%\System32\curl.exe" -fsSL "%WRAPPER_URL%" -o "%WRAPPER_JAR%"
  ) else (
    "%JAVACMD%" "%WRAPPER_DIR%\MavenWrapperDownloader.java" "%BASE_DIR%"
  )
)

"%JAVACMD%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" -classpath "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
