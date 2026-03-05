#!/usr/bin/env bash
set -euo pipefail

BASE_REF="${GITHUB_BASE_REF:-}"
HEAD_REF="${GITHUB_SHA:-HEAD}"

if [[ -n "$BASE_REF" ]]; then
  RANGE="$BASE_REF...$HEAD_REF"
else
  RANGE="HEAD~1...HEAD"
fi

CHANGED=$(git diff --name-only $RANGE || true)

PATTERN="*"

# Map de pacotes/arquivos para padrões de teste
if echo "$CHANGED" | grep -E '(src/test/java|src/main/java)/.*/(product|Product)' >/dev/null 2>&1; then
  PATTERN="*Product*"
fi
if echo "$CHANGED" | grep -E '(src/test/java|src/main/java)/.*/(user|User)' >/dev/null 2>&1; then
  PATTERN="*User*"
fi
if echo "$CHANGED" | grep -E '(src/test/java|src/main/java)/.*/(cart|Cart)' >/dev/null 2>&1; then
  PATTERN="*Cart*"
fi
# Se apenas utilitários/config mudaram, rodar smoke (ex.: EndToEndTest)
if echo "$CHANGED" | grep -E '(src/test/java|src/main/java)/.*/(utils|config|constants)' >/dev/null 2>&1; then
  PATTERN="*EndToEnd*"
fi

echo "TEST_PATTERN=$PATTERN" >> "$GITHUB_ENV"
echo "Determined TEST_PATTERN=$PATTERN"
