#!/bin/bash

set -euo pipefail

SAFE_SYMBOL_CHARS='@%_+=:.,-'

random_chars() {
    local chars="$1"
    local length="$2"
    local value=""
    local chunk

    while [ "${#value}" -lt "$length" ]; do
        chunk="$(LC_ALL=C tr -dc "$chars" </dev/urandom | head -c "$((length - ${#value}))" || true)"
        value="${value}${chunk}"
    done

    printf "%s" "$value"
}

generate_strong_secret() {
    local length="$1"
    if [ "$length" -lt 4 ]; then
        echo "generate_strong_secret length must be at least 4" >&2
        return 1
    fi

    local upper lower digit symbol rest
    upper="$(random_chars 'A-Z' 1)"
    lower="$(random_chars 'a-z' 1)"
    digit="$(random_chars '0-9' 1)"
    symbol="$(random_chars "$SAFE_SYMBOL_CHARS" 1)"
    rest="$(random_chars "A-Za-z0-9${SAFE_SYMBOL_CHARS}" "$((length - 4))")"

    printf "%s" "${upper}${lower}${digit}${symbol}${rest}"
}

get_env_value() {
    local env_file="$1"
    local key="$2"
    local value
    value="$(grep -E "^${key}=" "$env_file" | tail -n 1 | cut -d= -f2- || true)"
    printf "%s" "${value%$'\r'}"
}

is_placeholder_env_value() {
    local value="$1"
    case "$value" in
        "" | \
        "change_me_please_123" | \
        "replace_with_a_temporary_password" | \
        "replace_with_a_random_secret_at_least_32_chars" | \
        "replace_me" | \
        "replace_me_with_a_random_secret_at_least_32_chars")
            return 0
            ;;
        *)
            return 1
            ;;
    esac
}

upsert_env_value() {
    local env_file="$1"
    local key="$2"
    local value="$3"
    local tmp_file="${env_file}.tmp.$$"

    awk -v key="$key" -v value="$value" '
        BEGIN { updated = 0 }
        $0 ~ "^" key "=" {
            print key "=" value
            updated = 1
            next
        }
        { print }
        END {
            if (!updated) {
                print key "=" value
            }
        }
    ' "$env_file" > "$tmp_file"

    mv "$tmp_file" "$env_file"
}

ensure_env_secret() {
    local env_file="$1"
    local key="$2"
    local length="$3"
    local current_value generated_value

    current_value="$(get_env_value "$env_file" "$key")"
    if ! is_placeholder_env_value "$current_value"; then
        return 0
    fi

    generated_value="$(generate_strong_secret "$length")"
    upsert_env_value "$env_file" "$key" "$generated_value"
    printf "%s" "$generated_value"
}
