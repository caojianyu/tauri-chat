use serde::{Deserialize, Serialize};

#[derive(Serialize)]
pub struct R<T> {
    pub code: u32,
    pub msg: String,
    pub data: T,
}

impl<T> R<T> {
    pub fn new(code: u32, msg: String, data: T) -> Self {
        Self {
            code: code,
            msg: msg,
            data: data,
        }
    }
}

pub fn ok() -> R<String> {
    R::new(200, String::from("success"), Default::default())
}

pub fn ok_msg(msg: String) -> R<String> {
    R::new(200, msg, Default::default())
}

pub fn ok_data<T>(data: T) -> R<T> {
    R::new(200, String::from("success"), data)
}

pub fn error() -> R<String> {
    R::new(500, String::from("failed"), String::new())
}

pub fn error_msg(msg: String) -> R<String> {
    R::new(500, msg, String::new())
}

#[derive(Serialize, Deserialize)]
pub struct UcAccount {
    pub id: String,
    pub account: String,
    pub avatar: String,
    pub password: String,
}

impl UcAccount {
    pub fn new() -> Self {
        Self {
            id: Default::default(),
            account: Default::default(),
            avatar: Default::default(),
            password: Default::default(),
        }
    }

    pub fn build(id: String, account: String, avatar: String, password: String) -> Self {
        UcAccount {
            id,
            account,
            avatar,
            password,
        }
    }
}