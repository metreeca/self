---
title:      How To Integrate With Your System
excerpt:    System requirements, installation and other system-related info
---

# System Requirements

## Browser

Metreeca/Self is a client-side web application: to run it you need the latest version of a supported web browser ([Firefox](http://www.mozilla.org/firefox/new/), [Chrome](https://www.google.com/chrome/), [Safari](https://www.apple.com/safari/), [Edge](http://microsoft.com/en-us/windows/microsoft-edge), [Explorer](http://windows.microsoft.com/en-us/internet-explorer/download-ie)); unsupported browsers may work as well, but they are not tested and your mileage may vary…

## SPARQL Endpoint

Metreeca/Self requires the SPARQL endpoint to fully support [CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) requests.

In order to support access to HTTP authenticated endpoints, it also requires the CORS implementation to reply with an origin specific `Access-Control-Allow-Origin` header, that is:

```
Access-Control-Allow-Origin: http://example.org/
```

rather than:

```
Access-Control-Allow-Origin: *
```

If these these requirements are not met, consider routing SPARQL requests from Metreeca/Self through a compliant CORS-enabled proxy.

# Installation

<p class="warning">Metreeca/Self code base is being repackaged: delivery and installation procedures are not stable and yet to be fully automated.</p>

For the time being, to use Metreeca/Self follow the steps below:

- clone and compile the `master` branch of the GitHub repository
  ```sh
  git clone https://github.com/metreeca/self.git
  cd self
  mvn clean install
  ```

- integrate into another project as a Maven WAR overlay; the application will be available at `http(s)://{host}/{static}/self/`
  ```xml
  <project>

    <dependencies>
  
      <dependency>
        <groupId>com.metreeca.self</groupId>
        <artifactId>main</artifactId>
        <version>${project.version}</version>
        <type>war</type>
        <scope>runtime</scope>
      </dependency>
  
    </dependencies>
  
    <build>
  
      <plugins>
    
        <plugin> <!-- https://maven.apache.org/plugins/maven-war-plugin/ -->
      
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-war-plugin</artifactId>
              <version>3.2.2</version>
      
              <configuration>
                <overlays>
      
                  <overlay>
                    <groupId>com.metreeca.self</groupId>
                    <artifactId>main</artifactId>
                    <targetPath>/{static}/</targetPath> <!-- static file deployment area -->
                  </overlay>
      
                </overlays>
              </configuration>
      
            </plugin>
      
        </plugins>
      
    </build>

  </project>
  ```
- deploy to a local server copying static assets under `main/target/metreeca-main-0.47.0/self` to its static asset folder; the application will be available at `http(s)://{host}/{static}/{name}/`

  ```bash
  cp -R main/target/metreeca-main-0.47.0/self/ {static}/{name}/
  ```

In both cases, you may want to provide a self-configuring launch link to a predefined SPARQL endpoint as:

```
http(s)://{host}/{static}/{name}/#endpoint={URL}
```

See below for more content configuration [options](#content-options).

<p class="warning">The target endpoint must be either <a href="#sparql-endpoint">CORS-enabled</a>, as discussed above, or managed by the same deployment server.</p>
