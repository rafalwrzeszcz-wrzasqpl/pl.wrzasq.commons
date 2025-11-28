##
# This file is part of the pl.wrzasq.commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2023 - 2025 © by Rafał Wrzeszcz - Wrzasq.pl.
##

SHELL:=bash

default: build

clean:
	cargo clean
	find . -name "*.profraw" -exec rm {} \;
	rm -rf coverage.lcov

build:
	cargo build

test:
	CARGO_INCREMENTAL=0 \
	RUSTFLAGS="-Cinstrument-coverage" \
	LLVM_PROFILE_FILE="cargo-test-%p-%m.profraw" \
	cargo test --all-features --lib

test-local:
	docker run -d --rm --name dynamodb -p 8000:8000 amazon/dynamodb-local:3.1.0
	make test
	docker stop dynamodb

check:
	cargo fmt --check
	cargo clippy
	cargo udeps

check-local:
	cargo audit

doc:
	cargo doc --no-deps

fix:
	cargo fmt

lcov:
	grcov . \
		--binary-path ./target/debug/deps/ \
		-s . \
		-t lcov \
		--branch \
		--ignore-not-existing \
		--ignore "../*" \
		--ignore "/*" \
		-o coverage.lcov

coverage:
	grcov . \
		--binary-path ./target/debug/deps/ \
		-s . \
		-t html \
		--branch \
		--ignore-not-existing \
		--ignore "../*" \
		--ignore "/*" \
		-o target/coverage

full: clean build test-local check check-local doc
