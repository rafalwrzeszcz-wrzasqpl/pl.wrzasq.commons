<!---
# This file is part of the ChillDev-Commons.
#
# @license http://mit-license.org/ The MIT license
# @copyright 2017 © by Rafał Wrzeszcz - Wrzasq.pl.
-->

# General information

This library contains set of small integration/wrapper classes. The point is to cover common integration cases (especially for [**Lambda**](https://aws.amazon.com/lambda/) development), without bloating the code and dependencies. Main purpose is to separate **Lambda** entry point from the logic. This allows making thin, lightweight entry points that interact with implementation, which you can encapsulate in the testable library.

# Logging

As usual in **Chillout Development** projects, [**SLF4J**](https://www.slf4j.org/) is used. For using with **AWS** lambda we recommend using [`io.symphonia:lambda-logging`](https://github.com/symphoniacloud/lambda-monitoring/tree/master/lambda-logging) as a backend.
