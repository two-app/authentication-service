# Credentials API
Store new credentials against a user ID.
Raw passwords are encoded.

## Endpoints
| Verb | Endpoint | Description                     | Input                                  | Output            |
|------|----------|---------------------------------|----------------------------------------|-------------------|
| POST | `/credentials`  | Store new credentials.            | [UserCredentials](#User-Credentials) | [Tokens](#Tokens), ClientError, NotFoundError |

## Models
### User Credentials
| Attribute      | Type           | Contraints                           |
|----------------|----------------|--------------------------------------|
| uid            | `int`          | > 0                                  |
| password       | `string`       | length >= 6                          |

### Tokens
More information on tokens in the [Authentication Service](https://github.com/two-app/authentication-service).

| Attribute    | Type     |
|--------------|----------|
| accessToken  | `string` |
| refreshToken | `string` |