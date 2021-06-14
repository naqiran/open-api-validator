# open-api-validator
Open API Validator to validates request and response against Swagger 2 and Open API contracts 

```bash
Usage: oas-validator [-hV] [-d=<requestBody>] -s=<schema> [-X=<method>]
                     [-H=<headers>]... <uri>
      <uri>                  API Endpoint URL
  -d, --data=<requestBody>   HTTP Request Body
  -h, --help
  -H, --headers              Http Headers=<headers>
  -s, --schema=<schema>      Open API Schema URL or File
  -V, --version
  -X, --method=<method>      HTTP Method
```

```bash
java -jar open-api-validator.jar --schema https://petstore3.swagger.io/api/v3/openapi.json --method GET --header 'API-TOKEN:asdfasdfasdfasdfasdf' https://petstore3.swagger.io/api/v3/store/order/1234
```

####Build Instruction:
- Java Version: Java 11

```bash
Mac/Unix: ./gradlew jar
Windows: gradlew.bat jar
```

