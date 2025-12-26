-- 🔍 이 Lua가 보장하는 것
-- startToken 1회용
-- ready 상태 강제
-- turn/큐 동시성 보장
-- 새치기/중복 start 차단

-- KEYS:
-- 1: startTokenKey
-- 2: queueZsetKey
-- 3: readyKey

-- ARGV:
-- 1: userId
-- 2: machineId

-- 1) startToken 확인
local tokenValue = redis.call("GET", KEYS[1])
if not tokenValue then
    return "ERR_INVALID_TOKEN"
end

-- tokenValue: userId:machineId:queueEntryId
local parts = {}
for part in string.gmatch(tokenValue, "([^:]+)") do
    table.insert(parts, part)
end

local tokenUserId = parts[1]
local tokenMachineId = parts[2]
local queueEntryId = parts[3]

-- 2) userId / machineId 검증
if tokenUserId ~= ARGV[1] or tokenMachineId ~= ARGV[2] then
    return "ERR_TOKEN_MISMATCH"
end

-- 3) ready 상태 검증
local readyEntryId = redis.call("GET", KEYS[3])
if readyEntryId ~= queueEntryId then
    return "ERR_NOT_READY"
end

-- 4) 큐에서 제거
redis.call("ZREM", KEYS[2], queueEntryId)

-- 5) ready 제거
redis.call("DEL", KEYS[3])

-- 6) startToken 1회용 → 즉시 삭제
redis.call("DEL", KEYS[1])

-- 7) 성공 시 queueEntryId 반환
return queueEntryId
