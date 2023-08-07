##
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2023 © by Rafał Wrzeszcz - Wrzasq.pl.
##

SHELL:=bash

default: build

clean:
	cargo clean

build:
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

full: clean build test-local check check-local doc
