# Start Build on Openshit

```bash
mvn clean package
oc start-build twitter-to-kafka --from-dir=. --follow
```
