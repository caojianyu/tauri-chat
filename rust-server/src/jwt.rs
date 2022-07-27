use jsonwebtoken::errors::ErrorKind;
use jsonwebtoken::{decode, encode, DecodingKey, EncodingKey, Header, Validation};
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
struct Claims {
    aud: String,
    exp: usize,
}

pub fn generate_token(account: String) -> String {
    let key = b"secret";
    let my_claims = Claims {
        aud: account.to_owned(),
        exp: 10000000000,
    };

    let token = match encode(
        &Header::default(),
        &my_claims,
        &EncodingKey::from_secret(key),
    ) {
        Ok(t) => t,
        Err(_) => panic!(), // in practice you would return the error
    };

    token
}

pub fn get_account_by_token(token: &str) -> String {
    let claims = verify(token);
    claims.aud
}

fn verify(token: &str) -> Claims {
    let key = b"secret";
    let token_data = match decode::<Claims>(
        token,
        &DecodingKey::from_secret(key),
        &Validation::default(),
    ) {
        Ok(c) => c,
        Err(err) => match *err.kind() {
            ErrorKind::InvalidToken => panic!("Token is invalid"), // Example on how to handle a specific error
            ErrorKind::InvalidIssuer => panic!("Issuer is invalid"), // Example on how to handle a specific error
            _ => panic!("Some other errors"),
        },
    };

    token_data.claims
}
