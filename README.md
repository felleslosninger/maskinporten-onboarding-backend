<<<<<<< Updated upstream
# Maskinporten Onboarding: Backend

## Development

Run application with [Spring Profile](https://www.baeldung.com/spring-profiles) `dev` to use the default development environment.
=======
# Makinporten-onboaring-backend

## Development

Requires env-variables 
```
ANSATTPORTEN_CLIENT_ID
ANSATTPORTEN_CLIENT_SECRET
SIMPLIFIED_ONBOARDING_FRONTEND_URL
ANSATTPORTEN_LOGIN_BASE_URL
ANSATTPORTEN_API_BASE_URL
```
>>>>>>> Stashed changes

Additionally, you must inject the _ansattporten client secret_ in the context.

An easy way to manage this is with the [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) plugin and the following [.env](.env) file:
```
# client secret associated with `forenklet_onboarding_dev`
ANSATTPORTEN_CLIENT_SECRET=<thesecret>
```
