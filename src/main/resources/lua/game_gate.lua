-- 이 Lua 하나로 동시성 / 순서 / 단일 진입 모두 해결
-- DB는 이 이후 단계에서만 접근

-- KEYS[1] = queue:{machineId}
-- KEYS[2] = gate:lock:{machineId}
-- KEYS[3] = gate:start:{machineId}:{userId}

-- ARGV[1] = userId
-- ARGV[2] = startToken
-- ARGV[3] = startTokenTTL (초)

-- 이미 머신 사용 중
if redis.call("EXISTS", KEYS[2]) == 1 then
    return { "WAIT" }
end

-- 큐 맨 앞 유저 확인
local head = redis.call("LINDEX", KEYS[1], 0)
if head ~= ARGV[1] then
    return { "WAIT" }
end

-- 머신 락
redis.call("SET", KEYS[2], ARGV[1])

-- start token 발급
redis.call("SETEX", KEYS[3], ARGV[3], ARGV[2])

-- 큐 pop
redis.call("LPOP", KEYS[1])

return { "START", ARGV[2] }
