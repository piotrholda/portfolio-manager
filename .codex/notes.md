# Codex notes

## Maven on Windows sandbox

In this repo, `mvn` is not available globally and invoking `.\mvnw.cmd ...` from the PowerShell sandbox can fall through to Java help output. Run Maven wrapper through Java explicitly:

```powershell
$javaArgs = @(
  '-Dmaven.multiModuleProjectDirectory=C:\Users\phold\IdeaProjects\portfolio-manager',
  '-classpath',
  'C:\Users\phold\IdeaProjects\portfolio-manager\.mvn\wrapper\maven-wrapper.jar',
  'org.apache.maven.wrapper.MavenWrapperMain',
  '-Dtest=CorporateActionControllerIntegrationTest',
  'test'
)
java @javaArgs
```

The wrapper may need network access to resolve its Maven distribution, so rerun this command with escalation if the sandbox reports `Permission denied: connect`.
