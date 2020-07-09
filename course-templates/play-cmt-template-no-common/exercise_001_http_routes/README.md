## Play Framework - HTTP Routes

The router is the component in charge of translating each incoming HTTP request to an Action.

### STEPS

1. Create a new Action in the controller to read value from incoming request URL
   and add a greeting message to the response
   `http://localhost:9000/greet/Bob` Should return a response `Hello Bob`
2. Add an entry to `conf/routes` to map the url and the method in controller
3. Run tests and verify the application.
