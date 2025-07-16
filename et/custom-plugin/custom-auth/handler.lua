local kong = kong
local http = require "resty.http"
local cjson = require "cjson"

local CustomAuthHandler = {
  PRIORITY = 1000,
  VERSION = "1.0",
}

function CustomAuthHandler:access(config)
  local auth_service_url = config.auth_service_url
  local auth_header = kong.request.get_header("Authorization")

  if not auth_header then
    return kong.response.exit(401, { message = "Missing Authorization header" })
  end

  local http = http.new()
  http:set_timeout(5000) -- 5 seconds timeout

  local res, err = http:request_uri(auth_service_url, {
    method = "GET",
    headers = {
      ["Authorization"] = auth_header
    }
  })

  if not res then
    kong.log.err("Auth service call failed: ", err)
    return kong.response.exit(500, { message = "Internal Server Error" })
  end

  if res.status ~= 200 then
    return kong.response.exit(res.status, { message = "Unauthorized" })
  end

  local user_id
  pcall(function()
    user_id = cjson.decode(res.body).user_id
  end)

  if not user_id then
    kong.log.err("Invalid auth service response: ", res.body)
    return kong.response.exit(500, { message = "Invalid auth service response" })
  end

  kong.service.request.set_header("X-User-Id", user_id)
end

return CustomAuthHandler