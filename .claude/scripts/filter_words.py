#!/usr/bin/env python3
"""Filter /tmp/words.json by integer field 'n'."""
import json
import sys


def main() -> int:
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <integer>", file=sys.stderr)
        return 2

    # 정수만 허용 — 부동소수점, 문자열, 셸 메타문자 모두 여기서 차단됨
    try:
        n = int(sys.argv[1])
    except ValueError:
        print(
            f"Error: argument must be an integer, got: {sys.argv[1]!r}",
            file=sys.stderr,
        )
        return 2

    try:
        with open("/tmp/words.json", encoding="utf-8") as f:
            data = json.load(f)
    except FileNotFoundError:
        print("Error: /tmp/words.json not found", file=sys.stderr)
        return 1
    except json.JSONDecodeError as e:
        print(f"Error: invalid JSON in /tmp/words.json: {e}", file=sys.stderr)
        return 1

    if not isinstance(data, list):
        print("Error: expected JSON array at top level", file=sys.stderr)
        return 1

    matches = [w for w in data if isinstance(w, dict) and w.get("n") == n]
    print(json.dumps(matches, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    sys.exit(main())