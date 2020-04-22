# Tokens API
Internal API to generate a new pair of tokens.
TokensRequest(uid: Int, pid: Option[Int], cid: Option[Int])

## Endpoints
| Verb | Endpoint | Description                     | Input                                  | Output            |
|------|----------|---------------------------------|----------------------------------------|-------------------|
| POST | `/tokens` | Generate new tokens.           | [Tokens Request](#Tokens-Request) | [Tokens](#Tokens), ClientError |
| POST | `/refresh` | Generate a new access token.  | Refresh token in `Authorization` header with `Bearer`. | String access token, Authorization Error |

## Models
### Tokens Request
| Attribute      | Type           | Contraints                           |
|----------------|----------------|--------------------------------------|
| uid            | `int`          | > 0                                  |
| pid       | `Option[int]`       | > 0                          |
| cid       | `Option[int]`       | > 0                          |

### Tokens
More information on tokens in the [Authentication Service](https://github.com/two-app/authentication-service).

| Attribute    | Type     |
|--------------|----------|
| accessToken  | `string` |
| refreshToken | `string` |