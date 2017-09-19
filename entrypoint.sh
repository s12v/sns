#!/bin/sh

if [ "$(echo $DB_CONFIG | xargs)" != "" ]; then
  echo $DB_CONFIG > /etc/sns/db.json
  echo "Wrote config, running SNS"
fi

exec "$@"
