use std::env;
use std::fs;
use std::process;

fn main() {
    let args: Vec<String> = env::args().collect();

    if args.len() < 2 {
        eprintln!("⚡ [VoltOS CLI] Ralat: Sila nyatakan fail Volt.dsl");
        eprintln!("Penggunaan: volt-core-cli-X <laluan-ke-Volt.dsl>");
        process::exit(1);
    }

    let dsl_path = &args[1];
    println!("⚡ [VoltOS CLI] Membaca fail: {}", dsl_path);

    match fs::read_to_string(dsl_path) {
        Ok(content) => {
            println!("⚡ [VoltOS CLI] Berjaya membaca kandungan DSL.");
            volt_core::parse_dsl(&content);
        }
        Err(err) => {
            eprintln!("⚡ [VoltOS CLI] Gagal membaca fail '{}': {}", dsl_path, err);
            process::exit(1);
        }
    }
}

