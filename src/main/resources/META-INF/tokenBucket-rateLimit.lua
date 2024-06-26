-- https://gist.github.com/ptarjan/e38f45f2dfe601419ca3af937fff574d#file-1-check_request_rate_limiter-rb-L11-L34
redis.replicate_commands()

local bucket_left_tokens_key = KEYS[1]
local bucket_last_access_timestamp_key = KEYS[2]
-- 每秒产生令牌数
local rate = tonumber(ARGV[1])
-- 令牌桶容量
local capacity = tonumber(ARGV[2])
-- 本次请求令牌数
local requested = tonumber(ARGV[3])
local now = redis.call('TIME')[1]

-- 令牌桶充满所需时间（秒）
local fill_time = capacity/rate
local ttl = math.floor(fill_time*2)

-- 剩余令牌数
local left_token_num_at_last_access = tonumber(redis.call("get", bucket_left_tokens_key))
if left_token_num_at_last_access == nil then
  left_token_num_at_last_access = capacity
end

-- 上次申请令牌时间
local last_access_timestamp = tonumber(redis.call("get", bucket_last_access_timestamp_key))
if last_access_timestamp == nil then
  last_access_timestamp = 0
end

-- 据上次申请令牌已过去多久
local time_duration = math.max(0, now-last_access_timestamp)
-- 预估可用令牌数 = 剩余令牌 + 以指定速率生产至今的令牌数（不超过令牌桶容量）
local available_tokens_since_last_access = math.min(capacity, left_token_num_at_last_access+(time_duration*rate))
-- 预估可用令牌数 >= 请求令牌数，则允许请求
local new_tokens = available_tokens_since_last_access
local allowed_num = 0
if (available_tokens_since_last_access >= requested) then
  -- 令牌桶中剩余令牌数 = 预估可用令牌数 - 请求令牌数
  new_tokens = available_tokens_since_last_access - requested
  allowed_num = 1
end

if ttl > 0 then
  -- 这个条件判断总是为真？
  -- 记录新的剩余令牌数，两倍填满时间后过期是为了取整？
  redis.call("setex", bucket_left_tokens_key, ttl, new_tokens)
  -- 记录本次请求时间
  redis.call("setex", bucket_last_access_timestamp_key, ttl, now)
end

return { allowed_num, new_tokens }
