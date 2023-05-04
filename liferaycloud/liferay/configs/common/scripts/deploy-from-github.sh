#!/bin/sh
# Script from https://gist.github.com/lgdd/facc238e562a3bd95857ddbd44f56019

set -e

# List of GitHub repositories
REPOS=$(cat << EOF
https://github.com/lgdd/liferay-ocr-documents
EOF
)

# Pipe separated list of keywords to exclude certain assets URLs
# Example: EXCLUDE="keyword1|keyword2|keyword3"
EXCLUDE=""

function githubDownload() {
  modules=$(echo "$REPOS" | cut -d/ -f4- | awk '{print "https://api.github.com/repos/"$1"/releases/latest"}' \
  | xargs curl -s \
  | grep "browser_download_url.*jar" \
  | cut -d : -f 2,3 \
  | tr -d \"
  )

  if [ -n "${EXCLUDE}" ]; then
    modules=$(echo "$modules" \
    | grep -vE "$EXCLUDE"
    )
  fi
  
  for module in ${modules[@]}; do
    echo "Download $module"
    curl --no-progress-meter -L -O "$module"
  done

  if [ -d "/opt/liferay/deploy" ]; then
    mv *.jar /opt/liferay/deploy
  else
    echo "Skip moving files to /opt/liferay/deploy as the folder doesn't exist."
  fi
}

githubDownload