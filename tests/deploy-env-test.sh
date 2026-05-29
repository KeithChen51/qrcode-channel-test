#!/bin/bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$ROOT_DIR/deploy/env-secrets.sh"

fail() {
    echo "FAIL: $1" >&2
    exit 1
}

assert_strong_secret() {
    local value="$1"
    local expected_length="$2"

    [ "${#value}" -eq "$expected_length" ] || fail "expected length $expected_length, got ${#value}"
    [[ "$value" =~ [A-Z] ]] || fail "missing uppercase character"
    [[ "$value" =~ [a-z] ]] || fail "missing lowercase character"
    [[ "$value" =~ [0-9] ]] || fail "missing digit"
    [[ "$value" =~ [@%_+=:.,-] ]] || fail "missing symbol"
    [[ ! "$value" =~ [\&\;\$\"\`\(\)\<\>\|\ \#] ]] || fail "contains shell-unsafe character"
}

password="$(generate_strong_secret 24)"
assert_strong_secret "$password" 24

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT
env_file="$tmp_dir/.env"
cat > "$env_file" <<'EOF'
ADMIN_USERNAME=admin
ADMIN_INITIAL_PASSWORD=change_me_please_123
AUTH_TOKEN_SECRET=
EOF

generated_password="$(ensure_env_secret "$env_file" ADMIN_INITIAL_PASSWORD 24)"
generated_token_secret="$(ensure_env_secret "$env_file" AUTH_TOKEN_SECRET 48)"

assert_strong_secret "$generated_password" 24
assert_strong_secret "$generated_token_secret" 48
grep -q "^ADMIN_INITIAL_PASSWORD=$generated_password$" "$env_file" || fail "admin password was not written to env file"
grep -q "^AUTH_TOKEN_SECRET=$generated_token_secret$" "$env_file" || fail "token secret was not written to env file"

existing_password="$(ensure_env_secret "$env_file" ADMIN_INITIAL_PASSWORD 24)"
[ -z "$existing_password" ] || fail "existing generated password should not be replaced"

echo "deploy env secret tests passed"
