#!/bin/sh

echo $DB_CONFIG > /etc/sns/db.json
echo "Wrote config, running SNS"
exec "$@"
