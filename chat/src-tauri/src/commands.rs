use tauri::command;

use std::{
    fs::{File, OpenOptions},
    io::Write,
    path::Path,
};

/// 将聊天记录写入文件
#[command]
pub fn save_chat_history(file_path: String, mut data: String) {
    let path = Path::new(&file_path);

    let mut file;
    if path.exists() {
        file = OpenOptions::new()
            .append(true)
            .open(file_path)
            .expect("cannot open file");
    } else {
        file = File::create(file_path).expect("create failed");
    }

    data.push('\n');

    file.write_all(data.as_bytes()).expect("write failed");
}
