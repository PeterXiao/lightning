#!/bin/bash

aws lambda invoke \
    --function-name Lightning \
    --region eu-west-2 \
    --payload '{"region": "eu-west-2","bucket": "deliverymind.co.uk-lightning-aws-lambda","mode": "verify","jmeterCsv": "csv/jmeter/10_transactions.csv", "xml": "xml/1_client_2_server.xml", "perfmonCsv": "csv/perfmon/2_entries.csv"}' \
    response.json \
    &> /dev/null

OUT=`cat response.json | jq -r '.exitCode'`

echo -e ''; echo `basename "$0"`

if [ $OUT = 0 ];then
    echo "EXIT CODE = $OUT"
    echo "TEST PASSED"
    exit 0
else
    echo "EXIT CODE = $OUT"
    echo "TEST FAILED"
    exit 1
fi