use actix::prelude::*;
use actix_web::{
    get,
    http::header::ContentType,
    post,
    web::{self},
    Error, HttpRequest, HttpResponse, Responder, Result,
};
use actix_web_actors::ws;

use uuid::Uuid;

use entity::{error_msg, ok, ok_data, UcAccount};

mod entity;
mod jwt;
mod redis;

mod server;
mod session;

#[post("/app/register")]
async fn register(user: web::Json<UcAccount>) -> Result<impl Responder> {
    let uuid = Uuid::new_v4();

    let account: UcAccount = user.0;

    // TODO encryption
    let uc_account: UcAccount = UcAccount {
        id: uuid.to_string(),
        ..account
    };

    // json serialization
    let account_str = serde_json::to_string(&uc_account).unwrap();

    redis::set(uc_account.account, account_str);

    Ok(web::Json(ok()))
}

#[post("/app/login")]
async fn login(user: web::Json<UcAccount>) -> Result<impl Responder> {
    let result = redis::get(user.0.account.to_string());
    if !result.is_ok() {
        return Ok(web::Json(error_msg(String::from("用户不存在"))));
    }

    let account_str = result.unwrap();

    let account: UcAccount = serde_json::from_str(account_str.as_str()).unwrap();

    // TODO encryption
    if !user.password.eq(account.password.as_str()) {
        return Ok(web::Json(error_msg(String::from("账号或密码错误"))));
    }

    let token = jwt::generate_token(user.0.account);
    Ok(web::Json(ok_data(token)))
}

#[get("/app/get_account_info")]
async fn get_account_info(req: HttpRequest) -> HttpResponse {
    let header = req.headers();
    let token = header.get("token").unwrap();

    let account = jwt::get_account_by_token(token.to_str().unwrap());

    let result = redis::get(account);
    if !result.is_ok() {
        let r = error_msg(String::from("用户不存在"));
        let result_str = serde_json::to_string(&r).unwrap();
        return HttpResponse::Ok().body(result_str);
    }

    let account_str = result.unwrap();

    let account: UcAccount = serde_json::from_str(account_str.as_str()).unwrap();

    let r = ok_data(account);
    let result_str = serde_json::to_string(&r).unwrap();
    HttpResponse::Ok()
        .content_type(ContentType::json())
        .body(result_str)
}

async fn chat_route(
    req: HttpRequest,
    stream: web::Payload,
    srv: web::Data<Addr<server::ChatServer>>,
) -> Result<HttpResponse, Error> {
    let path = req.path();

    // account
    let account = &path[6..path.len()];

    let resp = ws::start(
        session::MyWs {
            id: 0,
            account: account.to_string(),
            addr: srv.get_ref().clone(),
        },
        &req,
        stream,
    )?;

    Ok(resp)
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    use actix_web::{App, HttpServer};

    // start chat server actor
    let server = server::ChatServer::new().start();

    HttpServer::new(move || {
        App::new()
            .app_data(web::Data::new(server.clone()))
            .route("/chat/{account}", web::get().to(chat_route))
            .service(register)
            .service(login)
            .service(get_account_info)
    })
    .bind(("0.0.0.0", 6503))?
    .run()
    .await
}
