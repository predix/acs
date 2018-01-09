#!/bin/bash

find . -name "*.java" -print0 | xargs -0 sed -i '' -e 's/package com.ge.predix/package org.eclipse/g'

find . -name "*.java" -print0 | xargs -0 sed -i '' -e 's/import com.ge.predix.acs/import org.eclipse.acs/g'
find . -name "*.java" -print0 | xargs -0 sed -i '' -e 's/import com.ge.predix.test/import org.eclipse.test/g'
find . -name "*.java" -print0 | xargs -0 sed -i '' -e 's/import static com.ge.predix.test/import static org.eclipse.test/g'
find . -name "*.java" -print0 | xargs -0 sed -i '' -e 's/import static com.ge.predix.integration.test/import static org.eclipse.integration.test/g'


declare -a arr1=($(grep -l --exclude={*.class,*.jar,*checkstyle-result.xml,*testng-results.xml,*.iml*,*.xml*,*.sh*,*ZoneAwareTokenServiceConfig.java*,*AcsRequestContextHolder.java*,*SpringSecurityZoneResolver.java*,*MockSecurityContext*,*ZoneServiceTest.java*} --exclude-dir={*failsafe-reports*,*.idea*,*.git*} -r "com.ge.predix" .))

for i in "${arr1[@]}"
do
    sed -i '' -e 's/com.ge.predix/org.eclipse/g' $i

done

# declare -a arr=($(grep -l --exclude={*.class,*.jar,*checkstyle-result.xml,*testng-results.xml,*.java} --exclude-dir={*failsafe-reports*,*.idea*,*.git*} -r "com.ge.predix" .))

# for i in "${arr[@]}"
# do
#    sed -i '' -e 's/com.ge.predix/org.eclipse/g' $i
#    # or do whatever with individual element of the array
# done
