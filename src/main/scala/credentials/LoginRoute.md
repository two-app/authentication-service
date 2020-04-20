# Login API
Generate tokens for a valid email + password combination.

The login API reaches out to the `user-service` to retrieve
data on the user.

## Endpoints
| Verb | Endpoint | Description                     | Input                                  | Output            |
|------|----------|---------------------------------|----------------------------------------|-------------------|
| POST | `/login`  | Attempt a login.            | [LoginCredentials](#Login-Credentials) | [Tokens](#Tokens), ClientError, NotFoundError |

## Models
### Login Credentials
| Attribute      | Type           | Contraints                           |
|----------------|----------------|--------------------------------------|
| email          | `string`       | unique && https://stackoverflow.com/a/32445372 |
| password       | `string`       | length >= 6                          |

### Tokens
More information on tokens in the [Authentication Service](https://github.com/two-app/authentication-service).

| Attribute    | Type     |
|--------------|----------|
| accessToken  | `string` |
| refreshToken | `string` |