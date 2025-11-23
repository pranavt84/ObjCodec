# ClickHouse Data Encoder/Decoder

Encode your object and you will get a hash string.
Decode your hash string and you will get a json object.

## 📋 Requirements Met

- Support String, Int32, DataInput (nested arrays) example -   ["foo", ["bar", 42]]
- No built-in encoding/decoding libraries
- Accessible through API

## Protocol Specification
**Wire Format:**
- Array: `[0x03][count: 2 bytes][elements...]`
- String: `[0x02][length: 4 bytes][UTF-8 bytes]`
- Int32: `[0x01][value: 4 bytes]`

**Constraints:**
- Max array size: 1000 elements ✓
- Max string length: 1,000,000 characters ✓

**Performance:**
- Encode: 50-600 μs typical
- Decode: 40-150 μs typical


## Below-mentioned commands are exactly verfied on Windows and MAC/Linux
## Quick Start with Docker (Easiest)
```bash
# Pull and run
docker pull hanumatey1/clickhouse-encoder:latest
docker run -d -p 8080:8080 --name clickhouse-encoder hanumatey1/clickhouse-encoder:latest

On Mac-
docker run --platform=linux/amd64 -d -p 8080:8080 --name clickhouse-encoder hanumatey1/clickhouse-encoder:latest
```

## Building from Source - Java 17 or later must be installed
```bash
# Build
mvn clean package

# Run
java -jar target/clickhouse-encoder.jar 8080
```

## Functional API Endpoints
### Health Check
```bash
On Mac/Linux
curl http://localhost:8080/health

On Windows
curl http://localhost:8080/health `
  -Headers @{ "Content-Type" = "application/json" } `
  -Method Get
```

### Encode
```bash
On Mac/Linux
curl -s -X POST http://localhost:8080/encode \
  -H "Content-Type: application/json" \
  -d '["foo", ["bar", 42]]' | jq .
  
On Windows
curl http://localhost:8080/encode `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '["foo", ["bar", 42]]' `
  -Method Post  
```

Response:
```json
{
  "encoded": "0300020200000003666f6f0300020200000003626172010000002a",
  "size": 27,
  "sizeUnit": "bytes",
  "encodeTime": 150,
  "encodeTimeUnit": "microseconds"
}
```

### Decode
```bash
On Mac/Linux
curl -s -X POST http://localhost:8080/decode \
  -H "Content-Type: application/json" \
  -d '{"encoded": "0300020200000003666f6f0300020200000003626172010000002a"}' | jq .
  
On Windows
curl http://localhost:8080/decode `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"encoded":"0300020200000003666f6f0300020200000003626172010000002a"}' `
  -Method Post  
```

Response:
```json
{
  "decoded": ["foo", ["bar", 42]],
  "decodeTime": 80,
  "decodeTimeUnit": "microseconds"
}
```


## Complexity Analysis

**Time Complexity:**
- Encoding: O(n + m)
    - n = number of elements
    - m = sum of string byte lengths
    - Single pass through data structure

- Decoding: O(n + m)
    - Single pass, no backtracking
    - Direct byte-to-value conversion

**Space Complexity:**
- Binary output: O(n + m)
- Overhead: 5 bytes per string/int, 3 bytes per array


## Extensibility

To add new types:
1. Add type tag (e.g., 0x04 for Int64)
2. Define wire format
3. Write encode/decode implementation
4. Add new switch statements in encoder and decoder.

## Author

pranavt84 - ClickHouse Take-home Assessment

Docker Hub: https://hub.docker.com/r/hanumatey1/clickhouse-encoder