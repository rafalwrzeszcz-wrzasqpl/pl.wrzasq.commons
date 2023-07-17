##
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2023 © by Rafał Wrzeszcz - Wrzasq.pl.
##

SHELL:=bash

default: build

init: init-rust init-cargo

init-rust:
	curl https://sh.rustup.rs -sSf | sh -s -- --default-toolchain nightly --component rustfmt clippy

init-cargo:
	cargo install cargo-strip --version 0.2.3
	cargo install cargo-udeps --version 0.1.40
	cargo install cargo-tarpaulin --version 0.26.1
	cargo install cargo-workspaces --version 0.2.44

init-local:
	cargo install cargo-audit --version 0.17.6

clean:
	cargo clean

build:
	cargo build --release
	cargo strip

build-dev:
	cargo build

test:
	cargo tarpaulin --all-features --out Xml --lib

test-local:
	docker run -d --rm --name dynamodb -p 8000:8000 amazon/dynamodb-local:2.0.0
	make test
	docker stop dynamodb

check:
	cargo fmt --check -- --config max_width=120,newline_style=Unix,edition=2021
	cargo clippy
	cargo udeps

check-local:
	cargo audit

doc:
	cargo doc --no-deps

full: clean build-dev test-local check check-local doc
