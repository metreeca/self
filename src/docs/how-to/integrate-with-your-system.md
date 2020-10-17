---
title:      How To Integrate With Your System
excerpt:    System requirements, installation and other system-related info
---

# System Requirements

## Browser

Metreeca/Self is a client-side web application: to run it you need the latest version of an evergreen web browser ([Firefox](http://www.mozilla.org/firefox/new/), [Chrome](https://www.google.com/chrome/), [Safari](https://www.apple.com/safari/), [Edge](http://microsoft.com/en-us/windows/microsoft-edge)); unsupported browsers may work as well, but they are not tested and your mileage may vary…

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

# Integration

Metreeca/Self may be integrated with your system either as a Maven WAR overlay or as a standalone static web app. In both cases, you may want to provide a self-configuring launch link including a predefined SPARQL endpoint or other content configuration [options](embed-into-a-html-page.md#content-options) as:

```
http(s)://{host}/{static}/{name}/#endpoint={URL}
```

<p class="warning">The target endpoint must be either <a href="#sparql-endpoint">CORS-enabled</a>, as discussed above, or managed by the same deployment server.</p>


## As a Maven WAR Overlay

Add the following to the `pom.xml` of your project; the application will be available at `http(s)://{host}/{app}/{static}/self/`.
  ```xml
<project>

    <dependencies>
  
      <dependency>
        <groupId>com.metreeca</groupId>
        <artifactId>self</artifactId>
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
                    <groupId>com.metreeca</groupId>
                    <artifactId>metreeca-self</artifactId>
                    <targetPath>/{static}/</targetPath> <!-- static file deployment area -->
                  </overlay>
      
                </overlays>
              </configuration>
      
            </plugin>
      
        </plugins>
      
    </build>

  </project>
  ```

## As a Standalone Static Web App

First, clone and compile the `master` branch of the GitHub repository:

```shell
git clone https://github.com/metreeca/self.git
cd self
mvn clean install
```

Then, deploy to a local server copying static assets under `main/target/metreeca-self-{{ page.version }}/self` to a static asset folder; the application will be available at `http(s)://{host}/{static}/{name}/`

```shell
cp -R target/metreeca-self-{{ page.version }}/self/ {static}/{name}/
```
