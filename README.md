# Maskinporten-onboarding-backend

## Environment variables

Maskiporten-oboarding-backend acts as a proxy for the datasharing-api, routing between test and prod. This allows prod-users to control test and production from a singe website.

Which backend is targeted is decided in the `ProxyEnvironmentController`. The routes under `api/{env}/datasharing` uses the config-object `maskinporten-config`. This can be a list of environments. 

```
maskinporten-config:
  - environment: envname
    api: datasharing-api-baseurl-for-envname
    authorization-server: maskinporten-authorization-server-for-envname
```

Given path parameter `env=envname`, the proxycontroller will use the value `datasharing-api-baseurl-for-envname` for baseurl when accessing clients and scopes, and use the `envname` and `authorization-server` information to populate the onboardingguide and exampels in the frontend.



## Development

Additionally, you must inject the _ansattporten client secret_ in the context.

An easy way to manage this is with the [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) plugin and the following [.env](.env) file:
```
# client secret associated with `forenklet_onboarding_dev`
ANSATTPORTEN_CLIENT_SECRET=<thesecret>
```

