#!/bin/bash

BASE_URL="http://localhost:8080"
PASS=0
FAIL=0

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

test_roundtrip() {
    local name="$1"
    local input="$2"

    echo -e "\n=== Test: $name ==="
    echo "Input: $input"

    # Encode
    ENCODE_RESP=$(curl -s -X POST "$BASE_URL/encode" \
        -H "Content-Type: application/json" \
        -d "$input" 2>&1)

    if echo "$ENCODE_RESP" | grep -q "error"; then
        echo -e "${RED}✗ FAIL - Encode error${NC}"
        echo "$ENCODE_RESP" | jq .
        ((FAIL++))
        return
    fi

    ENCODED=$(echo "$ENCODE_RESP" | jq -r .encoded)
    SIZE=$(echo "$ENCODE_RESP" | jq -r .size)
    ENC_TIME=$(echo "$ENCODE_RESP" | jq -r .encodeTime)

    echo "Encoded: ${ENCODED:0:60}..."
    echo "Size: $SIZE bytes, Encode time: $ENC_TIME μs"

    # Decode
    DECODE_RESP=$(curl -s -X POST "$BASE_URL/decode" \
        -H "Content-Type: application/json" \
        -d "{\"encoded\": \"$ENCODED\"}" 2>&1)

    if echo "$DECODE_RESP" | grep -q "error"; then
        echo -e "${RED}✗ FAIL - Decode error${NC}"
        echo "$DECODE_RESP" | jq .
        ((FAIL++))
        return
    fi

    DECODED=$(echo "$DECODE_RESP" | jq -c .decoded)
    DEC_TIME=$(echo "$DECODE_RESP" | jq -r .decodeTime)

    echo "Decoded: $DECODED"
    echo "Decode time: $DEC_TIME μs"

    # Verify (simple string comparison)
    INPUT_CLEAN=$(echo "$input" | jq -c .)
    if [ "$INPUT_CLEAN" = "$DECODED" ]; then
        echo -e "${GREEN}✓ PASS${NC}"
        ((PASS++))
    else
        echo -e "${RED}✗ FAIL - Mismatch${NC}"
        echo "Expected: $INPUT_CLEAN"
        echo "Got:      $DECODED"
        ((FAIL++))
    fi
}

echo "================================================"
echo "  ClickHouse Encoder - Comprehensive Test Suite"
echo "================================================"

# Category 1: Basic Types
echo -e "\n--- Category 1: Basic Types ---"
test_roundtrip "1.1 Empty array" '[]'
test_roundtrip "1.2 Single integer" '[1]'
test_roundtrip "1.3 Single string" '["hello"]'
test_roundtrip "1.4 Single negative" '[-42]'

# Category 2: Multiple Elements
echo -e "\n--- Category 2: Multiple Elements ---"
test_roundtrip "2.1 Two integers" '[1, 2]'
test_roundtrip "2.2 Three integers" '[1, 2, 3]'
test_roundtrip "2.3 Five integers" '[1, 2, 3, 4, 5]'
test_roundtrip "2.4 Ten integers" '[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]'
test_roundtrip "2.5 Two strings" '["foo", "bar"]'
test_roundtrip "2.6 Mixed string and int" '["test", 123]'
test_roundtrip "2.7 Int and string" '[456, "test"]'

# Category 3: Negative Numbers
echo -e "\n--- Category 3: Negative Numbers ---"
test_roundtrip "3.1 Simple negative" '[-1]'
test_roundtrip "3.2 Multiple negatives" '[-1, -2, -3]'
test_roundtrip "3.3 Mixed positive/negative" '[1, -2, 3, -4]'
test_roundtrip "3.4 Zero" '[0]'
test_roundtrip "3.5 Min integer" '[-2147483648]'
test_roundtrip "3.6 Max integer" '[2147483647]'

# Category 4: Nested Arrays
echo -e "\n--- Category 4: Nested Arrays ---"
test_roundtrip "4.1 Empty nested" '[[]]'
test_roundtrip "4.2 Simple nested" '[["nested"]]'
test_roundtrip "4.3 Requirements example" '["foo", ["bar", 42]]'
test_roundtrip "4.4 Nested with multiple" '["outer", ["inner1", "inner2"]]'
test_roundtrip "4.5 Multiple nested" '[["a"], ["b"]]'
test_roundtrip "4.6 Deep nesting" '[[["deep"]]]'
test_roundtrip "4.7 Very deep" '[[[[["very deep"]]]]]'
test_roundtrip "4.8 Complex nesting" '["a", ["b", ["c", ["d"]]]]'

