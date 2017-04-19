#!/usr/bin/env bash

#*******************************************************************************
# Copyright 2016 General Electric Company.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#*******************************************************************************

unset PORT_OFFSET
source ./set-env-local.sh

export DIR=$( dirname "$( python -c "import os; print os.path.abspath('${BASH_SOURCE[0]}')" )" )

if [ "$#" -eq 0 ]; then
    unset JAVA_DEBUG_OPTS
    unset LOGGING_OPTS
    unset JACOCO_OPTS
fi

main() {
    while [ "$1" != '' ]; do
        case $1 in
            'debug')
                JAVA_DEBUG_OPTS='-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005'
                shift
                ;;
            'human-readable-logging')
                LOGGING_OPTS="-Dlog4j.debug -Dlog4j.configuration=file:${DIR}/src/main/resources/log4j-dev.xml"
                shift
                ;;
            'jacoco')
                JACOCO_OPTS="-javaagent:${HOME}/.m2/repository/org/jacoco/org.jacoco.agent/0.7.9/org.jacoco.agent-0.7.9-runtime.jar=output=tcpserver,address=*,dumponexit=false,destfile=$( python -c "import os; print os.path.abspath('${DIR}/../target/jacoco-it.exec')" )"
                shift
                ;;
            *)
                break
                ;;
        esac
    done

    cp "${DIR}"/target/acs-service-*-exec.jar "${DIR}"/.acs-service-copy.jar
    java -Xms1g -Xmx1g $JAVA_DEBUG_OPTS $LOGGING_OPTS $JACOCO_OPTS $PROXY_OPTS -jar "${DIR}"/.acs-service-copy.jar
}

main "$@"
