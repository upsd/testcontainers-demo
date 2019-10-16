# Testcontainers demo
A repo exhibiting a simple use of [Testcontainers](https://www.testcontainers.org) and the
[Localstack module](https://www.testcontainers.org/modules/localstack/).

See the [acceptance test](./src/test/java/acceptance/WriteToS3.java) for an implementation of using the Localstack module to provision instances of SQS and S3
running within docker (inside of a Localstack container). The simple (and naive) application reads from SQS and then
writes the message into S3 (for no good reason).