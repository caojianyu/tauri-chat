use redis::Commands;

const URL: &str = "redis://:your_password@127.0.0.1:6379/";

pub fn set(key: String, value: String) {
    // connect to redis
    // The URL format is redis://[<username>][:<password>@]<hostname>[:port][/<db>]
    let client = redis::Client::open(URL).unwrap();
    let mut con = client.get_connection().unwrap();
    let _: () = con.set(key, value).unwrap();
}

pub fn get(key: String) -> redis::RedisResult<String> {
    let client = redis::Client::open(URL).unwrap();
    let mut con = client.get_connection().unwrap();
    con.get(key)
}