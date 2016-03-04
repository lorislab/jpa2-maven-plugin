# jpa2-maven-plugin
Maven JPA2 plugin

```xml
<plugin>
  <groupId>org.lorislab.maven</groupId>
  <artifactId>jpa2-maven-plugin</artifactId>
  <version>1.0.0</version>
  <executions>
    <execution>
      <id>export-oracle</id>
      <phase>process-classes</phase>
      <goals>
        <goal>generate</goal>
      </goals>
      <configuration>
        <outputTargetDir>target/generated-schema/oracle</outputTargetDir>
        <databaseProductName>Oracle</databaseProductName>
        <databaseMajorVersion>12</databaseMajorVersion>
        <databaseMinorVersion>1</databaseMinorVersion>
      </configuration>
    </execution>
    <execution>
      <id>export-hsqldb</id>
      <phase>process-classes</phase>
      <goals>
        <goal>generate</goal>
      </goals>
      <configuration>
        <outputTargetDir>target/generated-schema/hsqldb</outputTargetDir>
        <databaseProductName>HSQL Database Engine</databaseProductName>
      </configuration>
    </execution>                            
  </executions>
  <dependencies>
    <dependency>
      <groupId>org.hibernate</groupId>
      <artifactId>hibernate-entitymanager</artifactId>
      <version>5.0.1.Final</version>
    </dependency>
  </dependencies>
</plugin>
```
