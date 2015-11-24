#! /bin/bash
#exit
pip install influxdb
if [ $? -gt 0 ]; then 
  ctx logger info "Aborting ... "
  exit
fi


ctx logger info "Retrieving nodes_to_monitor and deployment_id"

NTM="$(ctx node properties nodes_to_monitor)"
ctx logger info "nodes_to_monitor = ${NTM}"
NTM=$(echo ${NTM} | sed "s/u'/'/g")
DPLID=$(ctx deployment id)
ctx logger info "deployment_id = ${DPLID}"

LOC=$(ctx download-resource scripts/policy.py)

rm /root/policycron

COMMAND="/root/cloudify.${DPLID}/env/bin/python ${LOC} \"${NTM}\" ${DPLID} > /root/logfile"
echo "*/1 * * * * $COMMAND" >> /root/policycron
crontab /root/policycron
