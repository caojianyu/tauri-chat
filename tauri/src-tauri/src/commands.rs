use tauri::command;

use std::io::BufRead;
use std::{
    fs::{create_dir, File, OpenOptions},
    io::{BufReader, Write},
    path::Path,
};

#[command]
pub fn save_chat_record(
    directory: String,
    first_account: String,
    second_account: String,
    json_str: String,
) {
    let mut current_dir = std::env::current_dir().unwrap();
    current_dir.push(directory);

    // create dir
    let dir = Path::new(current_dir.as_path());
    if !dir.exists() {
        create_dir(dir).unwrap();
    }

    let mut first_file_name = dir.to_path_buf();

    let mut first_name = String::new();
    first_name.push_str(first_account.as_str());
    first_name.push_str("_and_");
    first_name.push_str(second_account.as_str());

    first_file_name.push(first_name);

    let path = Path::new(first_file_name.as_path());

    let mut file_name: String = String::new();
    let mut file;

    if path.exists() {
        file_name.push_str(first_file_name.to_str().unwrap());
    } else {
        let mut second_file_name = dir.to_path_buf();

        let mut second_name = String::new();
        second_name.push_str(second_account.as_str());
        second_name.push_str("_and_");
        second_name.push_str(first_account.as_str());

        second_file_name.push(second_name);

        let path = Path::new(second_file_name.as_path());
        if path.exists() {
            file_name.push_str(second_file_name.to_str().unwrap());
        }
    }

    if file_name.is_empty() {
        file = File::create(path).expect("create failed");
    } else {
        file = OpenOptions::new()
            .append(true)
            .open(file_name)
            .expect("cannot open file");
    }

    let mut json = String::from(json_str);
    json.push_str("\n");

    file.write_all(json.as_bytes()).expect("write failed");
}

#[command]
pub fn get_chat_record(directory: String, first_account: String, second_account: String) -> String {
    let mut current_dir = std::env::current_dir().unwrap();
    current_dir.push(directory);

    // create dir
    let dir = Path::new(current_dir.as_path());
    if !dir.exists() {
        create_dir(dir).unwrap();
    }

    let mut first_file_name = dir.to_path_buf();

    let mut first_name = String::new();
    first_name.push_str(first_account.as_str());
    first_name.push_str("_and_");
    first_name.push_str(second_account.as_str());

    first_file_name.push(first_name);

    let path = Path::new(first_file_name.as_path());

    let mut file_name: String = String::new();

    if path.exists() {
        file_name.push_str(first_file_name.to_str().unwrap());
    } else {
        let mut second_file_name = dir.to_path_buf();

        let mut second_name = String::new();
        second_name.push_str(second_account.as_str());
        second_name.push_str("_and_");
        second_name.push_str(first_account.as_str());

        second_file_name.push(second_name);

        let path = Path::new(second_file_name.as_path());
        if path.exists() {
            file_name.push_str(second_file_name.to_str().unwrap());
        }
    }

    let mut data = json::JsonValue::new_array();

    if !file_name.is_empty() {
        let file = File::open(file_name.as_str()).expect("open failed");
        let reader = BufReader::new(file);

        for line in reader.lines() {
            let parsed = json::parse(&line.unwrap()).unwrap();
            data.push(parsed).unwrap();
        }
    }
    json::stringify(data)
}