# Category 5: UTF-8 and Special Characters
echo -e "\n--- Category 5: UTF-8 and Special Characters ---"
test_roundtrip "5.1 Chinese characters" '["世界"]'
test_roundtrip "5.2 Japanese characters" '["こんにちは"]'
test_roundtrip "5.3 Mixed English-Chinese" '["Hello 世界"]'
test_roundtrip "5.4 Single emoji" '["🚀"]'
test_roundtrip "5.5 Multiple emojis" '["🚀", "🎉", "✨"]'
test_roundtrip "5.6 Mixed text and emoji" '["Hello 🌍"]'
test_roundtrip "5.7 Accented characters" '["café", "naïve"]'
test_roundtrip "5.8 Cyrillic" '["Привет"]'

# Category 6: Edge Cases - Strings
echo -e "\n--- Category 6: Edge Cases - Strings ---"
test_roundtrip "6.1 Empty string" '[""]'
test_roundtrip "6.2 Single char" '["a"]'
test_roundtrip "6.3 Whitespace" '[" "]'
test_roundtrip "6.4 Multiple spaces" '["   "]'
test_roundtrip "6.5 String with newline" '["line1\nline2"]'
test_roundtrip "6.6 String with tab" '["before\tafter"]'

# Category 7: Large Data
echo -e "\n--- Category 7: Large Data ---"

# Generate arrays properly (no trailing comma)
ARRAY_100=$(seq 1 100 | paste -sd, -)
ARRAY_500=$(seq 1 500 | paste -sd, -)
STRING_100=$(printf 'x%.0s' {1..100})
STRING_1000=$(printf 'x%.0s' {1..1000})

test_roundtrip "7.1 100 integers" "[$ARRAY_100]"
test_roundtrip "7.2 String 100 chars" "[\"$STRING_100\"]"
test_roundtrip "7.3 String 1000 chars" "[\"$STRING_1000\"]"
test_roundtrip "7.4 10 strings" '["s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10"]'

# Category 8: Mixed Complex Cases
echo -e "\n--- Category 8: Mixed Complex Cases ---"
test_roundtrip "8.1 All types" '["string", 42, ["nested", 99]]'
test_roundtrip "8.2 Complex mix" '[1, "two", [3, "four"], 5]'
test_roundtrip "8.3 Multiple nesting levels" '["a", ["b", ["c"]], "d"]'
test_roundtrip "8.4 Empty and non-empty" '[[], ["not empty"], []]'
test_roundtrip "8.5 Negatives in nested" '[1, [-1, -2], 3]'

# Category 9: Boundary Values
echo -e "\n--- Category 9: Boundary Values ---"
test_roundtrip "9.1 Zero values" '[0, 0, 0]'
test_roundtrip "9.2 Large positive" '[1000000, 2000000]'
test_roundtrip "9.3 Large negative" '[-1000000, -2000000]'
test_roundtrip "9.4 Powers of 2" '[1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024]'

# Category 10: Real-World Examples
echo -e "\n--- Category 10: Real-World Examples ---"
test_roundtrip "10.1 User data" '["john@example.com", 12345, ["tag1", "tag2"]]'
test_roundtrip "10.2 Log entry" '["ERROR", 500, ["stack trace line 1", "line 2"]]'
test_roundtrip "10.3 Database row" '["Alice", 30, ["admin", "user"]]'
test_roundtrip "10.4 Metrics" '[100, 200, 300, 400, 500]'
test_roundtrip "10.5 Nested config" '["app", ["db", ["host", "port"]]]'

# Summary
echo -e "\n================================================"
echo "  Test Results"
echo "================================================"
echo -e "Passed: ${GREEN}$PASS${NC}"
echo -e "Failed: ${RED}$FAIL${NC}"
echo "Total:  $((PASS + FAIL))"

if [ $FAIL -eq 0 ]; then
    echo -e "\n${GREEN}🎉 All tests passed!${NC}"
    exit 0
else
    echo -e "\n${RED}❌ Some tests failed${NC}"
    exit 1
fi