-- User: yufan
-- Date: 2024/6/26
-- 撤销对指定 Key 的一次计数
local rateLimitKey = KEYS[1];

local currValue = tonumber(redis.call('get', rateLimitKey));
if currValue == nil or currValue <= 1 then
    redis.call('del', rateLimitKey);
else
    redis.call('decr', rateLimitKey);
end

return true