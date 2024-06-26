-- User: yufan
-- Date: 2024/6/26
-- 把 request 的令牌放回去
redis.replicate_commands()

local bucket_left_tokens_key = KEYS[1]
local bucket_last_access_timestamp_key = KEYS[2]

local rate = tonumber(ARGV[1])
local capacity = tonumber(ARGV[2])
local requested = tonumber(ARGV[3])

local now = redis.call('TIME')[1]

local fill_time = capacity/rate
local ttl = math.floor(fill_time*2)

-- 剩余令牌数
local left_token_num_at_last_access = tonumber(redis.call("get", bucket_left_tokens_key))
if left_token_num_at_last_access == nil then
  left_token_num_at_last_access = capacity
end

-- 上近申请令牌时间
local last_access_timestamp = tonumber(redis.call("get", bucket_last_access_timestamp_key))
if last_access_timestamp == nil then
  last_access_timestamp = 0
end

-- 据上次申请令牌已过去多久
local time_duration = math.max(0, now-last_access_timestamp)
-- 预估可用令牌数 = 剩余令牌 + 以指定速率生产至今的令牌数（不超过令牌桶容量）+ 放回去的令牌数
local available_tokens_since_last_access = math.min(capacity, left_token_num_at_last_access+(time_duration*rate)+requested)

if ttl > 0 then
  redis.call("setex", bucket_left_tokens_key, ttl, available_tokens_since_last_access)
  redis.call("setex", bucket_last_access_timestamp_key, ttl, now)
end

return true