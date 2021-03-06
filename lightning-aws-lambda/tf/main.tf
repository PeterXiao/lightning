provider "aws" {
  region = "eu-west-2"
}

terraform {
  backend "s3" {
    bucket = "automatictester.co.uk-lightning-aws-lambda-tf-state"
    key = "lightning-lambda.tfstate"
    region = "eu-west-2"
  }
}

data "aws_caller_identity" "current" {}

resource "aws_lambda_function" "lightning_ci" {
  function_name = "Lightning"
  handler = "uk.co.automatictester.lightning.lambda.LightningHandler"
  runtime = "java8"
  s3_bucket = "automatictester.co.uk-lightning-aws-lambda-jar"
  s3_key = "lightning-aws-lambda.jar"
  source_code_hash = "${base64sha256(file("${path.module}/../target/lightning-aws-lambda.jar"))}"
  role = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:role/LightningLambda"
  memory_size = "${var.memory}"
  timeout = "${var.timeout}"
}
